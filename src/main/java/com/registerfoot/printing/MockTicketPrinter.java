package com.registerfoot.printing;

import com.registerfoot.config.AppProperties;
import com.registerfoot.domain.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Impresora simulada: escribe el ticket en el log. Util sin hardware. */
public class MockTicketPrinter extends AbstractTicketPrinter {

    private static final Logger log = LoggerFactory.getLogger(MockTicketPrinter.class);

    public MockTicketPrinter(AppProperties.Printing cfg) { super(cfg); }

    @Override
    public String backend() { return "MOCK"; }

    @Override
    public void imprimir(Ticket ticket) {
        log.info("\n===== IMPRESION SIMULADA =====\n{}\n==============================",
                previsualizar(ticket));
    }
}
