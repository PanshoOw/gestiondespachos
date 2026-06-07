package com.duoc.gestiondespachos.dto;

import com.duoc.gestiondespachos.entity.EstadoGuia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GuiaDespachoResponseDTO {

    private Long id;
    private String numeroPedido;
    private String transportista;
    private String destinatario;
    private String direccionDestino;
    private String ciudadDestino;
    private LocalDate fechaDespacho;
    private String descripcionCarga;
    private BigDecimal pesoKg;
    private EstadoGuia estado;
    private String rutaTemporalEfs;
    private String keyS3;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public GuiaDespachoResponseDTO() {
        // Constructor requerido para serialización y deserialización.
    }

    public GuiaDespachoResponseDTO(Long id,
                                   String numeroPedido,
                                   String transportista,
                                   String destinatario,
                                   String direccionDestino,
                                   String ciudadDestino,
                                   LocalDate fechaDespacho,
                                   String descripcionCarga,
                                   BigDecimal pesoKg,
                                   EstadoGuia estado,
                                   String rutaTemporalEfs,
                                   String keyS3,
                                   LocalDateTime fechaCreacion,
                                   LocalDateTime fechaActualizacion) {
        this.id = id;
        this.numeroPedido = numeroPedido;
        this.transportista = transportista;
        this.destinatario = destinatario;
        this.direccionDestino = direccionDestino;
        this.ciudadDestino = ciudadDestino;
        this.fechaDespacho = fechaDespacho;
        this.descripcionCarga = descripcionCarga;
        this.pesoKg = pesoKg;
        this.estado = estado;
        this.rutaTemporalEfs = rutaTemporalEfs;
        this.keyS3 = keyS3;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getId() {
        return id;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public String getTransportista() {
        return transportista;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public String getCiudadDestino() {
        return ciudadDestino;
    }

    public LocalDate getFechaDespacho() {
        return fechaDespacho;
    }

    public String getDescripcionCarga() {
        return descripcionCarga;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public EstadoGuia getEstado() {
        return estado;
    }

    public String getRutaTemporalEfs() {
        return rutaTemporalEfs;
    }

    public String getKeyS3() {
        return keyS3;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public void setDireccionDestino(String direccionDestino) {
        this.direccionDestino = direccionDestino;
    }

    public void setCiudadDestino(String ciudadDestino) {
        this.ciudadDestino = ciudadDestino;
    }

    public void setFechaDespacho(LocalDate fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }

    public void setDescripcionCarga(String descripcionCarga) {
        this.descripcionCarga = descripcionCarga;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public void setEstado(EstadoGuia estado) {
        this.estado = estado;
    }

    public void setRutaTemporalEfs(String rutaTemporalEfs) {
        this.rutaTemporalEfs = rutaTemporalEfs;
    }

    public void setKeyS3(String keyS3) {
        this.keyS3 = keyS3;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}