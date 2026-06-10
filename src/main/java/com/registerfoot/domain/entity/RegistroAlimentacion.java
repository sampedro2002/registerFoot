package com.registerfoot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Cada transaccion de consumo registrada (biometrica o manual). */
@Entity
@Table(name = "registros_alimentacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistroAlimentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tipo_comida_id")
    private TipoComida tipoComida;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "concesion_id")
    private Concesion concesion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valor = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String origen = "BIOMETRICO";

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() { creadoEn = LocalDateTime.now(); }
}
