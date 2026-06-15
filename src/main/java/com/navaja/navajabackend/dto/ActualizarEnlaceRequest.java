package com.navaja.navajabackend.dto;

import com.navaja.navajabackend.models.TipoEnlace;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record ActualizarEnlaceRequest(
        @Size(max = 50) String alias,
        @Size(max = 2048) String urlOriginal,
        Boolean esDinamico,
        @Size(max = 50) String tipoHerramienta,
        TipoEnlace tipo,
        Map<String, Object> metadata
) {
}
