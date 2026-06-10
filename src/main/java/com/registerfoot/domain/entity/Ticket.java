package com.registerfoot.domain.entity;

import com.registerfoot.domain.enums.EstadoTicket;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @Column(name = "registro_id", nullable = false)
    private Long registroId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tipo_comida_id")
    private TipoComida tipoComida;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "concesion_id")
    private Concesion concesion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "qr_payload", nullable = false, length = 255)
    private String qrPayload;

    @Column(nullable = false)
    @Builder.Default
    private boolean impreso = false;

    @Column(nullable = false)
    @Builder.Default
    private int reimpresiones = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoTicket estado = EstadoTicket.GENERADO;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() { creadoEn = LocalDateTime.now(); }
}
