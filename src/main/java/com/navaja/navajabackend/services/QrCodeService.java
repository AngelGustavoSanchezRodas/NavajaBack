package com.navaja.navajabackend.services;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.navaja.navajabackend.dto.QrGenerateRequest;
import com.navaja.navajabackend.exceptions.AccesoDenegadoException;

@Service
public class QrCodeService {

    private static final int QR_SIZE = 512;
    private static final String COLOR_FRENTE_ESTANDAR = "#000000";
    private static final String COLOR_FONDO_ESTANDAR = "#FFFFFF";
    
    // Límite de seguridad: ~1.4 millones de caracteres en Base64 equivale aprox a 1MB físico.
    private static final int MAX_BASE64_LENGTH = 1_400_000;

    private final QuotaService quotaService;

    public QrCodeService(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    public byte[] generateStandardQr(String data, int width, int height) {
        return procesarImagenQr(data, COLOR_FRENTE_ESTANDAR, COLOR_FONDO_ESTANDAR, normalizarDimension(width), normalizarDimension(height), null);
    }

    public byte[] generarQrPremium(QrGenerateRequest request, String usuarioId) {
        String colorFrente = colorNormalizado(request.colorFrente(), COLOR_FRENTE_ESTANDAR);
        String colorFondo = colorNormalizado(request.colorFondo(), COLOR_FONDO_ESTANDAR);
        String logoBase64 = request.logoBase64();

        // 1. REGLA DE SEGURIDAD (Fail-Fast): Falla inmediatamente si el logo supera 1MB
        if (StringUtils.hasText(logoBase64) && logoBase64.length() > MAX_BASE64_LENGTH) {
            throw new IllegalArgumentException("El logo excede el tamaño máximo permitido de 1MB.");
        }

        boolean requierePremium = !COLOR_FRENTE_ESTANDAR.equalsIgnoreCase(colorFrente)
                || !COLOR_FONDO_ESTANDAR.equalsIgnoreCase(colorFondo)
                || StringUtils.hasText(logoBase64);

        if (requierePremium && !quotaService.validarPlanPremium(usuarioId)) {
            throw new AccesoDenegadoException("La personalización visual del QR (colores y logo) requiere plan PRO");
        }

        String contenidoFinal = parsearPayload(request.tipo(), request.payload());

        // 2. Procesamos la imagen y le pasamos el logo
        return procesarImagenQr(contenidoFinal, colorFrente, colorFondo, QR_SIZE, QR_SIZE, logoBase64);
    }

    private byte[] procesarImagenQr(String data, String colorFrenteHex, String colorFondoHex, int width, int height, String logoBase64) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            // IMPORTANTE: El nivel 'H' permite tapar hasta un 30% del QR sin dañarlo.
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            MatrixToImageConfig imageConfig = new MatrixToImageConfig(
                    Color.decode(colorFrenteHex).getRGB(),
                    Color.decode(colorFondoHex).getRGB()
            );

            // Generamos un BufferedImage en lugar de enviarlo directo a un stream
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix, imageConfig);

            // Si hay un logo, orquestamos la superposición
            if (StringUtils.hasText(logoBase64)) {
                qrImage = superponerLogo(qrImage, logoBase64);
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(qrImage, "png", outputStream);
                return outputStream.toByteArray();
            }
        } catch (WriterException | IOException exception) {
            throw new IllegalArgumentException("No se pudo generar el codigo QR con los parametros enviados", exception);
        }
    }

    private BufferedImage superponerLogo(BufferedImage qrImage, String base64Logo) throws IOException {
        // Limpiamos el prefijo (ej. "data:image/png;base64,") si el frontend lo envió
        String cleanBase64 = base64Logo;
        if (base64Logo.contains(",")) {
            cleanBase64 = base64Logo.substring(base64Logo.indexOf(",") + 1);
        }

        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
        BufferedImage logo = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (logo == null) {
            throw new IllegalArgumentException("El formato del logo Base64 no es válido o está corrupto.");
        }

        // El logo no debe ocupar más del 22% del QR para que las cámaras puedan leerlo
        int maxWidth = (int) Math.round(qrImage.getWidth() * 0.22);
        int maxHeight = (int) Math.round(qrImage.getHeight() * 0.22);

        int finalLogoWidth = logo.getWidth();
        int finalLogoHeight = logo.getHeight();

        // Escalar manteniendo la proporción si el logo es más grande que el área central permitida
        if (logo.getWidth() > maxWidth || logo.getHeight() > maxHeight) {
            float scale = Math.min((float) maxWidth / logo.getWidth(), (float) maxHeight / logo.getHeight());
            finalLogoWidth = Math.round(logo.getWidth() * scale);
            finalLogoHeight = Math.round(logo.getHeight() * scale);
        }

        // Centramos el logo calculando las coordenadas (X, Y)
        int x = (qrImage.getWidth() - finalLogoWidth) / 2;
        int y = (qrImage.getHeight() - finalLogoHeight) / 2;

        Graphics2D g = qrImage.createGraphics();
        
        // Fondo blanco detrás del logo para evitar que los píxeles del QR y el logo se mezclen
        g.setColor(Color.WHITE);
        g.fillRoundRect(x - 4, y - 4, finalLogoWidth + 8, finalLogoHeight + 8, 15, 15);
        
        // Dibujamos el logo final
        g.drawImage(logo, x, y, finalLogoWidth, finalLogoHeight, null);
        g.dispose();

        return qrImage;
    }

    private int normalizarDimension(int value) {
        return value > 0 ? value : QR_SIZE;
    }

    private String parsearPayload(QrGenerateRequest.TipoQr tipo, Map<String, String> payload) {
        return switch (tipo) {
            case URL -> obligatorio(payload, "url");
            case PHONE -> "tel:" + obligatorio(payload, "numero");
            case WHATSAPP -> {
                String numero = obligatorio(payload, "numero");
                String mensaje = obligatorio(payload, "mensaje");
                yield "https://wa.me/" + numero + "?text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
            }
            case EMAIL -> {
                String correo = obligatorio(payload, "correo");
                String asunto = obligatorio(payload, "asunto");
                yield "mailto:" + correo + "?subject=" + URLEncoder.encode(asunto, StandardCharsets.UTF_8);
            }
        };
    }

    private String obligatorio(Map<String, String> payload, String key) {
        String value = payload.get(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("El campo '" + key + "' es obligatorio para este tipo de QR");
        }
        return value;
    }

    private String colorNormalizado(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        if (!value.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("El color debe tener formato HEX #RRGGBB");
        }
        return value;
    }
}