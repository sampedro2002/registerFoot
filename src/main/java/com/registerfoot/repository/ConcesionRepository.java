package com.registerfoot.repository;

import com.registerfoot.domain.entity.Concesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcesionRepository extends JpaRepository<Concesion, Long> {
    List<Concesion> findByActivoTrue();
    boolean existsByCodigo(String codigo);
}
