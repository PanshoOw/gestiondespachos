package com.duoc.gestiondespachos.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.duoc.gestiondespachos.dto.GuiaS3ResponseDTO;
import com.duoc.gestiondespachos.entity.EstadoGuia;
import com.duoc.gestiondespachos.entity.GuiaDespacho;
import com.duoc.gestiondespachos.repository.GuiaDespachoRepository;

@Service
public class GuiaS3Service {

    private static final String MENSAJE_ID_GUIA_NULO = "El ID de la guía no puede ser nulo.";
    private static final String MENSAJE_RUTA_TEMPORAL_NULA = "La ruta temporal EFS de la guía no puede ser nula.";
    private static final String TIPO_CONTENIDO_PDF = "application/pdf";
    private static final String MENSAJE_SUBIDA_CORRECTA = "Guía de despacho subida correctamente a AWS S3.";
    private static final String MENSAJE_REEMPLAZO_CORRECTO = "Guía de despacho reemplazada correctamente en AWS S3.";
    private static final String MENSAJE_ELIMINACION_CORRECTA = "Guía de despacho eliminada correctamente desde AWS S3.";

    private final AmazonS3 s3Client;
    private final GuiaDespachoRepository guiaDespachoRepository;
    private final GuiaArchivoService guiaArchivoService;
    private final String bucketName;

    public GuiaS3Service(AmazonS3 s3Client,
                         GuiaDespachoRepository guiaDespachoRepository,
                         GuiaArchivoService guiaArchivoService,
                         @Value("${app.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.guiaDespachoRepository = guiaDespachoRepository;
        this.guiaArchivoService = guiaArchivoService;
        this.bucketName = bucketName;
    }

    @Transactional
    public GuiaS3ResponseDTO subirGuiaAS3(Long idGuia) {

        Long idValidado = Objects.requireNonNull(idGuia, MENSAJE_ID_GUIA_NULO);
        validarBucketConfigurado();

        GuiaDespacho guiaDespacho = buscarGuiaPorId(idValidado);

        Path rutaArchivo = obtenerRutaArchivoTemporal(guiaDespacho);

        String keyS3 = guiaArchivoService.construirKeyS3(guiaDespacho);
        String nombreArchivo = guiaArchivoService.obtenerNombreArchivo(idValidado);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(TIPO_CONTENIDO_PDF);

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName,
                keyS3,
                rutaArchivo.toFile()
        ).withMetadata(metadata);

        s3Client.putObject(putObjectRequest);

        guiaDespacho.setKeyS3(keyS3);
        guiaDespacho.setEstado(EstadoGuia.SUBIDA_S3);

        GuiaDespacho guiaActualizada = guiaDespachoRepository.save(guiaDespacho);

        return new GuiaS3ResponseDTO(
                guiaActualizada.getId(),
                guiaActualizada.getNumeroPedido(),
                guiaActualizada.getTransportista(),
                guiaActualizada.getFechaDespacho(),
                bucketName,
                guiaActualizada.getKeyS3(),
                nombreArchivo,
                guiaActualizada.getEstado(),
                MENSAJE_SUBIDA_CORRECTA
        );
    }

    @Transactional(readOnly = true)
    public byte[] descargarGuiaDesdeS3(Long idGuia, String transportistaSolicitante) {

        Long idValidado = Objects.requireNonNull(idGuia, MENSAJE_ID_GUIA_NULO);
        validarBucketConfigurado();

        if (!StringUtils.hasText(transportistaSolicitante)) {
            throw new IllegalArgumentException("Debe indicar el transportista solicitante para descargar la guía.");
        }

        GuiaDespacho guiaDespacho = buscarGuiaPorId(idValidado);

        validarPermisoDescarga(guiaDespacho, transportistaSolicitante);

        String keyS3 = obtenerKeyS3Validada(guiaDespacho);

        validarExistenciaArchivoS3(keyS3);

        try (S3Object objetoS3 = s3Client.getObject(bucketName, keyS3);
             S3ObjectInputStream inputStream = objetoS3.getObjectContent()) {

            return inputStream.readAllBytes();

        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo descargar la guía desde AWS S3.", ex);
        }
    }

    @Transactional
    public GuiaS3ResponseDTO reemplazarGuiaEnS3(Long idGuia) {

        Long idValidado = Objects.requireNonNull(idGuia, MENSAJE_ID_GUIA_NULO);
        validarBucketConfigurado();

        GuiaDespacho guiaDespacho = buscarGuiaPorId(idValidado);

        String keyAnterior = obtenerKeyS3Validada(guiaDespacho);

        validarExistenciaArchivoS3(keyAnterior);

        Path rutaArchivo = obtenerRutaArchivoTemporal(guiaDespacho);

        String nuevaKeyS3 = guiaArchivoService.construirKeyS3(guiaDespacho);
        String nombreArchivo = guiaArchivoService.obtenerNombreArchivo(idValidado);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(TIPO_CONTENIDO_PDF);

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName,
                nuevaKeyS3,
                rutaArchivo.toFile()
        ).withMetadata(metadata);

        s3Client.putObject(putObjectRequest);

        if (!keyAnterior.equals(nuevaKeyS3)) {
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, keyAnterior);
            s3Client.deleteObject(deleteObjectRequest);
        }

        guiaDespacho.setKeyS3(nuevaKeyS3);
        guiaDespacho.setEstado(EstadoGuia.ACTUALIZADA);

        GuiaDespacho guiaActualizada = guiaDespachoRepository.save(guiaDespacho);

        return new GuiaS3ResponseDTO(
                guiaActualizada.getId(),
                guiaActualizada.getNumeroPedido(),
                guiaActualizada.getTransportista(),
                guiaActualizada.getFechaDespacho(),
                bucketName,
                guiaActualizada.getKeyS3(),
                nombreArchivo,
                guiaActualizada.getEstado(),
                MENSAJE_REEMPLAZO_CORRECTO
        );
    }

    @Transactional
    public GuiaS3ResponseDTO eliminarGuiaDesdeS3(Long idGuia) {

        Long idValidado = Objects.requireNonNull(idGuia, MENSAJE_ID_GUIA_NULO);
        validarBucketConfigurado();

        GuiaDespacho guiaDespacho = buscarGuiaPorId(idValidado);

        String keyS3 = obtenerKeyS3Validada(guiaDespacho);

        validarExistenciaArchivoS3(keyS3);

        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, keyS3);

        s3Client.deleteObject(deleteObjectRequest);

        guiaDespacho.setEstado(EstadoGuia.ELIMINADA);

        GuiaDespacho guiaActualizada = guiaDespachoRepository.save(guiaDespacho);

        return new GuiaS3ResponseDTO(
                guiaActualizada.getId(),
                guiaActualizada.getNumeroPedido(),
                guiaActualizada.getTransportista(),
                guiaActualizada.getFechaDespacho(),
                bucketName,
                guiaActualizada.getKeyS3(),
                guiaArchivoService.obtenerNombreArchivo(idValidado),
                guiaActualizada.getEstado(),
                MENSAJE_ELIMINACION_CORRECTA
        );
    }

    public String obtenerNombreArchivo(Long idGuia) {
        Long idValidado = Objects.requireNonNull(idGuia, MENSAJE_ID_GUIA_NULO);
        return guiaArchivoService.obtenerNombreArchivo(idValidado);
    }

    private GuiaDespacho buscarGuiaPorId(Long idGuia) {
        return guiaDespachoRepository.findById(idGuia)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una guía de despacho con el ID: " + idGuia
                ));
    }

    private Path obtenerRutaArchivoTemporal(GuiaDespacho guiaDespacho) {

        String rutaTemporal = Objects.requireNonNull(
                guiaDespacho.getRutaTemporalEfs(),
                MENSAJE_RUTA_TEMPORAL_NULA
        );

        Path rutaArchivo = Path.of(rutaTemporal);

        if (Files.exists(rutaArchivo)) {
            return rutaArchivo;
        }

        Path rutaRegenerada = guiaArchivoService.generarArchivoPdfEnEfs(guiaDespacho);

        guiaDespacho.setRutaTemporalEfs(rutaRegenerada.toString());
        guiaDespachoRepository.save(guiaDespacho);

        return rutaRegenerada;
    }

    private void validarPermisoDescarga(GuiaDespacho guiaDespacho, String transportistaSolicitante) {

        String transportistaRegistrado = guiaDespacho.getTransportista();

        if (!transportistaRegistrado.equalsIgnoreCase(transportistaSolicitante.trim())) {
            throw new IllegalArgumentException(
                    "El transportista indicado no tiene permiso para descargar esta guía."
            );
        }
    }

    private String obtenerKeyS3Validada(GuiaDespacho guiaDespacho) {

        String keyS3 = guiaDespacho.getKeyS3();

        if (!StringUtils.hasText(keyS3)) {
            throw new IllegalArgumentException("La guía aún no ha sido subida a AWS S3.");
        }

        return keyS3;
    }

    private void validarExistenciaArchivoS3(String keyS3) {

        if (!s3Client.doesObjectExist(bucketName, keyS3)) {
            throw new IllegalArgumentException(
                    "No existe una guía almacenada en S3 con la ruta: " + keyS3
            );
        }
    }

    private void validarBucketConfigurado() {
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException(
                    "El nombre del bucket S3 no está configurado. Revise la variable AWS_S3_BUCKET_NAME."
            );
        }
    }
}