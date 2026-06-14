package com.navaja.navajabackend.services;

import com.navaja.navajabackend.repositories.EnlaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class LimpiezaProgramadaService {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaProgramadaService.class);

    private final EnlaceRepository enlaceRepository;

    public LimpiezaProgramadaService(EnlaceRepository enlaceRepository) {
        this.enlaceRepository = enlaceRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void limpiarEnlacesExpirados() {
        int eliminados = enlaceRepository.deleteByFechaExpiracionBefore(OffsetDateTime.now());
        if (eliminados > 0) {
            log.info("Limpieza programada: {} enlaces expirados eliminados", eliminados);
        }
    }
}

