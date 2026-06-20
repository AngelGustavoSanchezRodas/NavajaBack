package com.navaja.navajabackend.services;

import com.navaja.navajabackend.dto.PagoPendienteDto; // <-- IMPORT VITAL FALTANTE
import com.navaja.navajabackend.models.EstadoPago;
import com.navaja.navajabackend.models.PlanUsuario;
import com.navaja.navajabackend.models.Usuario;
import com.navaja.navajabackend.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticatedUserResolver userResolver;

    public PaymentService(UsuarioRepository usuarioRepository, AuthenticatedUserResolver userResolver) {
        this.usuarioRepository = usuarioRepository;
        this.userResolver = userResolver;
    }

    @Transactional
    public void registrarComprobante(String comprobanteUrl) {
        Usuario usuario = userResolver.resolveOrNull();
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        usuario.setComprobanteUrl(comprobanteUrl);
        usuario.setEstadoPago(EstadoPago.PENDING);
        usuarioRepository.save(usuario);
    }

    // <-- AQUÍ ESTABA EL ERROR: USAR PagoPendienteDto EN VEZ DE PagoPendiente
    public List<PagoPendienteDto> getPagosPendientes() {
        return usuarioRepository.findByEstadoPago(EstadoPago.PENDING)
                .stream()
                .map(u -> new PagoPendienteDto(u.getId(), u.getEmail(), u.getComprobanteUrl(), u.getPremiumHasta()))
                .toList();
    }

    @Transactional
    public void aprobarPago(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setPlan(PlanUsuario.PREMIUM);

        ZonedDateTime nuevaFecha;
        if (usuario.getPremiumHasta() != null && usuario.getPremiumHasta().isAfter(ZonedDateTime.now())) {
            nuevaFecha = usuario.getPremiumHasta().plusDays(30);
        } else {
            nuevaFecha = ZonedDateTime.now().plusDays(30);
        }
        usuario.setPremiumHasta(nuevaFecha);
        usuario.setEstadoPago(EstadoPago.APPROVED);

        usuarioRepository.save(usuario);
    }
}