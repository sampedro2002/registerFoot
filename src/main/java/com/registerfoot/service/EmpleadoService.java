package com.registerfoot.service;

import com.registerfoot.domain.entity.CategoriaPersonal;
import com.registerfoot.domain.entity.Concesion;
import com.registerfoot.domain.entity.Empleado;
import com.registerfoot.domain.enums.EstadoEmpleado;
import com.registerfoot.dto.EmpleadoDTO;
import com.registerfoot.exception.RecursoNoEncontradoException;
import com.registerfoot.exception.RegisterFootException;
import com.registerfoot.repository.CategoriaPersonalRepository;
import com.registerfoot.repository.ConcesionRepository;
import com.registerfoot.repository.EmpleadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmpleadoService {

    private final EmpleadoRepository repo;
    private final ConcesionRepository concesionRepo;
    private final CategoriaPersonalRepository categoriaRepo;
    private final AuditoriaService auditoria;

    public EmpleadoService(EmpleadoRepository repo, ConcesionRepository concesionRepo,
                           CategoriaPersonalRepository categoriaRepo, AuditoriaService auditoria) {
        this.repo = repo;
        this.concesionRepo = concesionRepo;
        this.categoriaRepo = categoriaRepo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listar() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    /** Busqueda rapida paginada por documento/nombre/codigo biometrico. */
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> buscar(String q, int pagina, int tamano) {
        if (q == null || q.isBlank()) return listar();
        Page<Empleado> page = repo.buscar(q.trim(),
                PageRequest.of(pagina, tamano, Sort.by("apellidos", "nombres")));
        return page.getContent().stream().map(this::toDto).toList();
    }

    public EmpleadoDTO crear(EmpleadoDTO dto) {
        if (repo.existsByDocumento(dto.documento()))
            throw new RegisterFootException("Ya existe un empleado con documento " + dto.documento());
        if (repo.existsByCodigoBiometrico(dto.codigoBiometrico()))
            throw new RegisterFootException("Codigo biometrico en uso: " + dto.codigoBiometrico());

        Empleado e = Empleado.builder()
                .codigoBiometrico(dto.codigoBiometrico())
                .documento(dto.documento())
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .cargo(dto.cargo())
                .concesion(concesion(dto.concesionId()))
                .categoria(categoria(dto.categoriaId()))
                .estado(dto.estado() == null ? EstadoEmpleado.ACTIVO : dto.estado())
                .build();
        e = repo.save(e);
        auditoria.ok("CREAR", "Empleado", String.valueOf(e.getId()), e.getNombreCompleto());
        return toDto(e);
    }

    public EmpleadoDTO actualizar(Long id, EmpleadoDTO dto) {
        Empleado e = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado", id));
        e.setCodigoBiometrico(dto.codigoBiometrico());
        e.setDocumento(dto.documento());
        e.setNombres(dto.nombres());
        e.setApellidos(dto.apellidos());
        e.setCargo(dto.cargo());
        e.setConcesion(concesion(dto.concesionId()));
        e.setCategoria(categoria(dto.categoriaId()));
        if (dto.estado() != null) e.setEstado(dto.estado());
        auditoria.ok("EDITAR", "Empleado", String.valueOf(id), e.getNombreCompleto());
        return toDto(e);
    }

    public void eliminar(Long id) {
        Empleado e = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado", id));
        e.setEstado(EstadoEmpleado.INACTIVO); // borrado logico
        auditoria.ok("ELIMINAR", "Empleado", String.valueOf(id), e.getNombreCompleto());
    }

    private Concesion concesion(Long id) {
        return concesionRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Concesion", id));
    }

    /** Categoría por id; null si no se especifica (el empleado queda con límite 1). */
    private CategoriaPersonal categoria(Long id) {
        if (id == null) return null;
        return categoriaRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("CategoriaPersonal", id));
    }

    private EmpleadoDTO toDto(Empleado e) {
        return new EmpleadoDTO(e.getId(), e.getCodigoBiometrico(), e.getDocumento(),
                e.getNombres(), e.getApellidos(), e.getCargo(),
                e.getConcesion().getId(), e.getConcesion().getNombre(),
                e.getCategoria() == null ? null : e.getCategoria().getId(),
                e.getCategoria() == null ? "(sin categoría)" : e.getCategoria().getNombre(),
                e.getEstado());
    }
}
