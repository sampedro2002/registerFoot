package com.registerfoot.biometric;

/**
 * Adaptador para terminales Hikvision (reconocimiento facial). En produccion
 * se suscribe al "Alarm/Event ISAPI Stream" (HTTP listening / ISAPI) y mapea
 * el campo {@code employeeNoString} a {@link BiometricEvent}.
 */
public class HikvisionBiometricProvider extends AbstractBiometricProvider {

    private final String ip;
    private final int puerto;

    public HikvisionBiometricProvider(String ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
    }

    @Override
    public String marca() { return "HIKVISION"; }

    @Override
    public void iniciar() {
        // TODO: abrir ISAPI alertStream y emitir por cada AccessControllerEvent
        activo.set(true);
    }

    @Override
    public void detener() {
        activo.set(false);
    }
}
