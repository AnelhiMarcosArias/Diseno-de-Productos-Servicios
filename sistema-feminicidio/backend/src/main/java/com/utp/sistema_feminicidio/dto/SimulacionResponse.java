package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.EstadoSimulacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record SimulacionResponse(Long evaluacionId, Long casoId, EstadoSimulacion estado,
        Map<String, RespuestaExperimental> respuestas, BigDecimal indiceExperimental,
        String nombreIndice, String modeloVersion, String tipoModelo, LocalDateTime fechaGeneracion,
        List<String> respuestasInciertas, List<String> senalesCriticas, String advertencia,
        String mensaje) {}
