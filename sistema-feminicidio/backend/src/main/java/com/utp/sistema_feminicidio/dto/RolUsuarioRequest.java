package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.RolUsuario;
import jakarta.validation.constraints.NotNull;

public record RolUsuarioRequest(@NotNull RolUsuario rol) {}
