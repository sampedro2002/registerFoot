package com.registerfoot.biometric;

/**
 * Contrato desacoplado para cualquier dispositivo biometrico. Permite
 * sustituir el SDK concreto (ZKTeco, Suprema, Anviz, Hikvision) o un mock
 * sin afectar la logica de negocio (Dependency Inversion - SOLID).
 *
 * Implementaciones reales envuelven el SDK del fabricante; la capa de
 * servicio solo conoce esta interfaz.
 */
public interface BiometricProvider {

    /** Identificador de la marca (ZKTECO, SUPREMA, ANVIZ, HIKVISION, MOCK). */
    String marca();

    /** Abre la conexion con el dispositivo y comienza a escuchar eventos. */
    void iniciar();

    /** Cierra la conexion y libera recursos. */
    void detener();

    /** true si el dispositivo esta conectado y escuchando. */
    boolean estaActivo();

    /** Registra un listener que recibira los eventos de identificacion. */
    void suscribir(BiometricListener listener);

    /** Quita un listener previamente registrado. */
    void desuscribir(BiometricListener listener);
}
