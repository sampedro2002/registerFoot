package com.registerfoot.domain.enums;

/**
 * Roles del sistema. Cada rol concede un conjunto de permisos en
 * {@code SecurityConfig}. Spring Security usa el prefijo ROLE_.
 */
public enum Rol {
    ADMINISTRADOR,  // acceso total
    SUPERVISOR,     // operacion + reportes, sin configuracion sensible
    OPERADOR,       // operacion del punto de consumo
    AUDITOR         // solo lectura + auditoria + reportes
}
