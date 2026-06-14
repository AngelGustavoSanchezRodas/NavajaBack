package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.dto.EnlaceResponse;
import com.navaja.navajabackend.security.UsuarioPrincipal;
import com.navaja.navajabackend.services.EnlaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/management/links")
@SecurityRequirement(name = "bearerAuth")
public class ManagementLinksController {

    private final EnlaceService enlaceService;

    public ManagementLinksController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<EnlaceResponse>> listarEnlaces(@AuthenticationPrincipal UsuarioPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(enlaceService.listarEnlaces(principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEnlace(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        enlaceService.eliminarEnlacePropietario(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
