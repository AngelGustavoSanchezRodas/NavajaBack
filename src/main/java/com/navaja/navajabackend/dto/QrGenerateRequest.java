package com.navaja.navajabackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record QrGenerateRequest(
        @NotNull TipoQr tipo,
        @NotEmpty Map<String, String> payload,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String colorFondo,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String colorFrente,
        String logoBase64
) {

    public enum TipoQr {
        URL,
        PHONE,
        WHATSAPP,
        EMAIL
    }
}


