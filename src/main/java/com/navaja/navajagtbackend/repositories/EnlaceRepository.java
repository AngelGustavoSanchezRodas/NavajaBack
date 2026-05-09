package com.navaja.navajagtbackend.repositories;

import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.models.TipoEnlace;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
