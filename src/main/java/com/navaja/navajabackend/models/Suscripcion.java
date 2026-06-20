package com.navaja.navajabackend.models;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "suscripciones")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 20)
    private PlanUsuario plan;

    @Column(name = "premium_hasta")
    private ZonedDateTime premiumHasta;

    @Column(name = "ultima_actualizacion", nullable = false)
    private ZonedDateTime ultimaActualizacion;

    public Suscripcion() {
    }

    public Suscripcion(Usuario usuario, PlanUsuario plan) {
        this.usuario = usuario;
        this.plan = plan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public PlanUsuario getPlan() {
        return plan;
    }

    public void setPlan(PlanUsuario plan) {
        this.plan = plan;
    }

    public ZonedDateTime getPremiumHasta() {
        return premiumHasta;
    }

    public void setPremiumHasta(ZonedDateTime premiumHasta) {
        this.premiumHasta = premiumHasta;
    }

    public ZonedDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(ZonedDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    @PrePersist
    void prePersist() {
        if (ultimaActualizacion == null) {
            ultimaActualizacion = ZonedDateTime.now();
        }
        if (plan == null) {
            plan = PlanUsuario.FREE;
        }
    }

    @PreUpdate
    void preUpdate() {
        ultimaActualizacion = ZonedDateTime.now();
    }
}
