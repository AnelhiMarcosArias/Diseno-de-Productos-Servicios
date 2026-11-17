package com.utp.sistema_feminicidio.dto;

import java.time.LocalDateTime;

public record DetalleCasoResponse(Long idDetalleCaso, Long casoId,
        Boolean variable1, Boolean variable2, Boolean variable3, Boolean variable4,
        Boolean variable5, Boolean variable6, Boolean variable7, Boolean variable8,
        Boolean variable9, Boolean variable10, Boolean variable11, Boolean variable12,
        LocalDateTime fechaActualizacion) {}
