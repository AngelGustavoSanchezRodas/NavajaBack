package com.navaja.navajabackend.services;

import com.navaja.navajabackend.repositories.SuscripcionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class SubscriptionCronService {

    private final SuscripcionRepository suscripcionRepository;

    public SubscriptionCronService(SuscripcionRepository suscripcionRepository) {
        this.suscripcionRepository = suscripcionRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void ejecutarDegradacion() {
        int actualizados = suscripcionRepository.degradarCuentasExpiradas(
            ZonedDateTime.now(), 
            com.navaja.navajabackend.models.PlanUsuario.FREE
    );
    System.out.println("CronJob de degradación ejecutado. Suscripciones degradadas: " + actualizados);
}
}
