package com.navaja.navajabackend.dto;

import java.time.ZonedDateTime;

public record PagoPendienteDto(
    Long id,
    String email,
    String comprobanteUrl,
    ZonedDateTime premiumHasta
) {}