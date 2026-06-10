package com.registerfoot.domain;

import com.registerfoot.domain.entity.HorarioComida;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class HorarioComidaTest {

    private HorarioComida ventana(String ini, String fin) {
        return HorarioComida.builder()
                .horaInicio(LocalTime.parse(ini))
                .horaFin(LocalTime.parse(fin))
                .activo(true)
                .build();
    }

    @Test
    void incluyeHoraDentroDeLaVentana() {
        HorarioComida almuerzo = ventana("11:30", "14:30");
        assertTrue(almuerzo.contiene(LocalTime.parse("12:00")));
        assertTrue(almuerzo.contiene(LocalTime.parse("11:30")), "limite inferior inclusivo");
        assertTrue(almuerzo.contiene(LocalTime.parse("14:30")), "limite superior inclusivo");
    }

    @Test
    void excluyeHoraFueraDeLaVentana() {
        HorarioComida desayuno = ventana("06:00", "09:30");
        assertFalse(desayuno.contiene(LocalTime.parse("10:00")));
        assertFalse(desayuno.contiene(LocalTime.parse("05:59")));
    }
}
