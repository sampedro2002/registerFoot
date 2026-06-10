package com.registerfoot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Mapea el bloque {@code registerfoot.*} de application.yml. */
@Component
@ConfigurationProperties(prefix = "registerfoot")
@Getter @Setter
public class AppProperties {

    private final Biometric biometric = new Biometric();
    private final Printing printing = new Printing();
    private final Security security = new Security();

    @Getter @Setter
    public static class Biometric {
        private String provider = "MOCK";
        private long pollingMillis = 800;
        private String deviceIp = "192.168.1.201";
        private int devicePort = 4370;
    }

    @Getter @Setter
    public static class Printing {
        private String backend = "MOCK";
        private String printerName = "";
        private int charPerLine = 42;
        private String companyName = "RegisterFoot S.A.S.";
        private String companyNit = "900.123.456-7";
    }

    @Getter @Setter
    public static class Security {
        private int sessionTimeoutMinutes = 30;
        private int maxLoginAttempts = 5;
    }
}
