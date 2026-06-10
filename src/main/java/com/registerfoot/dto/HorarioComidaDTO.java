package com.registerfoot.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioComidaDTO(
        Long id,
        @NotNull Long tipoComidaId,
        String tipoComidaNombre,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        boolean activo
) {}
