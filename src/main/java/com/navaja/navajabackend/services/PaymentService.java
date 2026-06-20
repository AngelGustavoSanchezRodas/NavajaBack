package com.navaja.navajabackend.services;

import com.navaja.navajabackend.dto.PagoPendienteDto;
import com.navaja.navajabackend.models.EstadoPago;
import com.navaja.navajabackend.models.PagoManual;
import com.navaja.navajabackend.models.PlanUsuario;
import com.navaja.navajabackend.models.Suscripcion;
import com.navaja.navajabackend.models.Usuario;
import com.navaja.navajabackend.repositories.PagoManualRepository;
import com.navaja.navajabackend.repositories.SuscripcionRepository;
import com.navaja.navajabackend.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PagoManualRepository pagoManualRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticatedUserResolver userResolver;

    public PaymentService(PagoManualRepository pagoManualRepository, SuscripcionRepository suscripcionRepository, UsuarioRepository usuarioRepository, AuthenticatedUserResolver userResolver) {
        this.pagoManualRepository = pagoManualRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.userResolver = userResolver;
    }

    @Transactional
    public void registrarComprobante(String comprobanteUrl) {
        Usuario usuario = userResolver.resolveOrNull();
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        PagoManual pagoManual = new PagoManual(usuario, comprobanteUrl, EstadoPago.PENDING);
        pagoManualRepository.save(pagoManual);
    }

    public List<PagoPendienteDto> getPagosPendientes() {
        return pagoManualRepository.findByEstado(EstadoPago.PENDING)
                .stream()
                .map(pm -> {
                    Usuario u = pm.getUsuario();
                    ZonedDateTime premiumHasta = (u.getSuscripcion() != null) ? u.getSuscripcion().getPremiumHasta() : null;
                    return new PagoPendienteDto(u.getId(), u.getEmail(), pm.getComprobanteUrl(), premiumHasta);
                })
                .toList();
    }

    @Transactional
    public void aprobarPago(Long usuarioId) {
        Usuario admin = userResolver.resolveOrNull();
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin no autenticado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<PagoManual> pagosPendientes = pagoManualRepository.findByEstado(EstadoPago.PENDING).stream()
                .filter(p -> p.getUsuario().getId().equals(usuarioId))
                .toList();

        if (pagosPendientes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay pagos pendientes para este usuario");
        }

        PagoManual pagoManual = pagosPendientes.get(0);
        pagoManual.setEstado(EstadoPago.APPROVED);
        pagoManual.setFechaResolucion(ZonedDateTime.now());
        pagoManual.setRevisadoPor(admin);
        pagoManualRepository.save(pagoManual);

        Suscripcion suscripcion = usuario.getSuscripcion();
        if (suscripcion == null) {
            suscripcion = new Suscripcion(usuario, PlanUsuario.PREMIUM);
            suscripcion.setPremiumHasta(ZonedDateTime.now().plusDays(30));
        } else {
            suscripcion.setPlan(PlanUsuario.PREMIUM);
            if (suscripcion.getPremiumHasta() != null && suscripcion.getPremiumHasta().isAfter(ZonedDateTime.now())) {
                suscripcion.setPremiumHasta(suscripcion.getPremiumHasta().plusDays(30));
            } else {
                suscripcion.setPremiumHasta(ZonedDateTime.now().plusDays(30));
            }
        }
        suscripcionRepository.save(suscripcion);
    }
}