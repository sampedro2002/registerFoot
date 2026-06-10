package com.registerfoot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Categoría de personal. Define cuántas veces puede consumir un empleado por
 * día (límite total diario, sin importar el tipo de comida). Ej: NORMAL=1,
 * ESPECIAL=2.
 */
@Entity
@Table(name = "categorias_personal")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoriaPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    /** Máximo de consumos permitidos por día para esta categoría. */
    @Column(name = "limite_diario", nullable = false)
    @Builder.Default
    private int limiteDiario = 1;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() { creadoEn = LocalDateTime.now(); }
}
