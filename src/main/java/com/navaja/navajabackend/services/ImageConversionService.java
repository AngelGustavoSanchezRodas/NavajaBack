package com.navaja.navajabackend.services;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.coobird.thumbnailator.geometry.Positions;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.awt.Color;
import java.awt.Graphics2D;

@Service
public class ImageConversionService {

    public ResponseEntity<byte[]> convert(MultipartFile file, String format, boolean isPremium, MultipartFile watermarkFile) {
        String normalizedFormat = validarYNormalizarFormato(format);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // 1. Leer la imagen principal a memoria para obtener sus dimensiones exactas
            BufferedImage mainImage = ImageIO.read(file.getInputStream());
            if (mainImage == null) {
                throw new IllegalArgumentException("El archivo principal no es una imagen válida.");
            }

            var builder = Thumbnails.of(mainImage).scale(1.0); // Mantenemos la escala de la imagen original

            // 2. Calcular el tamaño relativo de la marca de agua (20% del ancho de la imagen principal)
            // Aseguramos un mínimo de 50px de ancho para que no desaparezca en imágenes muy pequeñas
            int watermarkTargetWidth = Math.max((int) (mainImage.getWidth() * 0.20), 50);

            if (!isPremium) {
                // Lógica Gratis: Marca de agua por defecto ajustada dinámicamente
                try {
                    InputStream defaultWatermarkStream = new ClassPathResource("watermark.png").getInputStream();
                    BufferedImage watermarkImage = ImageIO.read(defaultWatermarkStream);
                    if (watermarkImage != null) {
                        BufferedImage resizedWatermark = Thumbnails.of(watermarkImage)
                                .width(watermarkTargetWidth)
                                .keepAspectRatio(true)
                                .asBufferedImage();
                        builder.watermark(Positions.BOTTOM_RIGHT, resizedWatermark, 0.5f);
                    }
                } catch (Exception e) {
                    // Ignorar si no existe el archivo watermark.png local
                }
            } else if (watermarkFile != null && !watermarkFile.isEmpty()) {
                // Lógica PRO: Marca de agua del usuario ajustada dinámicamente
                BufferedImage customWatermark = ImageIO.read(watermarkFile.getInputStream());
                if (customWatermark != null) {
                    BufferedImage resizedCustomWatermark = Thumbnails.of(customWatermark)
                            .width(watermarkTargetWidth)
                            .keepAspectRatio(true)
                            .asBufferedImage();
                    // Usamos 0.8f de opacidad según tu regla anterior
                    builder.watermark(Positions.BOTTOM_RIGHT, resizedCustomWatermark, 0.8f);
                }
            }

            // 3. Renderizar salida
            builder.outputFormat(normalizedFormat.toLowerCase()).toOutputStream(os);

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

    private BufferedImage corregirTransparencia(BufferedImage imagen, String formato) {
        if (("jpg".equals(formato) || "jpeg".equals(formato) || "bmp".equals(formato)) && imagen.getColorModel().hasAlpha()) {
            BufferedImage imagenSinTransparencia = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = imagenSinTransparencia.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, imagenSinTransparencia.getWidth(), imagenSinTransparencia.getHeight());
            g2d.drawImage(imagen, 0, 0, null);
            g2d.dispose();
            return imagenSinTransparencia;
        }
        return imagen;
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
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.valueOf("image/webp");
            case "tiff", "tif" -> MediaType.valueOf("image/tiff");
            case "bmp" -> MediaType.valueOf("image/bmp");
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}