package com.registerfoot.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Indicadores agregados para el dashboard. */
public record DashboardStatsDTO(
        long consumosHoy,
        long ticketsHoy,
        BigDecimal valorHoy,
        long empleadosActivos,
        Map<String, Long> consumosPorTipo,
        List<SerieDia> ultimosDias
) {
    public record SerieDia(String fecha, long consumos) {}
}
