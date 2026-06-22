package com.navaja.navajabackend.dto;

import java.util.Map;

import com.navaja.navajabackend.models.TipoEnlace;

import jakarta.validation.constraints.Size;

public record CrearEnlaceRequest(
        String nombre,
        @Size(max = 50) String codigoCorto,
        @Size(max = 2048) String urlOriginal,
        Boolean esDinamico,
        Long usuarioId,
        @Size(max = 50) String tipoHerramienta,
        @Size(max = 50) String alias,
        TipoEnlace tipo,
        Map<String, Object> metadata
) {
}

