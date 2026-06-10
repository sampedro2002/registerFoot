package com.registerfoot.dto;

/**
 * Resultado del flujo de consumo biometrico, devuelto a la UI.
 * Si {@code aprobado} es false, {@code motivo} explica el rechazo.
 */
public record ConsumoResultadoDTO(
        boolean aprobado,
        String motivo,
        String empleadoNombre,
        TicketDTO ticket
) {
    public static ConsumoResultadoDTO rechazado(String empleado, String motivo) {
        return new ConsumoResultadoDTO(false, motivo, empleado, null);
    }

    public static ConsumoResultadoDTO aprobado(String empleado, TicketDTO ticket) {
        return new ConsumoResultadoDTO(true, "Consumo aprobado", empleado, ticket);
    }
}
