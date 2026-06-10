package com.registerfoot.domain.entity;

import com.registerfoot.domain.enums.EstadoEmpleado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "empleados")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_biometrico", nullable = false, unique = true, length = 60)
    private String codigoBiometrico;

    @Column(nullable = false, unique = true, length = 30)
    private String documento;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 80)
    private String cargo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "concesion_id")
    private Concesion concesion;

    /** Categoría que determina el límite de consumos por día. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private CategoriaPersonal categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoEmpleado estado = EstadoEmpleado.ACTIVO;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void onUpdate() { actualizadoEn = LocalDateTime.now(); }

    @Transient
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    /** Límite de consumos por día (de la categoría; 1 si no tiene). */
    @Transient
    public int getLimiteDiario() {
        return categoria != null ? categoria.getLimiteDiario() : 1;
    }
}
