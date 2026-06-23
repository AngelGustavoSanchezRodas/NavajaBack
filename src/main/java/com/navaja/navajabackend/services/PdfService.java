package com.navaja.navajabackend.services;

import com.navaja.navajabackend.dto.SignatureMetadata; // O un nuevo IdentityDto
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarCvPdf(Map<String, Object> datosUsuario) {
        // 1. Inyectar datos en la plantilla HTML
        Context context = new Context();
        context.setVariables(datosUsuario);
        
        // Asume que tienes un archivo cv-template.html en resources/templates/
        String htmlRenderizado = templateEngine.process("cv-template", context);

        // 2. Convertir HTML a PDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlRenderizado, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de Identidad", e);
        }
    }
}