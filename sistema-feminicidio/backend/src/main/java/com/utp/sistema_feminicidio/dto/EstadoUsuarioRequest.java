package com.utp.sistema_feminicidio.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoUsuarioRequest(@NotNull Boolean activo) {}
