package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioAdminRequest(
        @NotBlank @Size(max = 50) String usuario,
        @NotBlank @Size(min = 12, max = 72) String contrasena,
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String apellido,
        @NotBlank @Email @Size(max = 254) String correo,
        @NotNull RolUsuario rol) {}
