package com.utp.sistema_feminicidio.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utp.sistema_feminicidio.dto.MlServiceStatusResponse;
import com.utp.sistema_feminicidio.service.MlServiceStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MlServiceStatusControllerTest {
    @Mock MlServiceStatusService service;
    @InjectMocks MlServiceStatusController controller;

    @Test
    void exposesOperationalStatusWithoutInventingRiskResult() throws Exception {
        when(service.consultarEstado()).thenReturn(new MlServiceStatusResponse(
                true, true, false, false, "retrospective ENDES D107 only",
                "Estado operativo del servicio; no constituye una evaluación de riesgo."));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(get("/api/system/ml-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceReachable").value(true))
                .andExpect(jsonPath("$.healthContractValid").value(true))
                .andExpect(jsonPath("$.modelLoaded").value(false))
                .andExpect(jsonPath("$.predictionEndpointEnabled").value(false))
                .andExpect(jsonPath("$.experimentScope").value("retrospective ENDES D107 only"));
    }
}
