package com.registerfoot.config;

import com.registerfoot.biometric.BiometricProvider;
import com.registerfoot.biometric.BiometricProviderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Publica el {@link BiometricProvider} activo como bean de Spring. */
@Configuration
public class BiometricConfig {

    @Bean(destroyMethod = "detener")
    public BiometricProvider biometricProvider(AppProperties props) {
        BiometricProvider provider = BiometricProviderFactory.crear(props.getBiometric());
        provider.iniciar();
        return provider;
    }
}
