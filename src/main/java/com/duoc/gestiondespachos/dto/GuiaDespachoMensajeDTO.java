package com.duoc.gestiondespachos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GuiaDespachoMensajeDTO(
        Long id,
        String numeroPedido,
        String transportista,
        String destinatario,
        String direccionDestino,
        String ciudadDestino,
        LocalDate fechaDespacho,
        String descripcionCarga,
        BigDecimal pesoKg,
        String estado,
        LocalDateTime fechaCreacion
) {
}