package com.registerfoot.repository;

import com.registerfoot.domain.entity.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
