package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.services.ClicAsyncService;
import com.navaja.navajabackend.services.EnlaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final EnlaceService enlaceService;
    private final ClicAsyncService clicAsyncService;

    public RedirectController(EnlaceService enlaceService, ClicAsyncService clicAsyncService) {
        this.enlaceService = enlaceService;
        this.clicAsyncService = clicAsyncService;
    }

    @GetMapping("/api/core/links/public/{alias}")
    public ResponseEntity<Void> redirigirPublic(
            @PathVariable("alias") String alias,
            HttpServletRequest request
    ) {
        String redirectUrl = enlaceService.obtenerUrlOriginalPorAlias(alias);
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        clicAsyncService.registrarClicAsync(alias, ip, userAgent);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}
