package com.navaja.navajabackend.repositories;

import com.navaja.navajabackend.models.Clic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClicRepository extends JpaRepository<Clic, Long> {
    void deleteByEnlaceId(Long enlaceId);
}

