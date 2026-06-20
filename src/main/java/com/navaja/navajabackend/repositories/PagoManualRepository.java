package com.navaja.navajabackend.repositories;

import com.navaja.navajabackend.models.EstadoPago;
import com.navaja.navajabackend.models.PagoManual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoManualRepository extends JpaRepository<PagoManual, Long> {
    List<PagoManual> findByEstado(EstadoPago estado);
}
