package com.navaja.navajabackend.services;

import com.navaja.navajabackend.repositories.SuscripcionRepository;
import com.navaja.navajabackend.models.PlanUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class SubscriptionCronService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionCronService.class);

    private final SuscripcionRepository suscripcionRepository;

    public SubscriptionCronService(SuscripcionRepository suscripcionRepository) {
        this.suscripcionRepository = suscripcionRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void ejecutarDegradacion() {
        int actualizados = suscripcionRepository.degradarCuentasExpiradas(
                ZonedDateTime.now(),
                PlanUsuario.FREE
        );
        log.info("Suscripciones expiradas degradadas: {}", actualizados);
    }
}
