package com.registerfoot.repository;

import com.registerfoot.domain.entity.TipoComida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoComidaRepository extends JpaRepository<TipoComida, Long> {
    List<TipoComida> findByActivoTrue();
    boolean existsByCodigo(String codigo);
}
