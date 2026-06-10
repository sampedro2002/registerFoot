package com.registerfoot.exception;

/**
 * El flujo biometrico rechazo el consumo (empleado inactivo, fuera de
 * horario, consumo duplicado, etc.). Lleva un motivo legible para el operador.
 */
public class ConsumoRechazadoException extends RegisterFootException {

    public enum Motivo {
        EMPLEADO_NO_ENCONTRADO,
        EMPLEADO_INACTIVO,
        FUERA_DE_HORARIO,
        LIMITE_DIARIO_ALCANZADO,
        TIPO_COMIDA_INACTIVO,
        SIN_HORARIO_ACTIVO
    }

    private final Motivo motivo;

    public ConsumoRechazadoException(Motivo motivo, String message) {
        super(message);
        this.motivo = motivo;
    }

    public Motivo getMotivo() { return motivo; }
}
