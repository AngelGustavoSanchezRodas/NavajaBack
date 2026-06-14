package com.navaja.navajabackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistroRequest(
        @Email @NotBlank String email,
        @NotBlank String contrasena
) {
}


