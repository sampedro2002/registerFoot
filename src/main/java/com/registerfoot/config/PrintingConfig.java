package com.registerfoot.config;

import com.registerfoot.printing.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Publica el {@link TicketPrinter} activo segun la configuracion. */
@Configuration
public class PrintingConfig {

    @Bean
    public TicketPrinter ticketPrinter(AppProperties props, QrCodeGenerator qr) {
        AppProperties.Printing cfg = props.getPrinting();
        String backend = cfg.getBackend() == null ? "MOCK" : cfg.getBackend().trim().toUpperCase();
        return switch (backend) {
            case "ESC_POS"    -> new EscPosTicketPrinter(cfg);
            case "JAVA_PRINT" -> new JavaPrintTicketPrinter(cfg, qr);
            default            -> new MockTicketPrinter(cfg);
        };
    }
}
