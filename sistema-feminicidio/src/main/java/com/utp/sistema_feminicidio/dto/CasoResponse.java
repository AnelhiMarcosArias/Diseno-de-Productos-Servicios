package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.EstadoCaso;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Respuesta de uso interno/autenticado; contiene datos personales del caso. */
public record CasoResponse(Long idCaso, String codigoCaso, LocalDateTime fechaRegistro,
        String nombrePersona, String apellidoPersona, LocalDate fechaNacimiento, Integer edad,
        String departamento, String provincia, String distrito, String direccion,
        EstadoCaso estadoCaso) {}
