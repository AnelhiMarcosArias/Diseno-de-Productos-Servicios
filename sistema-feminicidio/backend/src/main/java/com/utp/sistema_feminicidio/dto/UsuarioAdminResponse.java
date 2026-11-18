package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.RolUsuario;
import java.time.LocalDateTime;

public record UsuarioAdminResponse(Long id, String usuario, String nombre, String apellido,
        String correo, RolUsuario rol, Boolean activo, LocalDateTime fechaCreacion,
        LocalDateTime ultimoAcceso) {}
