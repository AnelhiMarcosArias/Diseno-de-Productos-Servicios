package com.utp.sistema_feminicidio.dto;

import java.time.LocalDateTime;

public record EvaluacionResponse(Long idEvaluacion, Long casoId,
        LocalDateTime fechaEvaluacion, String tipoEvaluacion, String versionModelo,
        String estadoEvaluacion, LocalDateTime fechaResultadoML, boolean tieneResultado) {}
