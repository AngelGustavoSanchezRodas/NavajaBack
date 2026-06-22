package com.navaja.navajabackend.dto;

import java.time.ZonedDateTime;

public record UserProfileDto(
        Long id,
        String nombre,
        String email,
        String rol,
        String plan,
        ZonedDateTime premium_hasta
) {
}
