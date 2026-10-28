package com.utp.sistema_feminicidio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Registro atómico de un caso y sus respuestas actuales. */
public record RegistroCasoRequest(
        @NotNull @Valid CasoRequest caso,
        @NotNull @Valid DetalleCasoRequest cuestionario) {}
