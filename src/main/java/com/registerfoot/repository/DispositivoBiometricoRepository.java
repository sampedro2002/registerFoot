package com.registerfoot.repository;

import com.registerfoot.domain.entity.DispositivoBiometrico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispositivoBiometricoRepository extends JpaRepository<DispositivoBiometrico, Long> {
    List<DispositivoBiometrico> findByActivoTrue();
}
