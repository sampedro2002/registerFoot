package com.registerfoot.biometric;

/**
 * Adaptador para lectores ZKTeco. En produccion envuelve el SDK
 * {@code ZKemkeeper} / {@code pyzk} (protocolo en puerto 4370) y traduce sus
 * callbacks de "OnAttTransaction" a {@link BiometricEvent}.
 *
 * <p>Aqui se deja la estructura lista; los TODO marcan los puntos de
 * integracion con el JNI/socket del fabricante.</p>
 */
public class ZkTecoBiometricProvider extends AbstractBiometricProvider {

    private final String ip;
    private final int puerto;

    public ZkTecoBiometricProvider(String ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
    }

    @Override
    public String marca() { return "ZKTECO"; }

    @Override
    public void iniciar() {
        // TODO: zk.connect(ip, puerto); zk.regEvent(EF_ATTLOG);
        //       zk.onTransaction = (pin) -> emitir(BiometricEvent.de(pin, "ZKTECO@"+ip, HUELLA));
        activo.set(true);
    }

    @Override
    public void detener() {
        // TODO: zk.disconnect();
        activo.set(false);
    }
}
