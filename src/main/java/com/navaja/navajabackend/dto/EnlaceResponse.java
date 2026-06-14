package com.navaja.navajabackend.dto;

import com.navaja.navajabackend.models.TipoEnlace;
import java.time.OffsetDateTime;
import java.util.Map;

public record EnlaceResponse(
        Long id,
        String codigoCorto,
        String urlOriginal,
        boolean esDinamico,
        Long usuarioId,
        OffsetDateTime fechaCreacion,
        String tipoHerramienta,
        OffsetDateTime fechaExpiracion,
        TipoEnlace tipo,
        Map<String, Object> metadata
) {
}


