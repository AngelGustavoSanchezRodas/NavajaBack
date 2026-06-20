package com.navaja.navajabackend.dto;
import java.time.ZonedDateTime;
public record PagoPendiente (Long id, String email, String comprobanteUrl, ZonedDateTime premiumHasta) {}