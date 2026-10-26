package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.NivelRiesgo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tools.jackson.databind.JsonNode;

/** La probabilidad y el nivel son estimaciones del modelo, no hechos verificados. */
public record ResultadoEvaluacionResponse(Long idResultadoEvaluacion, Long evaluacionId,
        NivelRiesgo nivelRiesgo, BigDecimal probabilidadEstimada, String modeloUtilizado,
        LocalDateTime fechaGeneracion, JsonNode factoresRelevantes, String recomendaciones,
        boolean requiereRevisionProfesional, String observaciones) {}
