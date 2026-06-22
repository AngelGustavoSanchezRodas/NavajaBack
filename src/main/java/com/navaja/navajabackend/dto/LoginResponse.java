package com.navaja.navajabackend.dto;

public record LoginResponse(
        String token,
        UserProfileDto user
) {
}
