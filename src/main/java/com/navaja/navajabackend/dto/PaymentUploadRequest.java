package com.navaja.navajabackend.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentUploadRequest(
        @NotBlank String url
) {
}
