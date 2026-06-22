package com.navaja.navajabackend.services;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

@Service
public class ImageConversionService {

    public record ConversionResult(byte[] data, String extension) {}

    public ConversionResult convert(MultipartFile file, String format, boolean isPremium, MultipartFile watermarkFile) {
        String normalizedFormat = validarYNormalizarFormato(format);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            var builder = Thumbnails.of(file.getInputStream()).scale(1.0);
            
            aplicarMarcaDeAgua(builder, isPremium, watermarkFile);

            BufferedImage imagenProcesada = builder.asBufferedImage();
            imagenProcesada = corregirTransparencia(imagenProcesada, normalizedFormat);

            ImageIO.scanForPlugins();
            boolean formatoSoportado = ImageIO.write(imagenProcesada, normalizedFormat.toLowerCase(), os);

            if (!formatoSoportado) {
                throw new IllegalArgumentException("El motor de conversión no tiene instalado el codificador para exportar a: " + normalizedFormat);
            }

            return new ConversionResult(os.toByteArray(), normalizedFormat.toLowerCase());
        } catch (IOException e) {
            throw new IllegalArgumentException("No fue posible convertir la imagen", e);
        }
    }

    private void aplicarMarcaDeAgua(Thumbnails.Builder<? extends InputStream> builder,
                                    boolean isPremium, MultipartFile watermarkFile) throws IOException {
        if (!isPremium) {
            try {
                InputStream defaultWatermarkStream = new ClassPathResource("watermark.png").getInputStream();
                BufferedImage watermarkImage = ImageIO.read(defaultWatermarkStream);
                if (watermarkImage != null) builder.watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f);
            } catch (Exception ignored) {
                // Se ignora si no se encuentra la marca de agua por defecto
            }
        } else if (watermarkFile != null && !watermarkFile.isEmpty()) {
            BufferedImage customWatermark = ImageIO.read(watermarkFile.getInputStream());
            if (customWatermark != null) builder.watermark(Positions.BOTTOM_RIGHT, customWatermark, 0.8f);
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
}