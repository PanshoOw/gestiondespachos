package com.duoc.gestiondespachos.service;

import com.duoc.gestiondespachos.entity.GuiaDespacho;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Service
public class GuiaArchivoService {

    private static final String MENSAJE_GUIA_NULA = "La guía de despacho no puede ser nula.";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Locale LOCALE_CHILE = new Locale("es", "CL");

    private final Path rutaBaseEfs;

    public GuiaArchivoService(@Value("${app.efs.mount-path}") String efsMountPath) {
        this.rutaBaseEfs = Paths.get(efsMountPath);
    }

    public Path generarArchivoPdfEnEfs(GuiaDespacho guiaDespacho) {

        GuiaDespacho guiaValidada = Objects.requireNonNull(guiaDespacho, MENSAJE_GUIA_NULA);

        if (guiaValidada.getId() == null) {
            throw new IllegalArgumentException("La guía debe estar guardada antes de generar el archivo PDF.");
        }

        Path directorioGuia = construirDirectorioGuia(guiaValidada);
        String nombreArchivo = obtenerNombreArchivo(guiaValidada.getId());
        Path rutaArchivo = directorioGuia.resolve(nombreArchivo);

        try {
            Files.createDirectories(directorioGuia);
            crearPdfGuia(guiaValidada, rutaArchivo);
            return rutaArchivo;

        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar la guía PDF en el almacenamiento temporal EFS.", ex);
        }
    }

    public String obtenerNombreArchivo(Long idGuia) {
        Long idValidado = Objects.requireNonNull(idGuia, "El ID de la guía no puede ser nulo.");
        return "guia-despacho-" + idValidado + ".pdf";
    }

    public String construirKeyS3(GuiaDespacho guiaDespacho) {
        GuiaDespacho guiaValidada = Objects.requireNonNull(guiaDespacho, MENSAJE_GUIA_NULA);

        String fecha = guiaValidada.getFechaDespacho().format(FORMATO_FECHA);
        String transportistaNormalizado = normalizarTextoParaRuta(guiaValidada.getTransportista());

        return fecha + "/" + transportistaNormalizado + "/" + obtenerNombreArchivo(guiaValidada.getId());
    }

    private Path construirDirectorioGuia(GuiaDespacho guiaDespacho) {

        String fecha = guiaDespacho.getFechaDespacho().format(FORMATO_FECHA);
        String transportistaNormalizado = normalizarTextoParaRuta(guiaDespacho.getTransportista());

        return rutaBaseEfs
                .resolve(fecha)
                .resolve(transportistaNormalizado);
    }

    private void crearPdfGuia(GuiaDespacho guiaDespacho, Path rutaArchivo) throws IOException {

        NumberFormat formatoNumero = NumberFormat.getNumberInstance(LOCALE_CHILE);

        try (PDDocument documento = new PDDocument()) {

            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {

                float y = 730;

                y = escribirLinea(contenido, "GUÍA DE DESPACHO", 50, y, PDType1Font.HELVETICA_BOLD, 18);
                y -= 15;

                y = escribirLinea(contenido, "Número de guía: " + guiaDespacho.getId(), 50, y, PDType1Font.HELVETICA_BOLD, 12);
                y = escribirLinea(contenido, "Número de pedido: " + guiaDespacho.getNumeroPedido(), 50, y, PDType1Font.HELVETICA, 12);
                y = escribirLinea(contenido, "Fecha de despacho: " + guiaDespacho.getFechaDespacho(), 50, y, PDType1Font.HELVETICA, 12);
                y -= 10;

                y = escribirLinea(contenido, "DATOS DEL TRANSPORTISTA", 50, y, PDType1Font.HELVETICA_BOLD, 12);
                y = escribirLinea(contenido, "Transportista: " + guiaDespacho.getTransportista(), 50, y, PDType1Font.HELVETICA, 12);
                y -= 10;

                y = escribirLinea(contenido, "DATOS DEL DESTINATARIO", 50, y, PDType1Font.HELVETICA_BOLD, 12);
                y = escribirLinea(contenido, "Destinatario: " + guiaDespacho.getDestinatario(), 50, y, PDType1Font.HELVETICA, 12);
                y = escribirLinea(contenido, "Dirección: " + guiaDespacho.getDireccionDestino(), 50, y, PDType1Font.HELVETICA, 12);
                y = escribirLinea(contenido, "Ciudad: " + guiaDespacho.getCiudadDestino(), 50, y, PDType1Font.HELVETICA, 12);
                y -= 10;

                y = escribirLinea(contenido, "DETALLE DE LA CARGA", 50, y, PDType1Font.HELVETICA_BOLD, 12);
                y = escribirLinea(contenido, "Descripción: " + guiaDespacho.getDescripcionCarga(), 50, y, PDType1Font.HELVETICA, 12);
                y = escribirLinea(contenido, "Peso: " + formatearPeso(guiaDespacho.getPesoKg(), formatoNumero) + " kg", 50, y, PDType1Font.HELVETICA, 12);
                y -= 10;

                escribirLinea(contenido, "Estado: " + guiaDespacho.getEstado(), 50, y, PDType1Font.HELVETICA_BOLD, 12);
            }

            documento.save(rutaArchivo.toFile());
        }
    }

    private float escribirLinea(PDPageContentStream contenido, String texto, float x, float y,
                                PDType1Font fuente, int tamanio) throws IOException {

        contenido.beginText();
        contenido.setFont(fuente, tamanio);
        contenido.newLineAtOffset(x, y);
        contenido.showText(limpiarTexto(texto));
        contenido.endText();

        return y - 20;
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }

    private String formatearPeso(BigDecimal peso, NumberFormat formatoNumero) {
        BigDecimal pesoSeguro = peso != null ? peso : BigDecimal.ZERO;
        return formatoNumero.format(pesoSeguro);
    }

    private String normalizarTextoParaRuta(String texto) {
        if (texto == null || texto.isBlank()) {
            return "sin-transportista";
        }

        return texto
                .trim()
                .toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-)|(-$)", "");
    }
}