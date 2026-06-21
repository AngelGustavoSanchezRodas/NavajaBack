package com.navaja.navajabackend.services;

import com.navaja.navajabackend.models.Suscripcion;
import com.navaja.navajabackend.exceptions.AccesoDenegadoException;
import com.navaja.navajabackend.exceptions.LimiteExcedidoException;
import com.navaja.navajabackend.models.PlanUsuario;
import com.navaja.navajabackend.models.Usuario;
import com.navaja.navajabackend.repositories.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class QuotaService {

    private static final Duration VENTANA_USO = Duration.ofHours(24);
    private static final int LIMITE_GRATIS_ENLACES = 3;
    private static final int LIMITE_GRATIS_QR = 4;
    private static final int LIMITE_GRATIS_FIRMAS = 1;
    private static final int LIMITE_GRATIS_CONVERSIONES = 5;
    private static final int LIMITE_INVITADO_ENLACES = 1;
    private static final int LIMITE_INVITADO_QR = 1;
    private static final int LIMITE_INVITADO_CONVERSIONES = 1;

    private final UsuarioRepository usuarioRepository;
    private final ConcurrentMap<String, UsoVentana> usos = new ConcurrentHashMap<>();

    public QuotaService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ... (resto de tus importaciones y constantes) ...

    public void verificarLimite(Usuario usuario, String aliasPersonalizado) {
        if (usuario == null) {
            return;
        }

        // 1. Bypass absoluto para administradores (Beneficio permanente)
        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return;
        }

        PlanUsuario plan = (usuario.getSuscripcion() != null && usuario.getSuscripcion().getPlan() != null)
                           ? usuario.getSuscripcion().getPlan() : PlanUsuario.FREE;

        if (plan == PlanUsuario.FREE && StringUtils.hasText(aliasPersonalizado)) {
            throw new AccesoDenegadoException("Los alias personalizados son una función PRO.");
        }
    }

    public boolean validarPlanPremium(String usuarioId) {
        if (!StringUtils.hasText(usuarioId)) {
            return false;
        }

        long id;
        try {
            id = Long.parseLong(usuarioId);
        } catch (NumberFormatException exception) {
            return false;
        }

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // 2. Si el rol es ADMIN, el sistema lo trata como un usuario PREMIUM perpetuo
                    if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
                        return true;
                    }
                    // Si no es admin, evaluamos su suscripción real
                    return usuario.getSuscripcion() != null && usuario.getSuscripcion().getPlan() == PlanUsuario.PREMIUM;
                })
                .orElse(false);
    }

    public void validarConversionPremium(String usuarioId, String formatoSalida) {
        String formatoUpper = formatoSalida == null ? "" : formatoSalida.toUpperCase();

        if ("WEBP".equals(formatoUpper) || "TIFF".equals(formatoUpper) ||
            "BMP".equals(formatoUpper) || "GIF".equals(formatoUpper)) {
            if (!validarPlanPremium(usuarioId)) {
                throw new AccesoDenegadoException("El formato " + formatoSalida + " es exclusivo del plan PRO");
            }
        }
    }

    public void validarCreacionFirma(String usuarioId, String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("El campo templateId es obligatorio para crear una firma");
        }

        Set<String> TEMPLATES_BASICOS = Set.of("1", "2");
        String templateNormalizado = templateId.trim();

        if (!TEMPLATES_BASICOS.contains(templateNormalizado)) {
            if (!validarPlanPremium(usuarioId)) {
                throw new AccesoDenegadoException("La plantilla seleccionada es exclusiva del plan PRO");
            }
        }
    }

    public void validarCreacionAcortador(String usuarioId, String identificadorCliente) {
        validarUso("STANDARD", usuarioId, identificadorCliente);
    }

    public void registrarCreacionAcortador(String usuarioId, String identificadorCliente) {
        registrarUso("STANDARD", usuarioId, identificadorCliente);
    }

    public void validarCreacionQr(String usuarioId, String identificadorCliente) {
        validarUso("QR", usuarioId, identificadorCliente);
    }

    public void registrarCreacionQr(String usuarioId, String identificadorCliente) {
        registrarUso("QR", usuarioId, identificadorCliente);
    }

    public void validarUsoConversionImagen(String usuarioId, String identificadorCliente) {
        validarUso("IMAGE_CONVERSION", usuarioId, identificadorCliente);
    }

    public void registrarUsoConversionImagen(String usuarioId, String identificadorCliente) {
        registrarUso("IMAGE_CONVERSION", usuarioId, identificadorCliente);
    }

    public void registrarCreacionFirma(String usuarioId, String identificadorCliente) {
        registrarUso("SIGNATURE", usuarioId, identificadorCliente);
    }

    public void validarUsoFirma(String usuarioId, String identificadorCliente) {
        if (!StringUtils.hasText(usuarioId)) {
            throw new AccesoDenegadoException("Las firmas requieren una cuenta registrada");
        }
        validarUso("SIGNATURE", usuarioId, identificadorCliente);
    }

    private void validarUso(String tipoUso, String usuarioId, String identificadorCliente) {
        if (validarPlanPremium(usuarioId)) {
            return;
        }

        int limite = limiteMaximo(tipoUso, usuarioId);
        String clave = claveUso(usuarioId, identificadorCliente, tipoUso);
        UsoVentana usoVentana = usos.compute(clave, (key, actual) -> normalizarVentana(actual));

        if (usoVentana.getCantidad() >= limite) {
            throw new LimiteExcedidoException(mensajeLimite(tipoUso, usuarioId));
        }
    }

    private void registrarUso(String tipoUso, String usuarioId, String identificadorCliente) {
        if (validarPlanPremium(usuarioId)) {
            return;
        }

        String clave = claveUso(usuarioId, identificadorCliente, tipoUso);
        usos.compute(clave, (key, actual) -> {
            UsoVentana ventana = normalizarVentana(actual);
            ventana.incrementar();
            return ventana;
        });
    }

    private UsoVentana normalizarVentana(UsoVentana actual) {
        OffsetDateTime ahora = OffsetDateTime.now();
        if (actual == null || actual.expiraEn.isBefore(ahora)) {
            return new UsoVentana(1, ahora.plus(VENTANA_USO));
        }
        return actual;
    }

    private String claveUso(String usuarioId, String identificadorCliente, String tipoUso) {
        String scope = StringUtils.hasText(usuarioId) ? "user:" + usuarioId.trim() : "guest:" + normalizarCliente(identificadorCliente);
        return scope + ":" + tipoUso;
    }

    private String normalizarCliente(String identificadorCliente) {
        return StringUtils.hasText(identificadorCliente) ? identificadorCliente.trim() : "unknown";
    }

    private int limiteMaximo(String tipoUso, String usuarioId) {
        if (validarPlanPremium(usuarioId)) {
            return Integer.MAX_VALUE;
        }

        boolean esUsuarioRegistrado = StringUtils.hasText(usuarioId);
        return switch (tipoUso) {
            case "STANDARD" -> esUsuarioRegistrado ? LIMITE_GRATIS_ENLACES : LIMITE_INVITADO_ENLACES;
            case "QR" -> esUsuarioRegistrado ? LIMITE_GRATIS_QR : LIMITE_INVITADO_QR;
            case "SIGNATURE" -> esUsuarioRegistrado ? LIMITE_GRATIS_FIRMAS : 0;
            case "IMAGE_CONVERSION" -> esUsuarioRegistrado ? LIMITE_GRATIS_CONVERSIONES : LIMITE_INVITADO_CONVERSIONES;
            default -> throw new IllegalArgumentException("Tipo de uso desconocido: " + tipoUso);
        };
    }

    private String mensajeLimite(String tipoUso, String usuarioId) {
        return switch (tipoUso) {
            case "STANDARD" -> StringUtils.hasText(usuarioId)
                    ? "Has alcanzado el límite de 3 enlaces cortos gratuitos. Actualiza a PRO."
                    : "Has alcanzado el límite de 1 enlace corto temporal. Vuelve a intentarlo después de 24 horas.";
            case "QR" -> StringUtils.hasText(usuarioId)
                    ? "Has alcanzado el límite de 4 códigos QR gratuitos. Actualiza a PRO para crear ilimitados."
                    : "Has alcanzado el límite de 1 código QR temporal. Vuelve a intentarlo después de 24 horas.";
            case "SIGNATURE" -> "Los usuarios gratuitos solo pueden tener 1 firma activa";
            case "IMAGE_CONVERSION" -> StringUtils.hasText(usuarioId)
                    ? "Has alcanzado el límite de 5 conversiones de imagen gratuitas. Actualiza a PRO."
                    : "Has alcanzado el límite temporal de conversiones. Vuelve a intentarlo después de 24 horas.";
            default -> "Has alcanzado el límite permitido";
        };
    }

    @Scheduled(fixedDelayString = "${app.quota.cleanup-ms:3600000}")
    public void limpiarUsosExpirados() {
        OffsetDateTime ahora = OffsetDateTime.now();
        usos.entrySet().removeIf(entry -> entry.getValue().expiraEn.isBefore(ahora));
    }

    private static final class UsoVentana {
        private int cantidad;
        private final OffsetDateTime expiraEn;

        private UsoVentana(int cantidad, OffsetDateTime expiraEn) {
            this.cantidad = cantidad;
            this.expiraEn = expiraEn;
        }

        private int getCantidad() {
            return cantidad;
        }

        private void incrementar() {
            this.cantidad++;
        }
    }


}
