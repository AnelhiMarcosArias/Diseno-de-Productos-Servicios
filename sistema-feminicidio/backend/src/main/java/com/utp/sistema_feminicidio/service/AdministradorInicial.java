package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.model.RolUsuario;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdministradorInicial implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;
    private final String name;
    private final String surname;

    public AdministradorInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            @Value("${AUTH_BOOTSTRAP_USERNAME:}") String username,
            @Value("${AUTH_BOOTSTRAP_EMAIL:}") String email,
            @Value("${AUTH_BOOTSTRAP_PASSWORD:}") String password,
            @Value("${AUTH_BOOTSTRAP_NAME:Administradora}") String name,
            @Value("${AUTH_BOOTSTRAP_SURNAME:Inicial}") String surname) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            log.info("No se creó una cuenta inicial. Configura AUTH_BOOTSTRAP_USERNAME, AUTH_BOOTSTRAP_EMAIL y AUTH_BOOTSTRAP_PASSWORD para habilitarla.");
            return;
        }
        if (usuarioRepository.existsByUsuario(username) || usuarioRepository.existsByCorreo(email)) {
            log.info("La cuenta inicial ya existe; no se realizaron cambios en usuarios.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setUsuario(username);
        admin.setContrasena(passwordEncoder.encode(password));
        admin.setNombre(name);
        admin.setApellido(surname);
        admin.setCorreo(email);
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setEstado(true);
        admin.setFechaCreacion(LocalDateTime.now());
        usuarioRepository.save(admin);
        log.info("Se creó la cuenta inicial de administración configurada para desarrollo.");
    }
}
