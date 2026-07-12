package com.duoc.gestiondespachos.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "GUIAS_PROCESADAS_COLA")
public class GuiaProcesadaCola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ID_GUIA_ORIGINAL", nullable = false)
    private Long idGuiaOriginal;

    @Column(name = "NUMERO_PEDIDO", nullable = false, length = 50)
    private String numeroPedido;

    @Column(name = "TRANSPORTISTA", nullable = false, length = 100)
    private String transportista;

    @Column(name = "FECHA_DESPACHO", nullable = false)
    private LocalDate fechaDespacho;

    @Column(name = "PAYLOAD_JSON", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "FECHA_PROCESAMIENTO", nullable = false)
    private LocalDateTime fechaProcesamiento;

    public GuiaProcesadaCola() {
        // Constructor requerido por JPA.
    }

    @PrePersist
    public void prePersist() {
        this.fechaProcesamiento = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getIdGuiaOriginal() {
        return idGuiaOriginal;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public String getTransportista() {
        return transportista;
    }

    public LocalDate getFechaDespacho() {
        return fechaDespacho;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIdGuiaOriginal(Long idGuiaOriginal) {
        this.idGuiaOriginal = idGuiaOriginal;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public void setFechaDespacho(LocalDate fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }
}