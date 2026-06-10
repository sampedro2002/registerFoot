package com.registerfoot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "concesiones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Concesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 30)
    private String nit;

    @Column(length = 120)
    private String contacto;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() { creadoEn = LocalDateTime.now(); }
}
