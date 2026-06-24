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

import com.navaja.navajabackend.dto.OpenGraphData;
import com.navaja.navajabackend.dto.QrGenerateRequest;
import com.navaja.navajabackend.security.UrlSecurityValidator;
import com.navaja.navajabackend.security.UsuarioPrincipal;
import com.navaja.navajabackend.services.ImageConversionService;
import com.navaja.navajabackend.services.OpenGraphService;
import com.navaja.navajabackend.services.QrCodeService;
import com.navaja.navajabackend.services.QuotaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolsController {

    private final QrCodeService qrCodeService;
    private final OpenGraphService openGraphService;
    private final QuotaService quotaService;
    private final ImageConversionService imageConversionService;
    private final UrlSecurityValidator urlSecurityValidator;

    public ToolsController(QrCodeService qrCodeService, OpenGraphService openGraphService, QuotaService quotaService, ImageConversionService imageConversionService, UrlSecurityValidator urlSecurityValidator) {
        this.qrCodeService = qrCodeService;
        this.openGraphService = openGraphService;
        this.quotaService = quotaService;
        this.imageConversionService = imageConversionService;
        this.urlSecurityValidator = urlSecurityValidator;
    }

    @GetMapping("/qr")
    public ResponseEntity<byte[]> generateQr(
            @RequestParam String url,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height
    ) {
        urlSecurityValidator.validateSafeUrl(url);
        byte[] image = qrCodeService.generateStandardQr(url, width, height);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(image);
    }

    @GetMapping("/opengraph")
    public ResponseEntity<OpenGraphData> getOpenGraph(@RequestParam String url) {
        urlSecurityValidator.validateSafeUrl(url);
        return ResponseEntity.ok(openGraphService.extract(url));
    }

    @PostMapping("/qr/generate")
    public ResponseEntity<byte[]> generatePremiumQr(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody QrGenerateRequest request
    ) {
        String usuarioId = principal == null ? null : String.valueOf(principal.getId());
        byte[] image = qrCodeService.generarQrPremium(request, usuarioId);
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
            @RequestParam(value = "watermark", required = false) org.springframework.web.multipart.MultipartFile watermarkFile
    ) {
        String usuarioId = principal == null ? null : String.valueOf(principal.getId());
        quotaService.validarConversionPremium(usuarioId, format);
        
        boolean isPremium = quotaService.validarPlanPremium(usuarioId);
        
        return imageConversionService.convert(file, format, isPremium, watermarkFile);
    }

    private MediaType resolverMediaType(String formato) {
        return switch (formato.toLowerCase()) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.valueOf("image/webp");
            case "tiff", "tif" -> MediaType.valueOf("image/tiff");
            case "bmp" -> MediaType.valueOf("image/bmp");
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private void validateHttpUri(String value) {
        urlSecurityValidator.validateSafeUrl(value);
    }
}

