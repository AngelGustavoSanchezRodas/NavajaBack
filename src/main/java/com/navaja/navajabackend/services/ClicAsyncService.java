package com.navaja.navajabackend.services;

import com.navaja.navajabackend.models.Clic;
import com.navaja.navajabackend.models.Enlace;
import com.navaja.navajabackend.repositories.ClicRepository;
import com.navaja.navajabackend.repositories.EnlaceRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClicAsyncService {

    private final ClicRepository clicRepository;
    private final EnlaceRepository enlaceRepository;

    public ClicAsyncService(ClicRepository clicRepository, EnlaceRepository enlaceRepository) {
        this.clicRepository = clicRepository;
        this.enlaceRepository = enlaceRepository;
    }

    @Async
    @Transactional
    public void registrarClicAsync(String codigoCorto, String direccionIp, String userAgent) {
        Enlace enlace = enlaceRepository.findByCodigoCorto(codigoCorto).orElse(null);
        if (enlace == null) {
            return;
        }

        Clic clic = new Clic();
        clic.setEnlace(enlace);
        clic.setDireccionIp(direccionIp);
        clic.setUserAgent(userAgent);
        clicRepository.save(clic);
    }
}
