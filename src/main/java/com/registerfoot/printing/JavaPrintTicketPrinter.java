package com.registerfoot.printing;

import com.registerfoot.config.AppProperties;
import com.registerfoot.domain.entity.Ticket;
import com.registerfoot.exception.RegisterFootException;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;

/**
 * Impresion mediante Java Print Service (java.awt.print). Renderiza el ticket
 * como grafico (texto monoespaciado + imagen QR), util para impresoras no
 * ESC/POS o para impresion a PDF virtual del SO.
 */
public class JavaPrintTicketPrinter extends AbstractTicketPrinter {

    private final QrCodeGenerator qr;

    public JavaPrintTicketPrinter(AppProperties.Printing cfg, QrCodeGenerator qr) {
        super(cfg);
        this.qr = qr;
    }

    @Override
    public String backend() { return "JAVA_PRINT"; }

    @Override
    public void imprimir(Ticket ticket) {
        String texto = previsualizar(ticket);
        BufferedImage qrImg = qr.imagen(ticket.getQrPayload(), 120);

        PrinterJob job = PrinterJob.getPrinterJob();
        PrintService service = resolverImpresora();
        try {
            if (service != null) job.setPrintService(service);
            job.setPrintable(new TicketPrintable(texto, qrImg));
            job.print();
        } catch (PrinterException e) {
            throw new RegisterFootException("Error en Java Print Service: " + e.getMessage(), e);
        }
    }

    private PrintService resolverImpresora() {
        String nombre = cfg.getPrinterName();
        if (nombre == null || nombre.isBlank()) {
            return PrintServiceLookup.lookupDefaultPrintService();
        }
        for (PrintService s : PrintServiceLookup.lookupPrintServices(null, null)) {
            if (s.getName().equalsIgnoreCase(nombre)) return s;
        }
        throw new RegisterFootException("Impresora no encontrada: " + nombre);
    }

    /** Dibuja el ticket en la pagina. */
    private record TicketPrintable(String texto, BufferedImage qr) implements Printable {
        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) {
            if (pageIndex > 0) return NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
            int y = 12;
            for (String linea : texto.split("\n")) {
                g2.drawString(linea, 5, y);
                y += 12;
            }
            g2.drawImage(qr, 5, y + 5, null);
            return PAGE_EXISTS;
        }
    }
}
