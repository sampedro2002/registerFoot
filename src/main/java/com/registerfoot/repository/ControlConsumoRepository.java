package com.registerfoot.repository;

import com.registerfoot.domain.entity.ControlConsumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ControlConsumoRepository extends JpaRepository<ControlConsumo, Long> {

    /** Cuántos consumos lleva el empleado ese día (para validar el límite). */
    long countByEmpleadoIdAndFecha(Long empleadoId, LocalDate fecha);
}
