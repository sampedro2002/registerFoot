package com.registerfoot.biometric;

import java.time.LocalDateTime;

/**
 * Evento emitido por un dispositivo biometrico cuando identifica a una persona.
 * El unico dato funcionalmente relevante es {@code codigoBiometrico}; el resto
 * es metadata de trazabilidad.
 */
public record BiometricEvent(
        String codigoBiometrico,
        String dispositivo,
        BiometricType tipo,
        LocalDateTime momento
) {
    public enum BiometricType { HUELLA, ROSTRO, TARJETA, DESCONOCIDO }

    public static BiometricEvent de(String codigo, String dispositivo, BiometricType tipo) {
        return new BiometricEvent(codigo, dispositivo, tipo, LocalDateTime.now());
    }
}
