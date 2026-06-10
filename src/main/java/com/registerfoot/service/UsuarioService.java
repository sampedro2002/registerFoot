package com.registerfoot.service;

import com.registerfoot.domain.entity.Usuario;
import com.registerfoot.dto.UsuarioDTO;
import com.registerfoot.exception.RecursoNoEncontradoException;
import com.registerfoot.exception.RegisterFootException;
import com.registerfoot.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder, AuditoriaService auditoria) {
        this.repo = repo;
        this.encoder = encoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public UsuarioDTO crear(UsuarioDTO dto) {
        if (repo.existsByUsername(dto.username()))
            throw new RegisterFootException("El usuario ya existe: " + dto.username());
        if (dto.password() == null || dto.password().isBlank())
            throw new RegisterFootException("La contrasena es obligatoria.");

        Usuario u = Usuario.builder()
                .username(dto.username())
                .passwordHash(encoder.encode(dto.password()))
                .nombreCompleto(dto.nombreCompleto())
                .email(dto.email())
                .rol(dto.rol())
                .activo(dto.activo())
                .build();
        u = repo.save(u);
        auditoria.ok("CREAR", "Usuario", String.valueOf(u.getId()), u.getUsername());
        return toDto(u);
    }

    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        u.setNombreCompleto(dto.nombreCompleto());
        u.setEmail(dto.email());
        u.setRol(dto.rol());
        u.setActivo(dto.activo());
        if (dto.password() != null && !dto.password().isBlank()) {
            u.setPasswordHash(encoder.encode(dto.password()));
        }
        auditoria.ok("EDITAR", "Usuario", String.valueOf(id), u.getUsername());
        return toDto(u);
    }

    public void eliminar(Long id) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        u.setActivo(false);
        auditoria.ok("ELIMINAR", "Usuario", String.valueOf(id), u.getUsername());
    }

    private UsuarioDTO toDto(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getUsername(), u.getNombreCompleto(),
                u.getEmail(), u.getRol(), u.isActivo(), null);
    }
}
