package com.utp.sistema_feminicidio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/** Registro atómico de un caso y sus respuestas actuales. */
public record RegistroCasoRequest(
        @NotNull @Valid CasoRequest caso,
        @NotNull @Valid DetalleCasoRequest cuestionario,
        Map<String, RespuestaExperimental> respuestasV1V12) {
    public RegistroCasoRequest(CasoRequest caso, DetalleCasoRequest cuestionario) {
        this(caso, cuestionario, null);
    }
}
