package com.registerfoot.dto;

import jakarta.validation.constraints.NotBlank;

public record ConcesionDTO(
        Long id,
        @NotBlank String codigo,
        @NotBlank String nombre,
        String nit,
        String contacto,
        String telefono,
        boolean activo
) {}
