package com.duoc.gestiondespachos.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.gestiondespachos.dto.GuiaDespachoRequestDTO;
import com.duoc.gestiondespachos.dto.GuiaDespachoResponseDTO;
import com.duoc.gestiondespachos.dto.GuiaDespachoUpdateDTO;
import com.duoc.gestiondespachos.dto.GuiaS3ResponseDTO;
import com.duoc.gestiondespachos.dto.HistorialGuiaDTO;
import com.duoc.gestiondespachos.service.GuiaDespachoService;
import com.duoc.gestiondespachos.service.GuiaS3Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/guias")
public class GuiaDespachoController {

    private final GuiaDespachoService guiaDespachoService;
    private final GuiaS3Service guiaS3Service;

    public GuiaDespachoController(GuiaDespachoService guiaDespachoService,
                                  GuiaS3Service guiaS3Service) {
        this.guiaDespachoService = guiaDespachoService;
        this.guiaS3Service = guiaS3Service;
    }

    @PostMapping
    public ResponseEntity<GuiaDespachoResponseDTO> crearGuia(
            @Valid @RequestBody GuiaDespachoRequestDTO requestDTO) {

        GuiaDespachoResponseDTO respuesta = guiaDespachoService.crearGuia(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping({"", "/historial"})
    public ResponseEntity<List<HistorialGuiaDTO>> consultarGuias(
            @RequestParam(required = false) String transportista,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<HistorialGuiaDTO> historial = guiaDespachoService.consultarHistorial(
                transportista,
                fecha
        );

        return ResponseEntity.ok(historial);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuiaDespachoResponseDTO> obtenerGuiaPorId(@PathVariable Long id) {

        GuiaDespachoResponseDTO respuesta = guiaDespachoService.obtenerGuiaPorId(id);

        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/{id}/s3")
    public ResponseEntity<GuiaS3ResponseDTO> subirGuiaAS3(@PathVariable Long id) {

        GuiaS3ResponseDTO respuesta = guiaS3Service.subirGuiaAS3(id);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping({"/{id}/s3", "/{id}/descargar"})
    public ResponseEntity<byte[]> descargarGuiaDesdeS3(
            @PathVariable Long id,
            @RequestParam String transportista) {

        byte[] archivo = guiaS3Service.descargarGuiaDesdeS3(id, transportista);

        String nombreArchivo = guiaS3Service.obtenerNombreArchivo(id);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(nombreArchivo)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(archivo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuiaDespachoResponseDTO> modificarGuia(
            @PathVariable Long id,
            @Valid @RequestBody GuiaDespachoUpdateDTO updateDTO) {

        GuiaDespachoResponseDTO respuesta = guiaDespachoService.modificarGuia(id, updateDTO);

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}/s3")
    public ResponseEntity<GuiaS3ResponseDTO> reemplazarGuiaEnS3(@PathVariable Long id) {

        GuiaS3ResponseDTO respuesta = guiaS3Service.reemplazarGuiaEnS3(id);

        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping({"/{id}", "/{id}/s3"})
    public ResponseEntity<GuiaS3ResponseDTO> eliminarGuia(@PathVariable Long id) {

        GuiaS3ResponseDTO respuesta = guiaS3Service.eliminarGuiaDesdeS3(id);

        return ResponseEntity.ok(respuesta);
    }
}