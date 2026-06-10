package com.registerfoot.dto;

import com.registerfoot.domain.enums.EstadoEmpleado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO de transporte/edicion de empleados. */
public record EmpleadoDTO(
        Long id,
        @NotBlank String codigoBiometrico,
        @NotBlank String documento,
        @NotBlank String nombres,
        @NotBlank String apellidos,
        String cargo,
        @NotNull Long concesionId,
        String concesionNombre,
        Long categoriaId,
        String categoriaNombre,
        EstadoEmpleado estado
) {}
