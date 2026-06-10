package com.registerfoot.biometric;

/**
 * Implementacion de simulacion: no requiere hardware. La UI o las pruebas
 * inyectan codigos llamando a {@link #simular(String)}, reproduciendo lo que
 * haria un lector real. Tambien soporta un modo de emision automatica.
 */
public class MockBiometricProvider extends AbstractBiometricProvider {

    public static final String NOMBRE_DISPOSITIVO = "MOCK-LECTOR-01";

    @Override
    public String marca() { return "MOCK"; }

    @Override
    public void iniciar() { activo.set(true); }

    @Override
    public void detener() { activo.set(false); }

    /**
     * Simula que el lector identifico a alguien con el codigo dado.
     * Util para el modulo de Consumos y para pruebas automatizadas.
     */
    public void simular(String codigoBiometrico) {
        if (!estaActivo()) iniciar();
        emitir(BiometricEvent.de(codigoBiometrico, NOMBRE_DISPOSITIVO,
                BiometricEvent.BiometricType.HUELLA));
    }
}
