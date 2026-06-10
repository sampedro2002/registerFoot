package com.registerfoot.service;

import com.registerfoot.domain.entity.Auditoria;
import com.registerfoot.repository.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Registra y consulta la bitacora de auditoria. */
@Service
public class AuditoriaService {

    private final AuditoriaRepository repo;

    public AuditoriaService(AuditoriaRepository repo) { this.repo = repo; }

    /**
     * Registra una accion. Usa una transaccion nueva para que la auditoria
     * persista aunque la transaccion de negocio principal haga rollback.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String accion, String entidad, String entidadId,
                          String detalle, String resultado) {
        Auditoria a = Auditoria.builder()
                .usuario(usuarioActual())
                .accion(accion)
                .entidad(entidad)
                .entidadId(entidadId)
                .detalle(recortar(detalle))
                .resultado(resultado == null ? "OK" : resultado)
                .build();
        repo.save(a);
    }

    public void ok(String accion, String entidad, String entidadId, String detalle) {
        registrar(accion, entidad, entidadId, detalle, "OK");
    }

    public void rechazado(String accion, String entidad, String entidadId, String detalle) {
        registrar(accion, entidad, entidadId, detalle, "RECHAZADO");
    }

    @Transactional(readOnly = true)
    public Page<Auditoria> consultar(LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return repo.findByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta, pageable);
    }

    private String usuarioActual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private String recortar(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}
