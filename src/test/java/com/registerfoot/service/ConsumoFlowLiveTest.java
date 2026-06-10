package com.registerfoot.service;

import com.registerfoot.domain.enums.EstadoEmpleado;
import com.registerfoot.dto.ConsumoResultadoDTO;
import com.registerfoot.dto.EmpleadoDTO;
import com.registerfoot.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración del flujo biométrico contra MySQL real. Valida el
 * límite diario por categoría: NORMAL (1/día) y ESPECIAL (2/día).
 * Solo se ejecuta si hay credenciales de BD (variable DB_USER) y requiere un
 * horario que cubra la hora actual.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USER", matches = ".+")
class ConsumoFlowLiveTest {

    @Autowired EmpleadoService empleadoService;
    @Autowired ConsumoService consumoService;
    @Autowired TicketRepository ticketRepo;

    @Test
    void normalConsumeUnaVezYSeRechazaLaSegunda() {
        String codigo = "IT-N-" + System.currentTimeMillis();
        crearEmpleado(codigo, 1L, "NORMAL"); // límite 1

        ConsumoResultadoDTO r1 = consumoService.procesar(codigo, "TEST");
        assertTrue(r1.aprobado(), "El primer consumo debió aprobarse. Motivo: " + r1.motivo());
        assertNotNull(r1.ticket());
        assertTrue(ticketRepo.findByNumero(r1.ticket().numero()).isPresent(),
                "El ticket debió persistirse");

        ConsumoResultadoDTO r2 = consumoService.procesar(codigo, "TEST");
        assertFalse(r2.aprobado(), "El segundo consumo de un NORMAL debe rechazarse");
        assertTrue(r2.motivo().toLowerCase().contains("límite"),
                "El motivo debe indicar límite. Fue: " + r2.motivo());
    }

    @Test
    void especialPuedeConsumirDosVecesYSeRechazaLaTercera() {
        String codigo = "IT-E-" + System.currentTimeMillis();
        crearEmpleado(codigo, 2L, "ESPECIAL"); // límite 2

        assertTrue(consumoService.procesar(codigo, "TEST").aprobado(), "1er consumo especial");
        assertTrue(consumoService.procesar(codigo, "TEST").aprobado(), "2do consumo especial");

        ConsumoResultadoDTO r3 = consumoService.procesar(codigo, "TEST");
        assertFalse(r3.aprobado(), "El tercer consumo de un ESPECIAL (límite 2) debe rechazarse");
    }

    private void crearEmpleado(String codigo, Long categoriaId, String categoriaNombre) {
        empleadoService.crear(new EmpleadoDTO(
                null, codigo, codigo, "Test", "Integracion", "QA",
                1L, null, categoriaId, categoriaNombre, EstadoEmpleado.ACTIVO));
    }
}
