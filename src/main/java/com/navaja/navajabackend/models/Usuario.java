package com.navaja.navajabackend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 20)
    private PlanUsuario plan;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @OneToMany(mappedBy = "usuario")
    private List<Enlace> enlaces = new ArrayList<>();

    @Column(name = "premium_hasta")
    private ZonedDateTime premiumHasta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", length = 20)
    private EstadoPago estadoPago;

    @Column(name = "comprobante_url", length = 500)
    private String comprobanteUrl;

    @Column(name = "rol", nullable = false, length = 50)
    private String rol;

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public PlanUsuario getPlan() {
        return plan;
    }

    public void setPlan(PlanUsuario plan) {
        this.plan = plan;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public List<Enlace> getEnlaces() {
        return enlaces;
    }

    public void setEnlaces(List<Enlace> enlaces) {
        this.enlaces = enlaces;
    }

    public ZonedDateTime getPremiumHasta() {
        return premiumHasta;
    }

    public void setPremiumHasta(ZonedDateTime premiumHasta) {
        this.premiumHasta = premiumHasta;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }

    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = OffsetDateTime.now();
        }
        if (plan == null) {
            plan = PlanUsuario.FREE;
        }
        if (estadoPago == null) {
            estadoPago = EstadoPago.NONE;
        }
        if (rol == null) {
            rol = "USER";
        }
    }
}


