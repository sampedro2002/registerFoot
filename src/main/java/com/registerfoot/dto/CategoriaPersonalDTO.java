package com.registerfoot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CategoriaPersonalDTO(
        Long id,
        @NotBlank String codigo,
        @NotBlank String nombre,
        @Positive int limiteDiario,
        boolean activo
) {}
