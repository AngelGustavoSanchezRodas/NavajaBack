package com.navaja.navajabackend.models;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "pagos_manuales")
public class PagoManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "comprobante_url", nullable = false, length = 500)
    private String comprobanteUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private ZonedDateTime fechaSolicitud;

    @Column(name = "fecha_resolucion")
    private ZonedDateTime fechaResolucion;

    @ManyToOne
    @JoinColumn(name = "revisado_por_id")
    private Usuario revisadoPor;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public PagoManual() {
    }

    public PagoManual(Usuario usuario, String comprobanteUrl, EstadoPago estado) {
        this.usuario = usuario;
        this.comprobanteUrl = comprobanteUrl;
        this.estado = estado;
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

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }

    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public ZonedDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(ZonedDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public ZonedDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(ZonedDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public Usuario getRevisadoPor() {
        return revisadoPor;
    }

    public void setRevisadoPor(Usuario revisadoPor) {
        this.revisadoPor = revisadoPor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @PrePersist
    void prePersist() {
        if (fechaSolicitud == null) {
            fechaSolicitud = ZonedDateTime.now();
        }
        if (estado == null) {
            estado = EstadoPago.PENDING;
        }
    }
}
