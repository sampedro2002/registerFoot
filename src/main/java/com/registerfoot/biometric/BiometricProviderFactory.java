package com.registerfoot.biometric;

import com.registerfoot.config.AppProperties;

/**
 * Crea la implementacion de {@link BiometricProvider} correspondiente a la
 * marca configurada (Factory Pattern). Centraliza el "switch" de SDKs para
 * que el resto del sistema permanezca agnostico al fabricante.
 */
public final class BiometricProviderFactory {

    private BiometricProviderFactory() {}

    public static BiometricProvider crear(AppProperties.Biometric cfg) {
        String marca = cfg.getProvider() == null ? "MOCK" : cfg.getProvider().trim().toUpperCase();
        String ip = cfg.getDeviceIp();
        int puerto = cfg.getDevicePort();
        return switch (marca) {
            case "ZKTECO"    -> new ZkTecoBiometricProvider(ip, puerto);
            case "SUPREMA"   -> new SupremaBiometricProvider(ip, puerto);
            case "ANVIZ"     -> new AnvizBiometricProvider(ip, puerto);
            case "HIKVISION" -> new HikvisionBiometricProvider(ip, puerto);
            default          -> new MockBiometricProvider();
        };
    }
}
