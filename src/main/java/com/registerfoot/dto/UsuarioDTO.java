package com.registerfoot.dto;

import com.registerfoot.domain.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioDTO(
        Long id,
        @NotBlank String username,
        @NotBlank String nombreCompleto,
        String email,
        @NotNull Rol rol,
        boolean activo,
        // solo se usa al crear/cambiar clave; nunca se devuelve poblado
        String password
) {}
