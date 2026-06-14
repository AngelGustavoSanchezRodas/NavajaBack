package com.navaja.navajabackend.services;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageConversionService {

    public ResponseEntity<byte[]> convert(MultipartFile file, String format) {
        String normalizedFormat = validarYNormalizarFormato(format);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Thumbnails.of(file.getInputStream())
                    .scale(1.0)
                    .outputFormat(normalizedFormat.toLowerCase())
                    .toOutputStream(os);

            MediaType mediaType = resolverMediaType(normalizedFormat);
            String nombreArchivo = "converted-image." + normalizedFormat.toLowerCase();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDisposition(ContentDisposition.attachment().filename(nombreArchivo).build());
            return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible convertir la imagen", exception);
        }
    }

    private String validarYNormalizarFormato(String formato) {
        if (formato == null || formato.isBlank()) {
            throw new IllegalArgumentException("El formato no puede estar vacío");
        }

        String formatoUpper = formato.toUpperCase();

        if ("SVG".equals(formatoUpper)) {
            throw new IllegalArgumentException("El formato SVG no está soportado");
        }

        if ("JPG".equals(formatoUpper) || "JPEG".equals(formatoUpper) || "PNG".equals(formatoUpper) ||
            "WEBP".equals(formatoUpper) || "TIFF".equals(formatoUpper) || "TIF".equals(formatoUpper) ||
            "BMP".equals(formatoUpper) || "GIF".equals(formatoUpper)) {
            return "JPG".equals(formatoUpper) ? "jpg" : ("JPEG".equals(formatoUpper) ? "jpg" : ("TIF".equals(formatoUpper) ? "tiff" : formatoUpper.toLowerCase()));
        }

        throw new IllegalArgumentException("El formato " + formato + " no está soportado");
    }

    private MediaType resolverMediaType(String formato) {
        return switch (formato.toLowerCase()) {
            case "jpg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.valueOf("image/webp");
            case "tiff" -> MediaType.valueOf("image/tiff");
            case "bmp" -> MediaType.valueOf("image/bmp");
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
