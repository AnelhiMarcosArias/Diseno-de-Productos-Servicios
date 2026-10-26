package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.NivelRiesgo;
import java.math.BigDecimal;
import tools.jackson.databind.JsonNode;

/** Contrato de salida esperado; representa una estimación, no un hecho confirmado. */
public record PrediccionResponse(NivelRiesgo nivelRiesgo, BigDecimal probabilidadEstimada,
        String modeloUtilizado, JsonNode factoresRelevantes, String recomendaciones,
        boolean requiereRevisionProfesional, String observaciones) {}
