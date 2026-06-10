package com.registerfoot.printing;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.registerfoot.config.AppProperties;
import com.registerfoot.domain.entity.Ticket;
import com.registerfoot.exception.RegisterFootException;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Impresion en impresoras termicas mediante comandos ESC/POS
 * (libreria escpos-coffee). Imprime encabezado, datos del ticket, valor y un
 * codigo QR nativo del comando ESC/POS.
 */
public class EscPosTicketPrinter extends AbstractTicketPrinter {

    private static final DateTimeFormatter FH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public EscPosTicketPrinter(AppProperties.Printing cfg) { super(cfg); }

    @Override
    public String backend() { return "ESC_POS"; }

    @Override
    public void imprimir(Ticket t) {
        PrintService service = resolverImpresora();
        try (EscPos escpos = new EscPos(new PrinterOutputStream(service))) {

            Style titulo = new Style().setBold(true)
                    .setJustification(EscPosConst.Justification.Center);
            Style centro = new Style().setJustification(EscPosConst.Justification.Center);
            Style normal = new Style().setJustification(EscPosConst.Justification.Left_Default);

            escpos.writeLF(titulo, cfg.getCompanyName())
                  .writeLF(centro, "NIT " + cfg.getCompanyNit())
                  .writeLF(centro, "TICKET DE ALIMENTACION")
                  .writeLF(linea(cfg.getCharPerLine()))
                  .writeLF(normal, "No.    : " + t.getNumero())
                  .writeLF("Fecha  : " + t.getFechaHora().format(FH))
                  .writeLF("Comida : " + t.getTipoComida().getNombre())
                  .writeLF("Emple. : " + t.getEmpleado().getNombreCompleto())
                  .writeLF("Doc.   : " + t.getEmpleado().getDocumento())
                  .writeLF("Conces.: " + t.getConcesion().getNombre())
                  .writeLF(linea(cfg.getCharPerLine()));

            Style valor = new Style().setBold(true).setFontSize(Style.FontSize._2, Style.FontSize._1);
            escpos.writeLF(valor, "VALOR: " + MONEDA.format(t.getValor()))
                  .writeLF(linea(cfg.getCharPerLine()));

            // Codigo QR nativo ESC/POS
            QRCode qr = new QRCode();
            qr.setSize(6).setJustification(EscPosConst.Justification.Center);
            escpos.write(qr, t.getQrPayload());

            if (t.getReimpresiones() > 0) {
                escpos.writeLF(centro, "*** REIMPRESION #" + t.getReimpresiones() + " ***");
            }
            escpos.writeLF(centro, "Gracias por su consumo")
                  .feed(3)
                  .cut(EscPos.CutMode.FULL);

        } catch (Exception e) {
            throw new RegisterFootException("Error imprimiendo en ESC/POS: " + e.getMessage(), e);
        }
    }

    private PrintService resolverImpresora() {
        String nombre = cfg.getPrinterName();
        if (nombre == null || nombre.isBlank()) {
            PrintService def = PrintServiceLookup.lookupDefaultPrintService();
            if (def == null) {
                throw new RegisterFootException("No hay impresora por defecto en el sistema.");
            }
            return def;
        }
        PrintService service = PrinterOutputStream.getPrintServiceByName(nombre);
        if (service == null) {
            throw new RegisterFootException("Impresora no encontrada: " + nombre);
        }
        return service;
    }
}
