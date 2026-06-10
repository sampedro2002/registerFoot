package com.registerfoot.repository;

import com.registerfoot.domain.entity.Impresora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImpresoraRepository extends JpaRepository<Impresora, Long> {
    List<Impresora> findByActivoTrue();
    Optional<Impresora> findFirstByPorDefectoTrueAndActivoTrue();
}
