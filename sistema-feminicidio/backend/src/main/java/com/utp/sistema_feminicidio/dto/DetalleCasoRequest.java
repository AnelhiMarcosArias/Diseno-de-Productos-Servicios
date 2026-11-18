package com.utp.sistema_feminicidio.dto;

/**
 * Esquema Boolean legado: null no distingue una respuesta incierta de una ausente.
 * La categoría exacta SI/NO/NO_ESTOY_SEGURA se conserva en resultado_simulacion.respuestas.
 */
public record DetalleCasoRequest(Boolean variable1, Boolean variable2, Boolean variable3, Boolean variable4,
        Boolean variable5, Boolean variable6, Boolean variable7, Boolean variable8,
        Boolean variable9, Boolean variable10, Boolean variable11, Boolean variable12) {}
