package com.navaja.navajabackend.repositories;

import com.navaja.navajabackend.models.PlanUsuario;
import com.navaja.navajabackend.models.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Suscripcion s SET s.plan = :plan, s.premiumHasta = null WHERE s.premiumHasta < :ahora")
    int degradarCuentasExpiradas(@Param("ahora") ZonedDateTime ahora, @Param("plan") PlanUsuario plan);
}
