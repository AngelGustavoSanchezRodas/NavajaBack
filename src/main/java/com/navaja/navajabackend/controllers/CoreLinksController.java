package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.dto.CrearEnlaceRequest;
import com.navaja.navajabackend.dto.EnlaceResponse;
import com.navaja.navajabackend.services.EnlaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core/links")
public class CoreLinksController {

    private final EnlaceService enlaceService;

    public CoreLinksController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    @PostMapping("/create")
    public ResponseEntity<EnlaceResponse> crearEnlace(@Valid @RequestBody CrearEnlaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enlaceService.crearEnlace(request));
    }
}
