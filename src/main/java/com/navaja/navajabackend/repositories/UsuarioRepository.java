package com.navaja.navajabackend.repositories;

import com.navaja.navajabackend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.repository.query.Param;
import com.navaja.navajabackend.models.EstadoPago;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByEstadoPago(EstadoPago estadoPago);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.plan = :plan, u.estadoPago = :estado, u.premiumHasta = null WHERE u.premiumHasta < :ahora")
    int degradarCuentasExpiradas(@Param("ahora") java.time.ZonedDateTime ahora, 
                             @Param("plan") com.navaja.navajabackend.models.PlanUsuario plan, 
                             @Param("estado") com.navaja.navajabackend.models.EstadoPago estado);
                             
}


