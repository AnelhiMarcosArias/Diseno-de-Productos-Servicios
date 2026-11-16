package com.utp.sistema_feminicidio.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utp.sistema_feminicidio.config.SecurityConfig;
import com.utp.sistema_feminicidio.dto.MlServiceStatusResponse;
import com.utp.sistema_feminicidio.service.MlServiceStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(MlServiceStatusController.class)
@Import(SecurityConfig.class)
class MlServiceStatusSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean MlServiceStatusService service;

    @Test
    void rejectsAnonymousStatusRequests() throws Exception {
        mvc.perform(get("/api/system/ml-service"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "paciente", roles = "PACIENTE")
    void rejectsPatientRoleForOperationalStatus() throws Exception {
        mvc.perform(get("/api/system/ml-service"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "profesional", roles = "PROFESIONAL")
    void returnsStatusToAuthenticatedSession() throws Exception {
        when(service.consultarEstado()).thenReturn(new MlServiceStatusResponse(
                true, true, false, false, "retrospective ENDES D107 only",
                "Estado operativo del servicio; no constituye una evaluación de riesgo."));

        mvc.perform(get("/api/system/ml-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelLoaded").value(false))
                .andExpect(jsonPath("$.predictionEndpointEnabled").value(false));
    }
}
