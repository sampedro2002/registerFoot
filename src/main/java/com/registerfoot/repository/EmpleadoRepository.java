package com.registerfoot.repository;

import com.registerfoot.domain.entity.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByCodigoBiometrico(String codigoBiometrico);

    Optional<Empleado> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    boolean existsByCodigoBiometrico(String codigoBiometrico);

    /** Busqueda rapida por documento, nombres o apellidos. */
    @Query("""
            SELECT e FROM Empleado e
            WHERE LOWER(e.documento)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.nombres)    LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.apellidos)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.codigoBiometrico) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Empleado> buscar(@Param("q") String q, Pageable pageable);
}
