package com.duoc.gestiondespachos.dto;

public record ConsumoColaResponseDTO(
        int mensajesRecibidos,
        int mensajesGuardados,
        int mensajesEnviadosAErrores,
        String detalle
) {
}