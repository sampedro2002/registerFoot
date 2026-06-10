package com.registerfoot.service;

import com.registerfoot.domain.entity.RegistroAlimentacion;
import com.registerfoot.domain.entity.Ticket;
import com.registerfoot.domain.enums.EstadoTicket;
import com.registerfoot.dto.TicketDTO;
import com.registerfoot.exception.RecursoNoEncontradoException;
import com.registerfoot.printing.TicketPrinter;
import com.registerfoot.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);
    private static final DateTimeFormatter NUM_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final TicketRepository repo;
    private final TicketPrinter printer;
    private final AuditoriaService auditoria;

    public TicketService(TicketRepository repo, TicketPrinter printer, AuditoriaService auditoria) {
        this.repo = repo;
        this.printer = printer;
        this.auditoria = auditoria;
    }

    /** Crea el ticket asociado a un registro de consumo (numero unico + QR). */
    public Ticket generar(RegistroAlimentacion reg) {
        String numero = generarNumero();
        String qr = construirQr(numero, reg);
        Ticket t = Ticket.builder()
                .numero(numero)
                .registroId(reg.getId())
                .empleado(reg.getEmpleado())
                .tipoComida(reg.getTipoComida())
                .concesion(reg.getConcesion())
                .fechaHora(LocalDateTime.of(reg.getFecha(), reg.getHora()))
                .valor(reg.getValor())
                .qrPayload(qr)
                .estado(EstadoTicket.GENERADO)
                .build();
        return repo.save(t);
    }

    /** Imprime el ticket y marca el estado. No relanza si la impresion falla. */
    public boolean imprimir(Ticket t) {
        try {
            printer.imprimir(t);
            t.setImpreso(true);
            t.setEstado(EstadoTicket.IMPRESO);
            repo.save(t);
            auditoria.ok("IMPRIMIR", "Ticket", t.getNumero(), "Impresion " + printer.backend());
            return true;
        } catch (Exception e) {
            log.error("Fallo impresion ticket {}: {}", t.getNumero(), e.getMessage());
            auditoria.registrar("IMPRIMIR", "Ticket", t.getNumero(), e.getMessage(), "ERROR");
            return false;
        }
    }

    /** Reimprime un ticket existente incrementando el contador. */
    public boolean reimprimir(String numero) {
        Ticket t = repo.findByNumero(numero)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ticket", numero));
        t.setReimpresiones(t.getReimpresiones() + 1);
        repo.save(t);
        boolean ok = imprimir(t);
        auditoria.ok("REIMPRIMIR", "Ticket", numero, "Reimpresion #" + t.getReimpresiones());
        return ok;
    }

    @Transactional(readOnly = true)
    public String previsualizar(String numero) {
        Ticket t = repo.findByNumero(numero)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ticket", numero));
        return printer.previsualizar(t);
    }

    @Transactional(readOnly = true)
    public List<TicketDTO> listarDelDia(java.time.LocalDate dia) {
        return repo.findByFechaHoraBetweenOrderByFechaHoraDesc(
                        dia.atStartOfDay(), dia.atTime(23, 59, 59))
                .stream().map(this::toDto).toList();
    }

    public TicketDTO toDto(Ticket t) {
        return new TicketDTO(t.getId(), t.getNumero(),
                t.getEmpleado().getNombreCompleto(), t.getEmpleado().getDocumento(),
                t.getTipoComida().getNombre(), t.getConcesion().getNombre(),
                t.getFechaHora(), t.getValor(), t.getQrPayload(),
                t.isImpreso(), t.getReimpresiones(), t.getEstado().name());
    }

    private String generarNumero() {
        String base = "RF-" + LocalDateTime.now().format(NUM_FMT)
                + "-" + ThreadLocalRandom.current().nextInt(1000, 9999);
        // garantiza unicidad ante colision improbable
        return repo.findByNumero(base).isPresent() ? generarNumero() : base;
    }

    private String construirQr(String numero, RegistroAlimentacion reg) {
        return String.join("|", "RF", numero,
                reg.getEmpleado().getDocumento(),
                reg.getTipoComida().getCodigo(),
                reg.getValor().toPlainString());
    }
}
