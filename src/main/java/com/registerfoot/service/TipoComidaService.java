package com.registerfoot.service;

import com.registerfoot.domain.entity.TipoComida;
import com.registerfoot.dto.TipoComidaDTO;
import com.registerfoot.exception.RecursoNoEncontradoException;
import com.registerfoot.repository.TipoComidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoComidaService {

    private final TipoComidaRepository repo;
    private final AuditoriaService auditoria;

    public TipoComidaService(TipoComidaRepository repo, AuditoriaService auditoria) {
        this.repo = repo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<TipoComidaDTO> listar() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TipoComidaDTO> listarActivos() {
        return repo.findByActivoTrue().stream().map(this::toDto).toList();
    }

    public TipoComidaDTO crear(TipoComidaDTO dto) {
        TipoComida t = TipoComida.builder()
                .codigo(dto.codigo()).nombre(dto.nombre())
                .valor(dto.valor()).activo(dto.activo())
                .build();
        t = repo.save(t);
        auditoria.ok("CREAR", "TipoComida", String.valueOf(t.getId()), t.getNombre());
        return toDto(t);
    }

    public TipoComidaDTO actualizar(Long id, TipoComidaDTO dto) {
        TipoComida t = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("TipoComida", id));
        t.setCodigo(dto.codigo());
        t.setNombre(dto.nombre());
        t.setValor(dto.valor());
        t.setActivo(dto.activo());
        auditoria.ok("EDITAR", "TipoComida", String.valueOf(id), t.getNombre());
        return toDto(t);
    }

    public void eliminar(Long id) {
        TipoComida t = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("TipoComida", id));
        t.setActivo(false);
        auditoria.ok("ELIMINAR", "TipoComida", String.valueOf(id), t.getNombre());
    }

    private TipoComidaDTO toDto(TipoComida t) {
        return new TipoComidaDTO(t.getId(), t.getCodigo(), t.getNombre(), t.getValor(), t.isActivo());
    }
}
