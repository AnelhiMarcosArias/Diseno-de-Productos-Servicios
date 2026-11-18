package com.utp.sistema_feminicidio.dto;

/** Operational status only; it never represents a risk assessment. */
public record MlServiceStatusResponse(
        boolean serviceReachable,
        boolean healthContractValid,
        boolean modelLoaded,
        boolean predictionEndpointEnabled,
        String experimentScope,
        String message,
        boolean syntheticModelLoaded,
        String syntheticModelVersion) {
    public MlServiceStatusResponse(boolean serviceReachable, boolean healthContractValid,
            boolean modelLoaded, boolean predictionEndpointEnabled, String experimentScope, String message) {
        this(serviceReachable, healthContractValid, modelLoaded, predictionEndpointEnabled,
                experimentScope, message, false, "unavailable");
    }
}
