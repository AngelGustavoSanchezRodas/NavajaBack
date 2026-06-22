package com.navaja.navajabackend.services;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

@Service
public class ImageConversionService {

    public ResponseEntity<byte[]> convert(MultipartFile file, String format, boolean isPremium, MultipartFile watermarkFile) {
        String normalizedFormat = validarYNormalizarFormato(format);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            var builder = Thumbnails.of(file.getInputStream()).scale(1.0);
            
            // Lógica de marcas de agua
            if (!isPremium) {
                try {
                    InputStream defaultWatermarkStream = new ClassPathResource("watermark.png").getInputStream();
                    BufferedImage watermarkImage = ImageIO.read(defaultWatermarkStream);
                    if (watermarkImage != null) builder.watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f);
                } catch (Exception ignored) {}
            } else if (watermarkFile != null && !watermarkFile.isEmpty()) {
                BufferedImage customWatermark = ImageIO.read(watermarkFile.getInputStream());
                if (customWatermark != null) builder.watermark(Positions.BOTTOM_RIGHT, customWatermark, 0.8f);
            }

            BufferedImage imagenProcesada = builder.asBufferedImage();

            // Prevención de corrupción de colores al pasar de PNG a JPG/BMP
            if (("jpg".equals(normalizedFormat) || "jpeg".equals(normalizedFormat) || "bmp".equals(normalizedFormat)) 
                    && imagenProcesada.getColorModel().hasAlpha()) {
                BufferedImage imagenSinTransparencia = new BufferedImage(imagenProcesada.getWidth(), imagenProcesada.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = imagenSinTransparencia.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, imagenSinTransparencia.getWidth(), imagenSinTransparencia.getHeight());
                g2d.drawImage(imagenProcesada, 0, 0, null);
                g2d.dispose();
                imagenProcesada = imagenSinTransparencia;
            }

            // Escritura nativa que soporta WEBP, TIFF, etc.
            ImageIO.write(imagenProcesada, normalizedFormat.toLowerCase(), os);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(resolverMediaType(normalizedFormat));
            headers.setContentDisposition(ContentDisposition.attachment().filename("converted-image." + normalizedFormat.toLowerCase()).build());
            return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new IllegalArgumentException("No fue posible convertir la imagen", e);
        }
    }

    private String validarYNormalizarFormato(String formato) {
        if (formato == null || formato.isBlank()) throw new IllegalArgumentException("El formato no puede estar vacío");
        String f = formato.toUpperCase();
        if ("SVG".equals(f)) throw new IllegalArgumentException("El formato SVG no está soportado");
        if ("JPG".equals(f) || "JPEG".equals(f) || "PNG".equals(f) || "WEBP".equals(f) || "TIFF".equals(f) || "TIF".equals(f) || "BMP".equals(f) || "GIF".equals(f)) {
            return "JPEG".equals(f) ? "jpg" : ("TIF".equals(f) ? "tiff" : f.toLowerCase());
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