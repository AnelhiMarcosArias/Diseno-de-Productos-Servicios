package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.MlServiceStatusResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

@Service
public class MlServiceStatusService {
    private static final String EXPERIMENTAL_SCOPE = "retrospective ENDES D107 only";
    private final RestClient restClient;

    public MlServiceStatusService(RestClient mlHealthRestClient) {
        this.restClient = mlHealthRestClient;
    }

    public MlServiceStatusResponse consultarEstado() {
        try {
            JsonNode payload = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new RestClientException("ML health endpoint returned an error status");
                    })
                    .body(JsonNode.class);
            if (!validContract(payload)) {
                return new MlServiceStatusResponse(true, false, false, false, "unknown",
                        "El servicio respondió con un contrato de salud no reconocido; las predicciones siguen deshabilitadas.");
            }

            boolean modelLoaded = payload.get("model_loaded").booleanValue();
            boolean predictionEnabled = payload.get("prediction_endpoint_enabled").booleanValue();
            String scope = payload.get("experiment_scope").stringValue();
            boolean syntheticLoaded = payload.has("synthetic_model_loaded")
                    && payload.get("synthetic_model_loaded").isBoolean()
                    && payload.get("synthetic_model_loaded").booleanValue();
            String syntheticVersion = payload.has("synthetic_model_version")
                    ? payload.get("synthetic_model_version").stringValue() : "unavailable";
            return new MlServiceStatusResponse(true, true, modelLoaded, predictionEnabled, scope,
                    "Servicio de simulación académica; no constituye una evaluación de riesgo real.",
                    syntheticLoaded, syntheticVersion);
        } catch (RestClientException | IllegalArgumentException exception) {
            return new MlServiceStatusResponse(false, false, false, false, "unknown",
                    "El microservicio ML no está disponible. No se ejecutan predicciones.");
        }
    }

    private boolean validContract(JsonNode payload) {
        return payload != null
                && payload.has("status")
                && "ok".equals(payload.get("status").stringValue())
                && payload.has("model_loaded")
                && payload.get("model_loaded").isBoolean()
                && payload.has("prediction_endpoint_enabled")
                && payload.get("prediction_endpoint_enabled").isBoolean()
                && payload.has("experiment_scope")
                && EXPERIMENTAL_SCOPE.equals(payload.get("experiment_scope").stringValue());
    }
}
