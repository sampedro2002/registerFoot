package com.registerfoot.service;

import com.registerfoot.domain.entity.CategoriaPersonal;
import com.registerfoot.dto.CategoriaPersonalDTO;
import com.registerfoot.exception.RecursoNoEncontradoException;
import com.registerfoot.repository.CategoriaPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaPersonalService {

    private final CategoriaPersonalRepository repo;
    private final AuditoriaService auditoria;

    public CategoriaPersonalService(CategoriaPersonalRepository repo, AuditoriaService auditoria) {
        this.repo = repo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<CategoriaPersonalDTO> listar() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaPersonalDTO> listarActivas() {
        return repo.findByActivoTrue().stream().map(this::toDto).toList();
    }

    public CategoriaPersonalDTO crear(CategoriaPersonalDTO dto) {
        CategoriaPersonal c = CategoriaPersonal.builder()
                .codigo(dto.codigo()).nombre(dto.nombre())
                .limiteDiario(dto.limiteDiario()).activo(dto.activo())
                .build();
        c = repo.save(c);
        auditoria.ok("CREAR", "CategoriaPersonal", String.valueOf(c.getId()),
                c.getNombre() + " (límite " + c.getLimiteDiario() + ")");
        return toDto(c);
    }

    public CategoriaPersonalDTO actualizar(Long id, CategoriaPersonalDTO dto) {
        CategoriaPersonal c = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("CategoriaPersonal", id));
        c.setCodigo(dto.codigo());
        c.setNombre(dto.nombre());
        c.setLimiteDiario(dto.limiteDiario());
        c.setActivo(dto.activo());
        auditoria.ok("EDITAR", "CategoriaPersonal", String.valueOf(id),
                c.getNombre() + " (límite " + c.getLimiteDiario() + ")");
        return toDto(c);
    }

    public void eliminar(Long id) {
        CategoriaPersonal c = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("CategoriaPersonal", id));
        c.setActivo(false);
        auditoria.ok("ELIMINAR", "CategoriaPersonal", String.valueOf(id), c.getNombre());
    }

    private CategoriaPersonalDTO toDto(CategoriaPersonal c) {
        return new CategoriaPersonalDTO(c.getId(), c.getCodigo(), c.getNombre(),
                c.getLimiteDiario(), c.isActivo());
    }
}
