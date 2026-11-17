package com.utp.sistema_feminicidio.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.utp.sistema_feminicidio.dto.MlServiceStatusResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MlServiceStatusServiceTest {
    private static final String HEALTH_URL = "http://ml.test/health";

    @Test
    void reportsReachableButKeepsPredictionDisabledWhenExperimentalServiceIsHealthy() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ml.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(HEALTH_URL)).andRespond(withSuccess("""
                {"status":"ok","model_loaded":false,"prediction_endpoint_enabled":false,
                 "experiment_scope":"retrospective ENDES D107 only"}
                """, MediaType.APPLICATION_JSON));

        MlServiceStatusResponse status = new MlServiceStatusService(builder.build()).consultarEstado();

        assertTrue(status.serviceReachable());
        assertTrue(status.healthContractValid());
        assertFalse(status.modelLoaded());
        assertFalse(status.predictionEndpointEnabled());
        server.verify();
    }

    @Test
    void failsClosedWhenHealthContractDoesNotMatch() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ml.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(HEALTH_URL)).andRespond(withSuccess("""
                {"status":"ok","model_loaded":true,"prediction_endpoint_enabled":true,
                 "experiment_scope":"unknown"}
                """, MediaType.APPLICATION_JSON));

        MlServiceStatusResponse status = new MlServiceStatusService(builder.build()).consultarEstado();

        assertTrue(status.serviceReachable());
        assertFalse(status.healthContractValid());
        assertFalse(status.modelLoaded());
        assertFalse(status.predictionEndpointEnabled());
        server.verify();
    }

    @Test
    void reportsUnavailableWithoutLeakingClientException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ml.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(HEALTH_URL)).andRespond(withServerError());

        MlServiceStatusResponse status = new MlServiceStatusService(builder.build()).consultarEstado();

        assertFalse(status.serviceReachable());
        assertFalse(status.healthContractValid());
        assertFalse(status.predictionEndpointEnabled());
        server.verify();
    }
}
