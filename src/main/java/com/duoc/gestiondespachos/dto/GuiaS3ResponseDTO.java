package com.duoc.gestiondespachos.dto;

import java.time.LocalDate;

import com.duoc.gestiondespachos.entity.EstadoGuia;

public class GuiaS3ResponseDTO {

    private Long idGuia;
    private String numeroPedido;
    private String transportista;
    private LocalDate fechaDespacho;
    private String bucket;
    private String keyS3;
    private String nombreArchivo;
    private EstadoGuia estado;
    private String mensaje;

    public GuiaS3ResponseDTO() {
        // Constructor requerido para serialización.
    }

    public GuiaS3ResponseDTO(Long idGuia,
                             String numeroPedido,
                             String transportista,
                             LocalDate fechaDespacho,
                             String bucket,
                             String keyS3,
                             String nombreArchivo,
                             EstadoGuia estado,
                             String mensaje) {
        this.idGuia = idGuia;
        this.numeroPedido = numeroPedido;
        this.transportista = transportista;
        this.fechaDespacho = fechaDespacho;
        this.bucket = bucket;
        this.keyS3 = keyS3;
        this.nombreArchivo = nombreArchivo;
        this.estado = estado;
        this.mensaje = mensaje;
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

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
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

    public EstadoGuia getEstado() {
        return estado;
    }

    public void setEstado(EstadoGuia estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}