package com.registerfoot.biometric;

/**
 * Adaptador para lectores Anviz (CrossChex SDK / protocolo TC). Traduce los
 * registros de marcacion a {@link BiometricEvent}.
 */
public class AnvizBiometricProvider extends AbstractBiometricProvider {

    private final String ip;
    private final int puerto;

    public AnvizBiometricProvider(String ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
    }

    @Override
    public String marca() { return "ANVIZ"; }

    @Override
    public void iniciar() {
        // TODO: conectar via TCP al protocolo Anviz y suscribir records
        activo.set(true);
    }

    @Override
    public void detener() {
        activo.set(false);
    }
}
