package com.registerfoot.repository;

import com.registerfoot.domain.entity.HorarioComida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioComidaRepository extends JpaRepository<HorarioComida, Long> {
    List<HorarioComida> findByTipoComidaIdAndActivoTrue(Long tipoComidaId);
    List<HorarioComida> findByActivoTrue();
}
