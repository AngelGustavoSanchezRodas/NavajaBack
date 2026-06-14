package com.navaja.navajabackend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navaja.navajabackend.dto.EnlaceResponse;
import com.navaja.navajabackend.models.Enlace;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EnlaceMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EnlaceResponse toResponse(Enlace enlace) {
        Long usuarioId = enlace.getUsuario() != null ? enlace.getUsuario().getId() : null;
        return new EnlaceResponse(
                enlace.getId(),
                enlace.getCodigoCorto(),
                enlace.getUrlOriginal(),
                enlace.isEsDinamico(),
                usuarioId,
                enlace.getFechaCreacion(),
                enlace.getTipoHerramienta(),
                enlace.getFechaExpiracion(),
                enlace.getTipo(),
                enlace.getMetadata()
        );
    }

    public Map<String, Object> toMetadata(Map<String, Object> metadata) {
        return metadata == null
                ? Map.of()
                : objectMapper.convertValue(metadata, new TypeReference<Map<String, Object>>() {
                });
    }
}
