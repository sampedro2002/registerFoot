package com.registerfoot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vista de un ticket lista para mostrar/imprimir. */
public record TicketDTO(
        Long id,
        String numero,
        String empleadoNombre,
        String empleadoDocumento,
        String tipoComida,
        String concesion,
        LocalDateTime fechaHora,
        BigDecimal valor,
        String qrPayload,
        boolean impreso,
        int reimpresiones,
        String estado
) {}
