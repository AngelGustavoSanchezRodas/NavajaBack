package com.navaja.navajabackend.services;

import com.navaja.navajabackend.repositories.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class SubscriptionCronService {

    private final UsuarioRepository usuarioRepository;

    public SubscriptionCronService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void ejecutarDegradacion() {
        int actualizados = usuarioRepository.degradarCuentasExpiradas(
            ZonedDateTime.now(), 
            com.navaja.navajabackend.models.PlanUsuario.FREE, 
            com.navaja.navajabackend.models.EstadoPago.NONE
    );
    System.out.println("CronJob de degradación ejecutado. Cuentas degradadas: " + actualizados);
}
}
