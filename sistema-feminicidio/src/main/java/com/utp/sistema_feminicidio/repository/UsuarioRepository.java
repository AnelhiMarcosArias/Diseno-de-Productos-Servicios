package com.utp.sistema_feminicidio.repository;

import com.utp.sistema_feminicidio.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
    boolean existsByCorreo(String correo);
}
