package com.navaja.navajabackend.repositories;

import com.navaja.navajabackend.models.Enlace;
import com.navaja.navajabackend.models.Usuario;
import com.navaja.navajabackend.models.TipoEnlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface EnlaceRepository extends JpaRepository<Enlace, Long> {
    Optional<Enlace> findByIdAndUsuarioId(Long id, Long usuarioId);

    Optional<Enlace> findByCodigoCorto(String codigoCorto);

    boolean existsByCodigoCorto(String codigoCorto);

    List<Enlace> findAllByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    long countByUsuario(Usuario usuario);

    long countByUsuarioIdAndTipo(Long usuarioId, TipoEnlace tipo);

    List<Enlace> findByFechaExpiracionBefore(OffsetDateTime fechaExpiracion);

    @Modifying
    @Query("delete from Enlace e where e.fechaExpiracion is not null and e.fechaExpiracion < :fechaExpiracion")
    int deleteByFechaExpiracionBefore(@Param("fechaExpiracion") OffsetDateTime fechaExpiracion);
}
