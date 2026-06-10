package com.registerfoot.service;

import com.registerfoot.domain.entity.Concesion;
import com.registerfoot.dto.ConcesionDTO;
import com.registerfoot.exception.RecursoNoEncontradoException;
import com.registerfoot.repository.ConcesionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConcesionService {

    private final ConcesionRepository repo;
    private final AuditoriaService auditoria;

    public ConcesionService(ConcesionRepository repo, AuditoriaService auditoria) {
        this.repo = repo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<ConcesionDTO> listar() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ConcesionDTO> listarActivas() {
        return repo.findByActivoTrue().stream().map(this::toDto).toList();
    }

    public ConcesionDTO crear(ConcesionDTO dto) {
        Concesion c = Concesion.builder()
                .codigo(dto.codigo()).nombre(dto.nombre()).nit(dto.nit())
                .contacto(dto.contacto()).telefono(dto.telefono()).activo(dto.activo())
                .build();
        c = repo.save(c);
        auditoria.ok("CREAR", "Concesion", String.valueOf(c.getId()), c.getNombre());
        return toDto(c);
    }

    public ConcesionDTO actualizar(Long id, ConcesionDTO dto) {
        Concesion c = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Concesion", id));
        c.setCodigo(dto.codigo());
        c.setNombre(dto.nombre());
        c.setNit(dto.nit());
        c.setContacto(dto.contacto());
        c.setTelefono(dto.telefono());
        c.setActivo(dto.activo());
        auditoria.ok("EDITAR", "Concesion", String.valueOf(id), c.getNombre());
        return toDto(c);
    }

    public void eliminar(Long id) {
        Concesion c = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Concesion", id));
        c.setActivo(false); // borrado logico para preservar integridad referencial
        auditoria.ok("ELIMINAR", "Concesion", String.valueOf(id), c.getNombre());
    }

    private ConcesionDTO toDto(Concesion c) {
        return new ConcesionDTO(c.getId(), c.getCodigo(), c.getNombre(), c.getNit(),
                c.getContacto(), c.getTelefono(), c.isActivo());
    }
}
