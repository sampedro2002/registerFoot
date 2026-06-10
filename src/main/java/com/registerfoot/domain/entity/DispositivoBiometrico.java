package com.registerfoot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispositivos_biometricos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispositivoBiometrico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String marca = "MOCK"; // ZKTECO|SUPREMA|ANVIZ|HIKVISION|MOCK

    @Column(length = 45)
    private String ip;

    private Integer puerto;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() { creadoEn = LocalDateTime.now(); }
}
