package com.registerfoot.printing;

import com.registerfoot.config.AppProperties;
import com.registerfoot.domain.entity.Ticket;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Logica comun de maquetacion del ticket en texto monoespaciado. */
public abstract class AbstractTicketPrinter implements TicketPrinter {

    protected final AppProperties.Printing cfg;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    protected AbstractTicketPrinter(AppProperties.Printing cfg) {
        this.cfg = cfg;
    }

    @Override
    public String previsualizar(Ticket t) {
        int w = cfg.getCharPerLine();
        StringBuilder sb = new StringBuilder();
        sb.append(center(cfg.getCompanyName(), w)).append('\n');
        sb.append(center("NIT " + cfg.getCompanyNit(), w)).append('\n');
        sb.append(center("TICKET DE ALIMENTACION", w)).append('\n');
        sb.append(linea(w)).append('\n');
        sb.append("No.    : ").append(t.getNumero()).append('\n');
        sb.append("Fecha  : ").append(t.getFechaHora().format(FECHA)).append('\n');
        sb.append("Hora   : ").append(t.getFechaHora().format(HORA)).append('\n');
        sb.append("Comida : ").append(t.getTipoComida().getNombre()).append('\n');
        sb.append("Emple. : ").append(t.getEmpleado().getNombreCompleto()).append('\n');
        sb.append("Doc.   : ").append(t.getEmpleado().getDocumento()).append('\n');
        sb.append("Conces.: ").append(t.getConcesion().getNombre()).append('\n');
        sb.append(linea(w)).append('\n');
        sb.append("VALOR  : ").append(MONEDA.format(t.getValor())).append('\n');
        sb.append(linea(w)).append('\n');
        sb.append(center("QR: " + t.getQrPayload(), w)).append('\n');
        if (t.getReimpresiones() > 0) {
            sb.append(center("*** REIMPRESION #" + t.getReimpresiones() + " ***", w)).append('\n');
        }
        sb.append(center("Gracias por su consumo", w)).append('\n');
        return sb.toString();
    }

    protected String center(String s, int w) {
        if (s.length() >= w) return s.substring(0, w);
        int pad = (w - s.length()) / 2;
        return " ".repeat(pad) + s;
    }

    protected String linea(int w) {
        return "-".repeat(w);
    }
}
