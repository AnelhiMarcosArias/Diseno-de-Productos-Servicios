package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Cuenta no encontrada"));
        return User.withUsername(usuario.getUsuario())
                .password(usuario.getContrasena())
                .authorities("ROLE_" + usuario.getRol().name())
                .disabled(!Boolean.TRUE.equals(usuario.getEstado()))
                .build();
    }
}
