package com.registerfoot.security;

import com.registerfoot.config.AppProperties;
import com.registerfoot.domain.entity.Usuario;
import com.registerfoot.repository.UsuarioRepository;
import com.registerfoot.service.AuditoriaService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de autenticacion para la UI de escritorio. Gestiona el login,
 * el bloqueo por intentos fallidos, la sesion en memoria y la auditoria.
 */
@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarioRepo;
    private final AuditoriaService auditoria;
    private final AppProperties props;

    private Usuario usuarioActual; // sesion del operador en el equipo

    public AuthService(AuthenticationManager authManager, UsuarioRepository usuarioRepo,
                       AuditoriaService auditoria, AppProperties props) {
        this.authManager = authManager;
        this.usuarioRepo = usuarioRepo;
        this.auditoria = auditoria;
        this.props = props;
    }

    @Transactional
    public Usuario login(String username, String password) {
        Usuario u = usuarioRepo.findByUsername(username).orElse(null);
        if (u != null && u.getIntentosFallidos() >= props.getSecurity().getMaxLoginAttempts()) {
            auditoria.rechazado("LOGIN", "Usuario", username, "Cuenta bloqueada por intentos");
            throw new LockedException("Cuenta bloqueada por multiples intentos fallidos.");
        }
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(auth);

            usuarioActual = u;
            u.setIntentosFallidos(0);
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepo.save(u);
            auditoria.ok("LOGIN", "Usuario", username, "Acceso exitoso");
            return u;
        } catch (AuthenticationException ex) {
            if (u != null) {
                u.setIntentosFallidos(u.getIntentosFallidos() + 1);
                usuarioRepo.save(u);
            }
            auditoria.rechazado("LOGIN", "Usuario", username, "Credenciales invalidas");
            throw new BadCredentialsException("Usuario o contrasena incorrectos.");
        }
    }

    public void logout() {
        if (usuarioActual != null) {
            auditoria.ok("LOGOUT", "Usuario", usuarioActual.getUsername(), "Cierre de sesion");
        }
        usuarioActual = null;
        SecurityContextHolder.clearContext();
    }

    public Usuario getUsuarioActual() { return usuarioActual; }

    public boolean tieneRol(String rol) {
        return usuarioActual != null && usuarioActual.getRol().name().equals(rol);
    }
}
