package com.navaja.navajabackend.services;

import com.navaja.navajabackend.exceptions.AccesoDenegadoException;
import com.navaja.navajabackend.models.PlanUsuario;
import com.navaja.navajabackend.models.Usuario;
import com.navaja.navajabackend.repositories.EnlaceRepository;
import com.navaja.navajabackend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotaServiceTest {

    @Mock
    private EnlaceRepository enlaceRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private QuotaService quotaService;

    @Test
    void verificarLimiteDebeBloquearAliasPersonalizadoEnPlanGratis() {
        Usuario usuario = new Usuario();
        usuario.setPlan(PlanUsuario.FREE);

        assertThrows(AccesoDenegadoException.class, () -> quotaService.verificarLimite(usuario, "mi-alias"));
    }

    @Test
    void verificarLimiteDebePermitirUsuarioPremium() {
        Usuario usuario = new Usuario();
        usuario.setPlan(PlanUsuario.PREMIUM);

        assertDoesNotThrow(() -> quotaService.verificarLimite(usuario, null));
    }
}
