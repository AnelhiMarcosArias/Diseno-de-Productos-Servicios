package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.RespuestaExperimental;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

@Component
public class SimulacionMlClient {
    private final RestClient client;

    public SimulacionMlClient(RestClient mlInferenceRestClient) {
        this.client = mlInferenceRestClient;
    }

    public InferenceResult inferir(Map<String, RespuestaExperimental> answers) {
        Map<String, String> encoded = new TreeMap<>();
        answers.forEach((key, value) -> encoded.put(key, value.name()));
        try {
            JsonNode response = client.post().uri("/experimental/simulate-v1-v12")
                    .body(Map.of("answers", encoded)).retrieve().body(JsonNode.class);
            if (response == null || !response.has("index") || !response.get("index").isNumber()
                    || !response.has("model_version") || !response.has("model_type")) {
                throw new RestClientException("Invalid synthetic inference response");
            }
            BigDecimal index = response.get("index").decimalValue();
            if (index.compareTo(BigDecimal.ZERO) < 0 || index.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new RestClientException("Synthetic index outside allowed range");
            }
            return new InferenceResult(index, response.get("model_version").stringValue(),
                    response.get("model_type").stringValue());
        } catch (RestClientException exception) {
            throw exception;
        }
    }

    public record InferenceResult(BigDecimal index, String modelVersion, String modelType) {}
}
