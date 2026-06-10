package com.registerfoot.biometric;

/** Callback invocado cuando llega un evento del lector. */
@FunctionalInterface
public interface BiometricListener {
    void onEvent(BiometricEvent event);
}
