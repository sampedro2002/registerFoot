package com.registerfoot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Bitacora inmutable de acciones del sistema. */
@Entity
@Table(name = "auditoria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60)
    private String usuario;

    @Column(nullable = false, length = 40)
    private String accion;

    @Column(length = 60)
    private String entidad;

    @Column(name = "entidad_id", length = 40)
    private String entidadId;

    @Column(length = 500)
    private String detalle;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String resultado = "OK";

    @Column(length = 45)
    private String ip;

    @Column(name = "fecha_hora", updatable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    void onCreate() { if (fechaHora == null) fechaHora = LocalDateTime.now(); }
}
