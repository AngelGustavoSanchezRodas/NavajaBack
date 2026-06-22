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
            // 1. Usamos Thumbnailator solo para el procesamiento (no para la salida)
            var builder = Thumbnails.of(file.getInputStream()).scale(1.0);
            
            if (!isPremium) {
                try {
                    InputStream defaultWatermarkStream = new ClassPathResource("watermark.png").getInputStream();
                    BufferedImage watermarkImage = ImageIO.read(defaultWatermarkStream);
                    if (watermarkImage != null) {
                        builder.watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f);
                    }
                } catch (Exception e) {
                    // Ignorar si no existe el archivo watermark.png
                }
            } else if (watermarkFile != null && !watermarkFile.isEmpty()) {
                BufferedImage customWatermark = ImageIO.read(watermarkFile.getInputStream());
                if (customWatermark != null) {
                    builder.watermark(Positions.BOTTOM_RIGHT, customWatermark, 0.8f);
                }
            }

            // 2. Extraemos la imagen procesada a memoria
            BufferedImage imagenProcesada = builder.asBufferedImage();

            // 3. REGLA ARQUITECTÓNICA: Prevención de corrupción de color (Pink Background Bug)
            // Si la imagen original tenía transparencia (PNG) y la pasamos a JPG/BMP, los colores colapsan.
            if (("jpg".equals(normalizedFormat) || "jpeg".equals(normalizedFormat) || "bmp".equals(normalizedFormat)) 
                    && imagenProcesada.getColorModel().hasAlpha()) {
                
                BufferedImage imagenSinTransparencia = new BufferedImage(
                        imagenProcesada.getWidth(), 
                        imagenProcesada.getHeight(), 
                        BufferedImage.TYPE_INT_RGB
                );
                
                Graphics2D g2d = imagenSinTransparencia.createGraphics();
                g2d.setColor(Color.WHITE); // Rellenar transparencia con blanco
                g2d.fillRect(0, 0, imagenSinTransparencia.getWidth(), imagenSinTransparencia.getHeight());
                g2d.drawImage(imagenProcesada, 0, 0, null);
                g2d.dispose();
                
                imagenProcesada = imagenSinTransparencia;
            }

            // 4. Bypass de Thumbnailator: Forzamos la escritura nativa de Java ImageIO 
            // Esto garantiza que los plugins de TwelveMonkeys (WEBP/TIFF) se ejecuten correctamente.
            boolean formatoSoportado = ImageIO.write(imagenProcesada, normalizedFormat.toLowerCase(), os);
            
            if (!formatoSoportado) {
                throw new IllegalArgumentException("El servidor no tiene un codificador configurado para: " + normalizedFormat);
            }

            MediaType mediaType = resolverMediaType(normalizedFormat);
            String nombreArchivo = "converted-image." + normalizedFormat.toLowerCase();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDisposition(ContentDisposition.attachment().filename(nombreArchivo).build());
            return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
            
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible procesar y convertir la imagen", exception);
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