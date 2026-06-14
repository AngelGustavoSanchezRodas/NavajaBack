package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.dto.EnlaceResponse;
import com.navaja.navajabackend.models.Enlace;
import com.navaja.navajabackend.services.EnlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/enlaces")
public class PublicLinkController {

    private final EnlaceService enlaceService;

    public PublicLinkController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    @GetMapping("/{alias}")
    public ResponseEntity<EnlaceResponse> getPublicLink(@PathVariable String alias) {
        Enlace enlace = enlaceService.obtenerEnlacePorCodigoCorto(alias);
        return ResponseEntity.ok(toResponse(enlace));
    }

    private EnlaceResponse toResponse(Enlace enlace) {
        return new EnlaceResponse(
                enlace.getId(),
                enlace.getCodigoCorto(),
                enlace.getUrlOriginal(),
                enlace.isEsDinamico(),
                null,
                enlace.getFechaCreacion(),
                enlace.getTipoHerramienta(),
                enlace.getFechaExpiracion(),
                enlace.getTipo(),
                enlace.getMetadata()
        );
    }
}
