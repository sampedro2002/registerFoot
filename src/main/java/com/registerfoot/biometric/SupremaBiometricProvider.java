package com.registerfoot.biometric;

/**
 * Adaptador para lectores Suprema (BioStar 2 Device SDK). En produccion usa
 * {@code BS2_StartMonitoringLog} y traduce los eventos de verificacion a
 * {@link BiometricEvent}.
 */
public class SupremaBiometricProvider extends AbstractBiometricProvider {

    private final String ip;
    private final int puerto;

    public SupremaBiometricProvider(String ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
    }

    @Override
    public String marca() { return "SUPREMA"; }

    @Override
    public void iniciar() {
        // TODO: BS2_AllocateContext(); BS2_ConnectDevice(ip, puerto);
        //       BS2_StartMonitoringLog(cb -> emitir(...));
        activo.set(true);
    }

    @Override
    public void detener() {
        // TODO: BS2_DisconnectDevice(); BS2_ReleaseContext();
        activo.set(false);
    }
}
