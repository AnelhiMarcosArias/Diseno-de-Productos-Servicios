package com.utp.sistema_feminicidio.dto;

public record ResumenDashboardResponse(long casosAutorizados, long evaluacionesRegistradas,
        long simulacionesCompletadas, long evaluacionesPendientes, long erroresTecnicos) {}
