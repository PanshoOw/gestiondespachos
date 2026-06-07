package com.duoc.gestiondespachos.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "GUIAS_DESPACHO")
public class GuiaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número de pedido es obligatorio")
    @Column(name = "NUMERO_PEDIDO", nullable = false, length = 50)
    private String numeroPedido;

    @NotBlank(message = "El transportista es obligatorio")
    @Column(name = "TRANSPORTISTA", nullable = false, length = 100)
    private String transportista;

    @NotBlank(message = "El destinatario es obligatorio")
    @Column(name = "DESTINATARIO", nullable = false, length = 120)
    private String destinatario;

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Column(name = "DIRECCION_DESTINO", nullable = false, length = 200)
    private String direccionDestino;

    @NotBlank(message = "La ciudad de destino es obligatoria")
    @Column(name = "CIUDAD_DESTINO", nullable = false, length = 100)
    private String ciudadDestino;

    @NotNull(message = "La fecha de despacho es obligatoria")
    @Column(name = "FECHA_DESPACHO", nullable = false)
    private LocalDate fechaDespacho;

    @NotBlank(message = "La descripción de la carga es obligatoria")
    @Column(name = "DESCRIPCION_CARGA", nullable = false, length = 300)
    private String descripcionCarga;

    @NotNull(message = "El peso de la carga es obligatorio")
    @DecimalMin(value = "0.1", message = "El peso debe ser mayor a 0")
    @Column(name = "PESO_KG", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 30)
    private EstadoGuia estado;

    @Column(name = "RUTA_TEMPORAL_EFS", length = 500)
    private String rutaTemporalEfs;

    @Column(name = "KEY_S3", length = 500)
    private String keyS3;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ACTUALIZACION")
    private LocalDateTime fechaActualizacion;

    public GuiaDespacho() {
        // Constructor requerido por JPA para instanciar la entidad mediante reflexión.
    }

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();

        if (this.estado == null) {
            this.estado = EstadoGuia.GENERADA;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
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