package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.services.*;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
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

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolsController {

    private final QrCodeService qrCodeService;
    private final OpenGraphService openGraphService;
    private final QuotaService quotaService;
    private final ImageConversionService imageConversionService;
    private final UrlSecurityValidator urlSecurityValidator;
    private final PdfService pdfService;

    public ToolsController(QrCodeService qrCodeService, OpenGraphService openGraphService, QuotaService quotaService, ImageConversionService imageConversionService, UrlSecurityValidator urlSecurityValidator, PdfService pdfService) {
        this.qrCodeService = qrCodeService;
        this.openGraphService = openGraphService;
        this.quotaService = quotaService;
        this.imageConversionService = imageConversionService;
        this.urlSecurityValidator = urlSecurityValidator;
        this.pdfService = pdfService;
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

    @PostMapping("/identity/pdf")
    public ResponseEntity<byte[]> descargarPdfIdentidad(@RequestBody Map<String, Object> requestData) {
        // Generar PDF
        byte[] pdfBytes = pdfService.generarCvPdf(requestData);

        // Configurar los headers para forzar la descarga en el navegador
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "CV_" + requestData.getOrDefault("nombre", "NavajaGT").toString().replaceAll("\\s+", "_") + ".pdf";
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    private void validateHttpUri(String value) {
        urlSecurityValidator.validateSafeUrl(value);
    }
}

