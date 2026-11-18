package com.utp.sistema_feminicidio.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utp.sistema_feminicidio.dto.RespuestaExperimental;
import com.utp.sistema_feminicidio.dto.SimulacionRequest;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.model.Evaluacion;
import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.model.EstadoSimulacion;
import com.utp.sistema_feminicidio.model.ResultadoSimulacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.utp.sistema_feminicidio.repository.EvaluacionRepository;
import com.utp.sistema_feminicidio.repository.ResultadoSimulacionRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class SimulacionServiceTest {
    @Mock EvaluacionRepository evaluaciones;
    @Mock ResultadoSimulacionRepository simulaciones;
    @Mock SimulacionMlClient mlClient;
    private SimulacionService service;

    @BeforeEach
    void setUp() {
        service = new SimulacionService(evaluaciones, simulaciones, mlClient, JsonMapper.builder().build());
    }

    @Test
    void noEjecutaSiElUsuarioNoEsPropietarioDeLaEvaluacion() {
        when(evaluaciones.findByIdEvaluacionAndCaso_Usuario_Usuario(9L, "profesional-a"))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.ejecutar(9L, new SimulacionRequest(respuestasValidas()), "profesional-a"));
        verify(mlClient, never()).inferir(org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void rechazaRespuestasAusentesSinLlamarAlModelo() {
        Evaluacion evaluation = new Evaluacion();
        evaluation.setIdEvaluacion(9L);
        Caso ownedCase = new Caso();
        ownedCase.setIdCaso(3L);
        evaluation.setCaso(ownedCase);
        when(evaluaciones.findByIdEvaluacionAndCaso_Usuario_Usuario(9L, "profesional-a"))
                .thenReturn(Optional.of(evaluation));
        Map<String, RespuestaExperimental> incomplete = respuestasValidas();
        incomplete.remove("V12");

        assertThrows(IllegalArgumentException.class,
                () -> service.ejecutar(9L, new SimulacionRequest(incomplete), "profesional-a"));
        verify(mlClient, never()).inferir(org.mockito.ArgumentMatchers.anyMap());
        verify(simulaciones, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void mantieneLaCategoriaNoEstoySeguraEnElContratoDeEntrada() {
        Evaluacion evaluation = new Evaluacion();
        evaluation.setIdEvaluacion(9L);
        Caso ownedCase = new Caso();
        ownedCase.setIdCaso(3L);
        evaluation.setCaso(ownedCase);
        when(evaluaciones.findByIdEvaluacionAndCaso_Usuario_Usuario(9L, "profesional-a"))
                .thenReturn(Optional.of(evaluation));
        Map<String, RespuestaExperimental> answers = respuestasValidas();
        answers.put("V1", RespuestaExperimental.NO_ESTOY_SEGURA);
        // The validation must accept the explicit uncertain option without mapping it to NO.
        // Inference/persistence behavior is exercised through the FastAPI contract tests.
        when(simulaciones.findByEvaluacion_IdEvaluacion(9L)).thenReturn(Optional.empty());
        when(simulaciones.saveAndFlush(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mlClient.inferir(org.mockito.ArgumentMatchers.anyMap()))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("test outage"));

        var response = service.ejecutar(9L, new SimulacionRequest(answers), "profesional-a");

        org.junit.jupiter.api.Assertions.assertEquals(RespuestaExperimental.NO_ESTOY_SEGURA,
                response.respuestas().get("V1"));
        org.junit.jupiter.api.Assertions.assertEquals(com.utp.sistema_feminicidio.model.EstadoSimulacion.ERROR_TECNICO,
                response.estado());
    }

    @Test
    void calculaYPersisteElIndiceExperimentalConVersionYFecha() {
        Evaluacion evaluation = new Evaluacion();
        evaluation.setIdEvaluacion(10L);
        Caso ownedCase = new Caso();
        ownedCase.setIdCaso(4L);
        evaluation.setCaso(ownedCase);
        when(evaluaciones.findByIdEvaluacionAndCaso_Usuario_Usuario(10L, "profesional-a"))
                .thenReturn(Optional.of(evaluation));
        when(simulaciones.findByEvaluacion_IdEvaluacion(10L)).thenReturn(Optional.empty());
        when(simulaciones.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mlClient.inferir(org.mockito.ArgumentMatchers.anyMap())).thenReturn(
                new SimulacionMlClient.InferenceResult(new BigDecimal("73.25"),
                        "synthetic-v1-v12-xgb-demo-v1", "synthetic_academic_demonstration"));

        var response = service.ejecutar(10L, new SimulacionRequest(respuestasValidas()), "profesional-a");

        org.junit.jupiter.api.Assertions.assertEquals(EstadoSimulacion.COMPLETADA, response.estado());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("73.25"), response.indiceExperimental());
        org.junit.jupiter.api.Assertions.assertEquals("synthetic-v1-v12-xgb-demo-v1", response.modeloVersion());
        org.junit.jupiter.api.Assertions.assertNotNull(response.fechaGeneracion());
        org.mockito.ArgumentCaptor<ResultadoSimulacion> savedSimulation =
                org.mockito.ArgumentCaptor.forClass(ResultadoSimulacion.class);
        verify(simulaciones).save(savedSimulation.capture());
        org.junit.jupiter.api.Assertions.assertEquals(EstadoSimulacion.COMPLETADA, savedSimulation.getValue().getEstado());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("73.25"),
                savedSimulation.getValue().getIndiceExperimental());
        org.junit.jupiter.api.Assertions.assertNotNull(savedSimulation.getValue().getFechaGeneracion());
        verify(evaluaciones).save(evaluation);
    }

    @Test
    void unaRepeticionDeUnaEvaluacionCompletadaDevuelveElResultadoSinRecalcular() {
        Evaluacion evaluation = new Evaluacion();
        evaluation.setIdEvaluacion(9L);
        Caso ownedCase = new Caso();
        ownedCase.setIdCaso(3L);
        evaluation.setCaso(ownedCase);
        when(evaluaciones.findByIdEvaluacionAndCaso_Usuario_Usuario(9L, "profesional-a"))
                .thenReturn(Optional.of(evaluation));
        var answers = respuestasValidas();
        ResultadoSimulacion saved = new ResultadoSimulacion();
        saved.setEvaluacion(evaluation);
        saved.setRespuestas(JsonMapper.builder().build().valueToTree(new java.util.TreeMap<>(answers)));
        saved.setEstado(EstadoSimulacion.COMPLETADA);
        saved.setIndiceExperimental(new BigDecimal("64.25"));
        saved.setModeloVersion("synthetic-v1-v12-xgb-demo-v1");
        saved.setFechaGeneracion(LocalDateTime.now());
        when(simulaciones.findByEvaluacion_IdEvaluacion(9L)).thenReturn(Optional.of(saved));

        var response = service.ejecutar(9L, new SimulacionRequest(answers), "profesional-a");

        org.junit.jupiter.api.Assertions.assertEquals(EstadoSimulacion.COMPLETADA, response.estado());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("64.25"), response.indiceExperimental());
        verify(mlClient, never()).inferir(org.mockito.ArgumentMatchers.anyMap());
        verify(simulaciones, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }

    private Map<String, RespuestaExperimental> respuestasValidas() {
        Map<String, RespuestaExperimental> answers = new HashMap<>();
        for (int i = 1; i <= 12; i++) answers.put("V" + i, RespuestaExperimental.NO);
        return answers;
    }
}
