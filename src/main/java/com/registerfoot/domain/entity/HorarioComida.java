package com.registerfoot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Ventana horaria valida para un tipo de comida. Un tipo de comida puede
 * tener varias ventanas (p.ej. dos turnos de almuerzo).
 */
@Entity
@Table(name = "horarios_comida")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HorarioComida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tipo_comida_id")
    private TipoComida tipoComida;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() { creadoEn = LocalDateTime.now(); }

    public boolean contiene(LocalTime hora) {
        return !hora.isBefore(horaInicio) && !hora.isAfter(horaFin);
    }
}
