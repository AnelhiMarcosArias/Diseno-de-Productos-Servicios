package com.utp.sistema_feminicidio.dto;

import java.time.LocalDateTime;

public record AuditoriaResponse(Long id, String actor, String accion, String recurso,
        String referencia, LocalDateTime fechaEvento) {}
