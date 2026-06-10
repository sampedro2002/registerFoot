package com.registerfoot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TipoComidaDTO(
        Long id,
        @NotBlank String codigo,
        @NotBlank String nombre,
        @NotNull @PositiveOrZero BigDecimal valor,
        boolean activo
) {}
