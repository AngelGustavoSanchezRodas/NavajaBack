package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.dto.IdentityRequest;
import com.navaja.navajabackend.services.IdentityPdfService;
import com.navaja.navajabackend.services.IdentityVCardService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/tools/identity")
public class IdentityController {

    private final IdentityPdfService identityPdfService;
    private final IdentityVCardService identityVCardService;

    public IdentityController(IdentityPdfService identityPdfService, IdentityVCardService identityVCardService) {
        this.identityPdfService = identityPdfService;
        this.identityVCardService = identityVCardService;
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody IdentityRequest request) {
        try {
            byte[] pdfBytes = identityPdfService.generateIdentityPdf(request);
            
            String filename = request.getNombre() != null ? request.getNombre().replaceAll("\\s+", "_") : "CV";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "CV_" + filename + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/vcard")
    public ResponseEntity<byte[]> generateVCard(@RequestBody IdentityRequest request) {
        byte[] vcardBytes = identityVCardService.generateIdentityVCard(request);
        
        String filename = request.getNombre() != null ? request.getNombre().replaceAll("\\s+", "_") : "Contact";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/vcard"));
        headers.setContentDispositionFormData("attachment", filename + ".vcf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(vcardBytes);
    }
}
