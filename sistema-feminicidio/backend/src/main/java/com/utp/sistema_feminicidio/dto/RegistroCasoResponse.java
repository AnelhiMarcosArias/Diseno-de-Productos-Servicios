package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.EstadoCaso;
import java.time.LocalDateTime;

/** Confirmación mínima; no devuelve datos personales ni una predicción. */
public record RegistroCasoResponse(Long idCaso, String codigoCaso, EstadoCaso estadoCaso,
        Long idEvaluacion, String estadoEvaluacion, LocalDateTime fechaRegistro, boolean reintento) {}
