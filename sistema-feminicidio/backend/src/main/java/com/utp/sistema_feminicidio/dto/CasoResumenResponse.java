package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.EstadoCaso;
import java.time.LocalDateTime;

/** Datos mínimos usados por el historial y las confirmaciones del registro. */
public record CasoResumenResponse(Long idCaso, String codigoCaso,
        LocalDateTime fechaRegistro, EstadoCaso estadoCaso) {}
