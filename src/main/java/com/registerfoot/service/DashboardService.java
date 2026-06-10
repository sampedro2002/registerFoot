package com.registerfoot.service;

import com.registerfoot.domain.entity.RegistroAlimentacion;
import com.registerfoot.dto.DashboardStatsDTO;
import com.registerfoot.repository.EmpleadoRepository;
import com.registerfoot.repository.RegistroAlimentacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    private final RegistroAlimentacionRepository registroRepo;
    private final EmpleadoRepository empleadoRepo;

    public DashboardService(RegistroAlimentacionRepository registroRepo,
                            EmpleadoRepository empleadoRepo) {
        this.registroRepo = registroRepo;
        this.empleadoRepo = empleadoRepo;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO estadisticas() {
        LocalDate hoy = LocalDate.now();
        List<RegistroAlimentacion> deHoy = registroRepo.findByFecha(hoy);

        BigDecimal valorHoy = deHoy.stream()
                .map(RegistroAlimentacion::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (Object[] row : registroRepo.resumenPorTipo(hoy)) {
            porTipo.put((String) row[0], (Long) row[1]);
        }

        List<DashboardStatsDTO.SerieDia> serie = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = hoy.minusDays(i);
            serie.add(new DashboardStatsDTO.SerieDia(d.format(DIA),
                    registroRepo.contarPorFecha(d)));
        }

        long activos = empleadoRepo.findAll().stream()
                .filter(e -> e.getEstado().name().equals("ACTIVO"))
                .count();

        return new DashboardStatsDTO(
                deHoy.size(), deHoy.size(), valorHoy, activos, porTipo, serie);
    }
}
