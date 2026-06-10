package com.registerfoot.service;

import com.registerfoot.domain.entity.*;
import com.registerfoot.domain.enums.EstadoEmpleado;
import com.registerfoot.dto.ConsumoResultadoDTO;
import com.registerfoot.exception.ConsumoRechazadoException;
import com.registerfoot.exception.ConsumoRechazadoException.Motivo;
import com.registerfoot.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Orquesta el FLUJO BIOMETRICO completo:
 *   codigo -> empleado -> estado -> horario -> consumo previo ->
 *   registrar -> generar ticket -> imprimir -> auditar.
 *
 * Es el caso de uso central del sistema (Application Service).
 */
@Service
public class ConsumoService {

    private static final Logger log = LoggerFactory.getLogger(ConsumoService.class);

    private final EmpleadoRepository empleadoRepo;
    private final HorarioComidaRepository horarioRepo;
    private final ControlConsumoRepository controlRepo;
    private final RegistroAlimentacionRepository registroRepo;
    private final TicketService ticketService;
    private final AuditoriaService auditoria;

    /** Referencia al propio bean (proxy) para que @Transactional aplique. */
    @Autowired @Lazy
    private ConsumoService self;

    public ConsumoService(EmpleadoRepository empleadoRepo, HorarioComidaRepository horarioRepo,
                          ControlConsumoRepository controlRepo, RegistroAlimentacionRepository registroRepo,
                          TicketService ticketService, AuditoriaService auditoria) {
        this.empleadoRepo = empleadoRepo;
        this.horarioRepo = horarioRepo;
        this.controlRepo = controlRepo;
        this.registroRepo = registroRepo;
        this.ticketService = ticketService;
        this.auditoria = auditoria;
    }

    /**
     * Procesa un evento biometrico identificado por su codigo. Devuelve el
     * resultado (aprobado con ticket, o rechazado con motivo). No lanza
     * excepcion al exterior: encapsula el rechazo en el DTO para la UI.
     */
    public ConsumoResultadoDTO procesar(String codigoBiometrico, String origen) {
        try {
            // invocacion via proxy para que la transaccion (y su rollback) apliquen
            return self.procesarInterno(codigoBiometrico, origen);
        } catch (ConsumoRechazadoException e) {
            auditoria.rechazado("CONSUMO", "Empleado", codigoBiometrico, e.getMessage());
            return ConsumoResultadoDTO.rechazado(codigoBiometrico, e.getMessage());
        }
    }

    @Transactional
    public ConsumoResultadoDTO procesarInterno(String codigoBiometrico, String origen) {
        // (2) Buscar empleado
        Empleado empleado = empleadoRepo.findByCodigoBiometrico(codigoBiometrico)
                .orElseThrow(() -> new ConsumoRechazadoException(
                        Motivo.EMPLEADO_NO_ENCONTRADO,
                        "Empleado no encontrado para el codigo " + codigoBiometrico));

        // (3) Verificar estado activo
        if (empleado.getEstado() != EstadoEmpleado.ACTIVO) {
            throw new ConsumoRechazadoException(Motivo.EMPLEADO_INACTIVO,
                    "El empleado esta " + empleado.getEstado() + " y no puede consumir.");
        }

        // (4) Verificar horario permitido -> determina el tipo de comida vigente
        LocalTime ahora = LocalTime.now();
        HorarioComida horario = horarioVigente(ahora)
                .orElseThrow(() -> new ConsumoRechazadoException(Motivo.FUERA_DE_HORARIO,
                        "No hay ninguna comida habilitada a las " + ahora.withNano(0)));
        TipoComida tipo = horario.getTipoComida();
        if (!tipo.isActivo()) {
            throw new ConsumoRechazadoException(Motivo.TIPO_COMIDA_INACTIVO,
                    "El tipo de comida " + tipo.getNombre() + " esta inactivo.");
        }

        LocalDate hoy = LocalDate.now();

        // (5) Verificar límite diario según la CATEGORÍA del empleado
        //     (total de consumos en el día, sin importar el tipo de comida).
        long consumosHoy = controlRepo.countByEmpleadoIdAndFecha(empleado.getId(), hoy);
        int limite = empleado.getLimiteDiario();
        if (consumosHoy >= limite) {
            throw new ConsumoRechazadoException(Motivo.LIMITE_DIARIO_ALCANZADO,
                    empleado.getNombreCompleto() + " alcanzó su límite de " + limite
                    + " consumo(s) por día.");
        }

        // (6) Registrar transaccion
        RegistroAlimentacion reg = RegistroAlimentacion.builder()
                .empleado(empleado)
                .tipoComida(tipo)
                .concesion(empleado.getConcesion())
                .fecha(hoy)
                .hora(ahora.withNano(0))
                .valor(tipo.getValor())
                .origen(origen == null ? "BIOMETRICO" : origen)
                .build();
        reg = registroRepo.save(reg);

        // Candado por secuencia: UNIQUE(empleado, fecha, secuencia) garantiza
        // que dos consumos concurrentes no excedan el límite (uno fallará).
        try {
            controlRepo.save(ControlConsumo.builder()
                    .empleadoId(empleado.getId())
                    .tipoComidaId(tipo.getId())
                    .fecha(hoy)
                    .secuencia((int) consumosHoy + 1)
                    .registroId(reg.getId())
                    .build());
        } catch (DataIntegrityViolationException dup) {
            throw new ConsumoRechazadoException(Motivo.LIMITE_DIARIO_ALCANZADO,
                    "Límite diario alcanzado (consumo concurrente) para "
                            + empleado.getNombreCompleto() + ".");
        }

        // (7) Generar ticket
        Ticket ticket = ticketService.generar(reg);

        // (8) Imprimir automaticamente
        ticketService.imprimir(ticket);

        // (9) Auditar
        auditoria.ok("CONSUMO", "Ticket", ticket.getNumero(),
                empleado.getNombreCompleto() + " - " + tipo.getNombre());

        log.info("Consumo aprobado: {} - {} - ticket {}",
                empleado.getNombreCompleto(), tipo.getNombre(), ticket.getNumero());

        return ConsumoResultadoDTO.aprobado(empleado.getNombreCompleto(),
                ticketService.toDto(ticket));
    }

    /** Primer horario activo cuya ventana contiene la hora actual. */
    private Optional<HorarioComida> horarioVigente(LocalTime hora) {
        return horarioRepo.findByActivoTrue().stream()
                .filter(h -> h.contiene(hora))
                .findFirst();
    }
}
