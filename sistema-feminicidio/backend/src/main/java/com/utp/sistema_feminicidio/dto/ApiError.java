package com.utp.sistema_feminicidio.dto;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant fecha, int status, String error, String mensaje,
        Map<String, String> detalles) {}
