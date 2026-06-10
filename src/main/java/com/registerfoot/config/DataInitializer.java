package com.registerfoot.config;

import com.registerfoot.domain.entity.Usuario;
import com.registerfoot.domain.enums.Rol;
import com.registerfoot.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Crea los usuarios iniciales (uno por rol) si no existen, garantizando un
 * hash BCrypt correcto. Credenciales por defecto -> ver README.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public ApplicationRunner seedUsuarios(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            crear(repo, encoder, "admin",      "admin123",      "Administrador General", Rol.ADMINISTRADOR);
            crear(repo, encoder, "supervisor", "supervisor123", "Supervisor Turno",      Rol.SUPERVISOR);
            crear(repo, encoder, "operador",   "operador123",   "Operador Punto",        Rol.OPERADOR);
            crear(repo, encoder, "auditor",    "auditor123",    "Auditor Interno",       Rol.AUDITOR);
        };
    }

    private void crear(UsuarioRepository repo, PasswordEncoder encoder,
                       String user, String pass, String nombre, Rol rol) {
        if (repo.existsByUsername(user)) return;
        repo.save(Usuario.builder()
                .username(user)
                .passwordHash(encoder.encode(pass))
                .nombreCompleto(nombre)
                .email(user + "@registerfoot.local")
                .rol(rol)
                .activo(true)
                .build());
        log.info("Usuario inicial creado: {} ({})", user, rol);
    }
}
