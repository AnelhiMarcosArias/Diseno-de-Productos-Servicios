package com.utp.sistema_feminicidio.dto;

import java.time.LocalDateTime;

public record EvaluacionResponse(Long idEvaluacion, Long casoId, Long usuarioEvaluadorId,
        LocalDateTime fechaEvaluacion, String tipoEvaluacion, String versionModelo,
        String estadoEvaluacion, String observaciones, LocalDateTime fechaResultadoML) {}
