package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.dto.ActualizarEnlaceRequest;
import com.navaja.navajabackend.dto.EnlaceResponse;
import com.navaja.navajabackend.models.TipoEnlace;
import com.navaja.navajabackend.security.UsuarioPrincipal;
import com.navaja.navajabackend.services.EnlaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management/qrs")
@SecurityRequirement(name = "bearerAuth")
public class ManagementQrController {

    private final EnlaceService enlaceService;

    public ManagementQrController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<EnlaceResponse>> listarQrs(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(enlaceService.listarEnlacesPorTipo(principal.getId(), TipoEnlace.QR));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnlaceResponse> actualizarQr(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEnlaceRequest request,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(enlaceService.actualizarEnlace(id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarQr(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        enlaceService.eliminarEnlacePropietario(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
