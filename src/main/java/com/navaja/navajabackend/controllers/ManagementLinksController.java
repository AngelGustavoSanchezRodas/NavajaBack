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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import com.navaja.navajabackend.dto.ActualizarEnlaceRequest;
import com.navaja.navajabackend.models.TipoEnlace;

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
        List<EnlaceResponse> enlaces = enlaceService.listarTodosLosEnlacesDelUsuario(principal.getId());
        return ResponseEntity.ok(enlaces);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnlaceResponse> actualizarEnlace(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEnlaceRequest request,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(enlaceService.actualizarEnlace(id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEnlace(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        enlaceService.eliminarEnlacePropietario(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
