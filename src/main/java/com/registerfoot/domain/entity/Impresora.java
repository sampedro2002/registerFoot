package com.registerfoot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "impresoras")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Impresora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String tipo = "ESC_POS"; // ESC_POS|JAVA_PRINT|MOCK

    @Column(length = 150)
    private String destino;

    @Column(name = "char_por_linea", nullable = false)
    @Builder.Default
    private int charPorLinea = 42;

    @Column(name = "por_defecto", nullable = false)
    @Builder.Default
    private boolean porDefecto = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() { creadoEn = LocalDateTime.now(); }
}
