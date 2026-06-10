package com.registerfoot.biometric;

import com.registerfoot.config.AppProperties;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BiometricProviderTest {

    @Test
    void factoriaCreaImplementacionSegunMarca() {
        AppProperties.Biometric cfg = new AppProperties.Biometric();
        cfg.setProvider("ZKTECO");
        assertEquals("ZKTECO", BiometricProviderFactory.crear(cfg).marca());

        cfg.setProvider("HIKVISION");
        assertEquals("HIKVISION", BiometricProviderFactory.crear(cfg).marca());

        cfg.setProvider("DESCONOCIDA"); // cae al MOCK por defecto
        assertEquals("MOCK", BiometricProviderFactory.crear(cfg).marca());
    }

    @Test
    void mockNotificaAlListenerElCodigoSimulado() {
        MockBiometricProvider mock = new MockBiometricProvider();
        AtomicReference<String> recibido = new AtomicReference<>();
        mock.suscribir(ev -> recibido.set(ev.codigoBiometrico()));

        mock.iniciar();
        mock.simular("BIO-1001");

        assertEquals("BIO-1001", recibido.get());
        assertTrue(mock.estaActivo());
    }
}
