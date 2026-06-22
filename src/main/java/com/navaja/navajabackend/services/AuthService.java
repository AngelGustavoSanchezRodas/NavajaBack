package com.navaja.navajabackend.services;

import com.navaja.navajabackend.dto.LoginResponse;
import com.navaja.navajabackend.dto.RegistroRequest;
import com.navaja.navajabackend.models.PlanUsuario;
import com.navaja.navajabackend.models.Suscripcion;
import com.navaja.navajabackend.models.Usuario;
import com.navaja.navajabackend.repositories.UsuarioRepository;
import com.navaja.navajabackend.security.ServicioJwt;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicioJwt servicioJwt;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, ServicioJwt servicioJwt) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.servicioJwt = servicioJwt;
    }

    public void registrar(RegistroRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));

        Suscripcion suscripcion = new Suscripcion(usuario, PlanUsuario.FREE);
        usuario.setSuscripcion(suscripcion);

        usuarioRepository.save(usuario);
    }

    public LoginResponse iniciarSesion(String email, String contrasena) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = servicioJwt.generarToken(usuario.getEmail(), Map.of("uid", usuario.getId()));
        
        com.navaja.navajabackend.dto.UserProfileDto userProfileDto = new com.navaja.navajabackend.dto.UserProfileDto(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getSuscripcion() != null ? usuario.getSuscripcion().getPlan().name() : "FREE",
                usuario.getSuscripcion() != null ? usuario.getSuscripcion().getPremiumHasta() : null
        );

        return new LoginResponse(token, userProfileDto);
    }

    public com.navaja.navajabackend.dto.UserProfileDto obtenerPerfilUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
                
        return new com.navaja.navajabackend.dto.UserProfileDto(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getSuscripcion() != null ? usuario.getSuscripcion().getPlan().name() : "FREE",
                usuario.getSuscripcion() != null ? usuario.getSuscripcion().getPremiumHasta() : null
        );
    }
}
