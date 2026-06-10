package com.registerfoot.biometric;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Plantilla base con la gestion de listeners y estado, comun a todas las
 * implementaciones. Las subclases solo implementan la conexion real al SDK
 * y llaman a {@link #emitir(BiometricEvent)} cuando el dispositivo identifica.
 */
public abstract class AbstractBiometricProvider implements BiometricProvider {

    private final List<BiometricListener> listeners = new CopyOnWriteArrayList<>();
    protected final AtomicBoolean activo = new AtomicBoolean(false);

    @Override
    public boolean estaActivo() { return activo.get(); }

    @Override
    public void suscribir(BiometricListener listener) {
        if (listener != null) listeners.add(listener);
    }

    @Override
    public void desuscribir(BiometricListener listener) {
        listeners.remove(listener);
    }

    /** Difunde el evento a todos los listeners registrados. */
    protected void emitir(BiometricEvent event) {
        for (BiometricListener l : listeners) {
            try {
                l.onEvent(event);
            } catch (Exception ex) {
                // un listener defectuoso no debe afectar a los demas
                System.err.println("Listener biometrico fallo: " + ex.getMessage());
            }
        }
    }
}
