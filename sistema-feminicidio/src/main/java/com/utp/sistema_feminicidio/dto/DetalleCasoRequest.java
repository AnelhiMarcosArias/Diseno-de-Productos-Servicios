package com.utp.sistema_feminicidio.dto;

import jakarta.validation.constraints.NotNull;

/** Las variables conservan nombres provisionales y tipo booleano del modelo actual. */
public record DetalleCasoRequest(Boolean variable1, Boolean variable2, Boolean variable3, Boolean variable4,
        Boolean variable5, Boolean variable6, Boolean variable7, Boolean variable8,
        Boolean variable9, Boolean variable10, Boolean variable11, Boolean variable12) {}
