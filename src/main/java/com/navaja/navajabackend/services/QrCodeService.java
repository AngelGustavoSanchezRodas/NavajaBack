package com.navaja.navajabackend.services;

import com.navaja.navajabackend.dto.QrGenerateRequest;
import com.navaja.navajabackend.exceptions.AccesoDenegadoException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QrCodeService {

    private static final int QR_SIZE = 512;
    private static final String COLOR_FRENTE_ESTANDAR = "#000000";
    private static final String COLOR_FONDO_ESTANDAR = "#FFFFFF";

    private final QuotaService quotaService;

    public QrCodeService(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    public byte[] generateStandardQr(String data, int width, int height) {
        return procesarImagenQr(data, COLOR_FRENTE_ESTANDAR, COLOR_FONDO_ESTANDAR, normalizarDimension(width), normalizarDimension(height));
    }

    public byte[] generarQrPremium(QrGenerateRequest request, String usuarioId) {
        String colorFrente = colorNormalizado(request.colorFrente(), COLOR_FRENTE_ESTANDAR);
        String colorFondo = colorNormalizado(request.colorFondo(), COLOR_FONDO_ESTANDAR);
        boolean requierePremium = !COLOR_FRENTE_ESTANDAR.equalsIgnoreCase(colorFrente)
                || !COLOR_FONDO_ESTANDAR.equalsIgnoreCase(colorFondo)
                || StringUtils.hasText(request.logoBase64());

        if (requierePremium && !quotaService.validarPlanPremium(usuarioId)) {
            throw new AccesoDenegadoException("La personalización visual del QR requiere plan PRO");
        }

        String contenidoFinal = parsearPayload(request.tipo(), request.payload());

        return procesarImagenQr(contenidoFinal, colorFrente, colorFondo, QR_SIZE, QR_SIZE);
    }

    private byte[] procesarImagenQr(String data, String colorFrenteHex, String colorFondoHex, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            MatrixToImageConfig imageConfig = new MatrixToImageConfig(
                    Color.decode(colorFrenteHex).getRGB(),
                    Color.decode(colorFondoHex).getRGB()
            );
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream, imageConfig);
                return outputStream.toByteArray();
            }
        } catch (WriterException | IOException exception) {
            throw new IllegalArgumentException("No se pudo generar el codigo QR con los parametros enviados", exception);
        }
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

