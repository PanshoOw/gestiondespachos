package com.duoc.gestiondespachos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.duoc.gestiondespachos.entity.EstadoGuia;

public class HistorialGuiaDTO {

    private Long idGuia;
    private String numeroPedido;
    private String transportista;
    private LocalDate fechaDespacho;
    private EstadoGuia estado;
    private String rutaTemporalEfs;
    private String keyS3;
    private String nombreArchivo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public HistorialGuiaDTO() {
        // Constructor requerido para serialización.
    }

    public Long getIdGuia() {
        return idGuia;
    }

    public void setIdGuia(Long idGuia) {
        this.idGuia = idGuia;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getTransportista() {
        return transportista;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public LocalDate getFechaDespacho() {
        return fechaDespacho;
    }

    public void setFechaDespacho(LocalDate fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }

    public EstadoGuia getEstado() {
        return estado;
    }

    public void setEstado(EstadoGuia estado) {
        this.estado = estado;
    }

    public String getRutaTemporalEfs() {
        return rutaTemporalEfs;
    }

    public void setRutaTemporalEfs(String rutaTemporalEfs) {
        this.rutaTemporalEfs = rutaTemporalEfs;
    }

    public String getKeyS3() {
        return keyS3;
    }

    public void setKeyS3(String keyS3) {
        this.keyS3 = keyS3;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}