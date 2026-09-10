package com.navaja.navajabackend.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import com.navaja.navajabackend.dto.QrGenerateRequest;
import com.navaja.navajabackend.security.UrlSecurityValidator;
import com.navaja.navajabackend.security.UsuarioPrincipal;
import com.navaja.navajabackend.services.ImageConversionService;
import com.navaja.navajabackend.services.QrCodeService;
import com.navaja.navajabackend.services.QuotaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolsController {

    private final QrCodeService qrCodeService;
    private final QuotaService quotaService;
    private final ImageConversionService imageConversionService;
    private final UrlSecurityValidator urlSecurityValidator;

    public ToolsController(QrCodeService qrCodeService, QuotaService quotaService, ImageConversionService imageConversionService, UrlSecurityValidator urlSecurityValidator) {
        this.qrCodeService = qrCodeService;
        this.quotaService = quotaService;
        this.imageConversionService = imageConversionService;
        this.urlSecurityValidator = urlSecurityValidator;
    }

    @GetMapping("/qr")
    public ResponseEntity<byte[]> generateQr(
            @RequestParam String url,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height,
            HttpServletRequest httpRequest
    ) {
        urlSecurityValidator.validateSafeUrl(url);
        quotaService.validarCreacionQr(null, httpRequest.getRemoteAddr());
        byte[] image = qrCodeService.generateStandardQr(url, width, height);
        quotaService.registrarCreacionQr(null, httpRequest.getRemoteAddr());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(image);
    }

    @PostMapping("/qr/generate")
    public ResponseEntity<byte[]> generatePremiumQr(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody QrGenerateRequest request,
            HttpServletRequest httpRequest
    ) {
        String usuarioId = principal == null ? null : String.valueOf(principal.getId());
        String identificadorCliente = principal == null ? httpRequest.getRemoteAddr() : usuarioId;
        quotaService.validarCreacionQr(usuarioId, identificadorCliente);
        byte[] image = qrCodeService.generarQrPremium(request, usuarioId);
        quotaService.registrarCreacionQr(usuarioId, identificadorCliente);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(image);
    }

    @PostMapping(value = "/convert-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertImage(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("format") String format,
            @RequestParam(value = "watermark", required = false) org.springframework.web.multipart.MultipartFile watermarkFile,
            HttpServletRequest httpRequest
    ) {
        String usuarioId = principal == null ? null : String.valueOf(principal.getId());
        String identificadorCliente = principal == null ? httpRequest.getRemoteAddr() : usuarioId;
        quotaService.validarConversionPremium(usuarioId, format);
        quotaService.validarUsoConversionImagen(usuarioId, identificadorCliente);
        
        boolean isPremium = quotaService.validarPlanPremium(usuarioId);
        
        ResponseEntity<byte[]> response = imageConversionService.convert(file, format, isPremium, watermarkFile);
        quotaService.registrarUsoConversionImagen(usuarioId, identificadorCliente);
        return response;
    }
}
