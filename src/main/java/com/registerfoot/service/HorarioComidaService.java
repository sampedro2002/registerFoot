package com.registerfoot.service;

import com.registerfoot.domain.entity.HorarioComida;
import com.registerfoot.domain.entity.TipoComida;
import com.registerfoot.dto.HorarioComidaDTO;
import com.registerfoot.exception.RecursoNoEncontradoException;
import com.registerfoot.repository.HorarioComidaRepository;
import com.registerfoot.repository.TipoComidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class HorarioComidaService {

    private final HorarioComidaRepository repo;
    private final TipoComidaRepository tipoRepo;
    private final AuditoriaService auditoria;

    public HorarioComidaService(HorarioComidaRepository repo, TipoComidaRepository tipoRepo,
                                AuditoriaService auditoria) {
        this.repo = repo;
        this.tipoRepo = tipoRepo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<HorarioComidaDTO> listar() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    /** Determina si la hora dada cae dentro de alguna ventana activa del tipo. */
    @Transactional(readOnly = true)
    public boolean horaPermitida(Long tipoComidaId, LocalTime hora) {
        return repo.findByTipoComidaIdAndActivoTrue(tipoComidaId).stream()
                .anyMatch(h -> h.contiene(hora));
    }

    @Transactional(readOnly = true)
    public boolean tieneHorarioActivo(Long tipoComidaId) {
        return !repo.findByTipoComidaIdAndActivoTrue(tipoComidaId).isEmpty();
    }

    public HorarioComidaDTO crear(HorarioComidaDTO dto) {
        TipoComida tipo = tipoRepo.findById(dto.tipoComidaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("TipoComida", dto.tipoComidaId()));
        HorarioComida h = HorarioComida.builder()
                .tipoComida(tipo)
                .horaInicio(dto.horaInicio())
                .horaFin(dto.horaFin())
                .activo(dto.activo())
                .build();
        h = repo.save(h);
        auditoria.ok("CREAR", "HorarioComida", String.valueOf(h.getId()), tipo.getNombre());
        return toDto(h);
    }

    public HorarioComidaDTO actualizar(Long id, HorarioComidaDTO dto) {
        HorarioComida h = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("HorarioComida", id));
        TipoComida tipo = tipoRepo.findById(dto.tipoComidaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("TipoComida", dto.tipoComidaId()));
        h.setTipoComida(tipo);
        h.setHoraInicio(dto.horaInicio());
        h.setHoraFin(dto.horaFin());
        h.setActivo(dto.activo());
        auditoria.ok("EDITAR", "HorarioComida", String.valueOf(id), tipo.getNombre());
        return toDto(h);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
        auditoria.ok("ELIMINAR", "HorarioComida", String.valueOf(id), null);
    }

    private HorarioComidaDTO toDto(HorarioComida h) {
        return new HorarioComidaDTO(h.getId(), h.getTipoComida().getId(),
                h.getTipoComida().getNombre(), h.getHoraInicio(), h.getHoraFin(), h.isActivo());
    }
}
