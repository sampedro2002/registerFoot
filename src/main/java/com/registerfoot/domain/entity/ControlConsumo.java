package com.registerfoot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Controla el límite de consumos por día por empleado. La unicidad
 * (empleado, fecha, secuencia) respaldada por UNIQUE garantiza, incluso ante
 * concurrencia, que no se exceda el límite diario de la categoría: dos
 * consumos simultáneos intentarían la misma secuencia y uno fallaría.
 */
@Entity
@Table(name = "control_consumo",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_control",
                columnNames = {"empleado_id", "fecha", "secuencia"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empleado_id", nullable = false)
    private Long empleadoId;

    @Column(name = "tipo_comida_id", nullable = false)
    private Long tipoComidaId;

    @Column(nullable = false)
    private LocalDate fecha;

    /** Número de consumo del empleado en el día (1, 2, ...). */
    @Column(nullable = false)
    private int secuencia;

    @Column(name = "registro_id", nullable = false)
    private Long registroId;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() { creadoEn = LocalDateTime.now(); }
}
