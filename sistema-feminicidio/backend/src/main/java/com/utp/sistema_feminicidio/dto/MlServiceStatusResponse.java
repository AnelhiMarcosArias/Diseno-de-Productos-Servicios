package com.utp.sistema_feminicidio.dto;

/** Operational status only; it never represents a risk assessment. */
public record MlServiceStatusResponse(
        boolean serviceReachable,
        boolean healthContractValid,
        boolean modelLoaded,
        boolean predictionEndpointEnabled,
        String experimentScope,
        String message) {
}
