package com.registerfoot.printing;

import com.registerfoot.domain.entity.Ticket;

/**
 * Contrato de impresion desacoplado del hardware (Dependency Inversion).
 * Implementaciones: ESC/POS termica, Java Print Service y un mock.
 */
public interface TicketPrinter {

    /** Identifica el backend (ESC_POS, JAVA_PRINT, MOCK). */
    String backend();

    /** Imprime el ticket. Lanza excepcion si la impresion falla. */
    void imprimir(Ticket ticket);

    /** Devuelve la representacion textual del ticket (para previsualizar). */
    String previsualizar(Ticket ticket);
}
