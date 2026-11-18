package com.utp.sistema_feminicidio.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record SimulacionRequest(@NotNull Map<String, RespuestaExperimental> respuestas) {}
