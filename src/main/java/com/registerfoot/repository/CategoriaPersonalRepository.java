package com.registerfoot.repository;

import com.registerfoot.domain.entity.CategoriaPersonal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaPersonalRepository extends JpaRepository<CategoriaPersonal, Long> {
    List<CategoriaPersonal> findByActivoTrue();
    Optional<CategoriaPersonal> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
