package com.navaja.navajabackend.services;

import com.navaja.navajabackend.dto.IdentityRequest;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class IdentityPdfService {

    private final TemplateEngine templateEngine;

    public IdentityPdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateIdentityPdf(IdentityRequest request) throws IOException {
        Context context = new Context();
        context.setVariable("identity", request);

        String htmlContent = templateEngine.process("cv-template", context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "classpath:/templates/");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
