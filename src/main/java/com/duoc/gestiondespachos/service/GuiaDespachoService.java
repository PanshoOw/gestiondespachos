package com.duoc.gestiondespachos.service;

import com.duoc.gestiondespachos.dto.GuiaDespachoRequestDTO;
import com.duoc.gestiondespachos.dto.GuiaDespachoResponseDTO;
import com.duoc.gestiondespachos.dto.GuiaDespachoUpdateDTO;
import com.duoc.gestiondespachos.dto.HistorialGuiaDTO;
import com.duoc.gestiondespachos.entity.EstadoGuia;
import com.duoc.gestiondespachos.entity.GuiaDespacho;
import com.duoc.gestiondespachos.repository.GuiaDespachoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class GuiaDespachoService {

    private static final String MENSAJE_ID_GUIA_NULO = "El ID de la guía no puede ser nulo.";

    private final GuiaDespachoRepository guiaDespachoRepository;
    private final GuiaArchivoService guiaArchivoService;

    public GuiaDespachoService(GuiaDespachoRepository guiaDespachoRepository,
                               GuiaArchivoService guiaArchivoService) {
        this.guiaDespachoRepository = guiaDespachoRepository;
        this.guiaArchivoService = guiaArchivoService;
    }

    @Transactional
    public GuiaDespachoResponseDTO crearGuia(GuiaDespachoRequestDTO requestDTO) {

        String numeroPedidoNormalizado = requestDTO.getNumeroPedido().trim();

        if (guiaDespachoRepository.existsByNumeroPedidoIgnoreCase(numeroPedidoNormalizado)) {
            throw new IllegalArgumentException(
                    "Ya existe una guía asociada al número de pedido: " + numeroPedidoNormalizado
            );
        }

        GuiaDespacho guiaDespacho = new GuiaDespacho();
        guiaDespacho.setNumeroPedido(numeroPedidoNormalizado);
        guiaDespacho.setTransportista(requestDTO.getTransportista().trim());
        guiaDespacho.setDestinatario(requestDTO.getDestinatario().trim());
        guiaDespacho.setDireccionDestino(requestDTO.getDireccionDestino().trim());
        guiaDespacho.setCiudadDestino(requestDTO.getCiudadDestino().trim());
        guiaDespacho.setFechaDespacho(requestDTO.getFechaDespacho());
        guiaDespacho.setDescripcionCarga(requestDTO.getDescripcionCarga().trim());
        guiaDespacho.setPesoKg(requestDTO.getPesoKg());
        guiaDespacho.setEstado(EstadoGuia.GENERADA);

        GuiaDespacho guiaGuardada = guiaDespachoRepository.save(guiaDespacho);

        Path rutaArchivo = guiaArchivoService.generarArchivoPdfEnEfs(guiaGuardada);
        guiaGuardada.setRutaTemporalEfs(rutaArchivo.toString());

        GuiaDespacho guiaActualizada = guiaDespachoRepository.save(guiaGuardada);

        return convertirAResponseDTO(guiaActualizada);
    }

    @Transactional
    public GuiaDespachoResponseDTO modificarGuia(Long id, GuiaDespachoUpdateDTO updateDTO) {

        GuiaDespacho guiaDespacho = buscarEntidadPorId(id);

        guiaDespacho.setTransportista(updateDTO.getTransportista().trim());
        guiaDespacho.setDestinatario(updateDTO.getDestinatario().trim());
        guiaDespacho.setDireccionDestino(updateDTO.getDireccionDestino().trim());
        guiaDespacho.setCiudadDestino(updateDTO.getCiudadDestino().trim());
        guiaDespacho.setFechaDespacho(updateDTO.getFechaDespacho());
        guiaDespacho.setDescripcionCarga(updateDTO.getDescripcionCarga().trim());
        guiaDespacho.setPesoKg(updateDTO.getPesoKg());
        guiaDespacho.setEstado(EstadoGuia.ACTUALIZADA);

        GuiaDespacho guiaGuardada = guiaDespachoRepository.save(guiaDespacho);

        Path rutaArchivoActualizada = guiaArchivoService.generarArchivoPdfEnEfs(guiaGuardada);
        guiaGuardada.setRutaTemporalEfs(rutaArchivoActualizada.toString());

        GuiaDespacho guiaActualizada = guiaDespachoRepository.save(guiaGuardada);

        return convertirAResponseDTO(guiaActualizada);
    }

    @Transactional(readOnly = true)
    public GuiaDespachoResponseDTO obtenerGuiaPorId(Long id) {
        GuiaDespacho guiaDespacho = buscarEntidadPorId(id);
        return convertirAResponseDTO(guiaDespacho);
    }

    @Transactional(readOnly = true)
    public GuiaDespacho obtenerEntidadPorId(Long id) {
        return buscarEntidadPorId(id);
    }

    @Transactional(readOnly = true)
    public List<HistorialGuiaDTO> consultarHistorial(String transportista, LocalDate fecha) {

        boolean tieneTransportista = StringUtils.hasText(transportista);
        boolean tieneFecha = fecha != null;

        if (!tieneTransportista && !tieneFecha) {
            throw new IllegalArgumentException(
                    "Debe indicar al menos un filtro: transportista, fecha, o ambos."
            );
        }

        List<GuiaDespacho> guias;

        if (tieneTransportista && tieneFecha) {
            guias = guiaDespachoRepository.findByTransportistaIgnoreCaseAndFechaDespacho(
                    transportista.trim(),
                    fecha
            );
        } else if (tieneTransportista) {
            guias = guiaDespachoRepository.findByTransportistaIgnoreCase(transportista.trim());
        } else {
            guias = guiaDespachoRepository.findByFechaDespacho(fecha);
        }

        if (guias.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se encontraron guías generadas para los filtros indicados."
            );
        }

        return guias.stream()
                .map(this::convertirAHistorialDTO)
                .toList();
    }

    private GuiaDespacho buscarEntidadPorId(Long id) {

        Long idValidado = Objects.requireNonNull(id, MENSAJE_ID_GUIA_NULO);

        return guiaDespachoRepository.findById(idValidado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una guía de despacho con el ID: " + idValidado
                ));
    }

    private GuiaDespachoResponseDTO convertirAResponseDTO(GuiaDespacho guiaDespacho) {
        return new GuiaDespachoResponseDTO(
                guiaDespacho.getId(),
                guiaDespacho.getNumeroPedido(),
                guiaDespacho.getTransportista(),
                guiaDespacho.getDestinatario(),
                guiaDespacho.getDireccionDestino(),
                guiaDespacho.getCiudadDestino(),
                guiaDespacho.getFechaDespacho(),
                guiaDespacho.getDescripcionCarga(),
                guiaDespacho.getPesoKg(),
                guiaDespacho.getEstado(),
                guiaDespacho.getRutaTemporalEfs(),
                guiaDespacho.getKeyS3(),
                guiaDespacho.getFechaCreacion(),
                guiaDespacho.getFechaActualizacion()
        );
    }

    private HistorialGuiaDTO convertirAHistorialDTO(GuiaDespacho guiaDespacho) {

        HistorialGuiaDTO historialDTO = new HistorialGuiaDTO();

        historialDTO.setIdGuia(guiaDespacho.getId());
        historialDTO.setNumeroPedido(guiaDespacho.getNumeroPedido());
        historialDTO.setTransportista(guiaDespacho.getTransportista());
        historialDTO.setFechaDespacho(guiaDespacho.getFechaDespacho());
        historialDTO.setEstado(guiaDespacho.getEstado());
        historialDTO.setRutaTemporalEfs(guiaDespacho.getRutaTemporalEfs());
        historialDTO.setKeyS3(guiaDespacho.getKeyS3());
        historialDTO.setNombreArchivo(guiaArchivoService.obtenerNombreArchivo(guiaDespacho.getId()));
        historialDTO.setFechaCreacion(guiaDespacho.getFechaCreacion());
        historialDTO.setFechaActualizacion(guiaDespacho.getFechaActualizacion());

        return historialDTO;
    }
}