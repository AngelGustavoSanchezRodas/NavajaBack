package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.dto.PagoPendienteDto;
import com.navaja.navajabackend.dto.PaymentUploadRequest;
import com.navaja.navajabackend.services.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/api/v1/payments/upload")
    public ResponseEntity<Void> uploadPayment(@Valid @RequestBody PaymentUploadRequest request) {
        paymentService.registrarComprobante(request.url());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/v1/admin/payments/pending")
    public ResponseEntity<List<PagoPendienteDto>> getPendingPayments() {
        return ResponseEntity.ok(paymentService.getPagosPendientes());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/admin/payments/approve/{usuarioId}")
    public ResponseEntity<Void> approvePayment(@PathVariable Long usuarioId) {
        paymentService.aprobarPago(usuarioId);
        return ResponseEntity.ok().build();
    }
}
