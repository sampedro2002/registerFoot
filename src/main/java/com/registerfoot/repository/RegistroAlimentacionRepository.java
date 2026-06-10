package com.registerfoot.repository;

import com.registerfoot.domain.entity.RegistroAlimentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RegistroAlimentacionRepository extends JpaRepository<RegistroAlimentacion, Long> {

    List<RegistroAlimentacion> findByFecha(LocalDate fecha);

    List<RegistroAlimentacion> findByFechaBetween(LocalDate desde, LocalDate hasta);

    @Query("SELECT COUNT(r) FROM RegistroAlimentacion r WHERE r.fecha = :fecha")
    long contarPorFecha(@Param("fecha") LocalDate fecha);

    @Query("""
            SELECT r.tipoComida.nombre, COUNT(r)
            FROM RegistroAlimentacion r
            WHERE r.fecha = :fecha
            GROUP BY r.tipoComida.nombre
            """)
    List<Object[]> resumenPorTipo(@Param("fecha") LocalDate fecha);
}
