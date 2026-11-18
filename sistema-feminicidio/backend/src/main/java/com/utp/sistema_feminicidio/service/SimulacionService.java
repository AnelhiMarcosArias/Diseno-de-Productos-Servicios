package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.RespuestaExperimental;
import com.utp.sistema_feminicidio.dto.SimulacionRequest;
import com.utp.sistema_feminicidio.dto.SimulacionResponse;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.model.EstadoSimulacion;
import com.utp.sistema_feminicidio.model.Evaluacion;
import com.utp.sistema_feminicidio.model.ResultadoSimulacion;
import com.utp.sistema_feminicidio.repository.EvaluacionRepository;
import com.utp.sistema_feminicidio.repository.ResultadoSimulacionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

@Service
public class SimulacionService {
    private static final String MODEL_TYPE = "synthetic_academic_demonstration";
    private static final String INDEX_NAME = "Índice experimental de señales identificadas";
    private static final String WARNING = "Simulación académica con datos y etiquetas sintéticos. No es diagnóstico, "
            + "probabilidad de feminicidio ni predicción de violencia futura. Requiere revisión profesional.";
    private final EvaluacionRepository evaluacionRepository;
    private final ResultadoSimulacionRepository simulacionRepository;
    private final SimulacionMlClient mlClient;
    private final ObjectMapper objectMapper;

    public SimulacionService(EvaluacionRepository evaluacionRepository,
            ResultadoSimulacionRepository simulacionRepository, SimulacionMlClient mlClient,
            ObjectMapper objectMapper) {
        this.evaluacionRepository = evaluacionRepository;
        this.simulacionRepository = simulacionRepository;
        this.mlClient = mlClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SimulacionResponse ejecutar(Long evaluacionId, SimulacionRequest request, String username) {
        Evaluacion evaluacion = evaluacionRepository.findByIdEvaluacionAndCaso_Usuario_Usuario(evaluacionId, username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluación", evaluacionId));
        Map<String, RespuestaExperimental> answers = validarYOrdenar(request.respuestas());
        var answersJson = objectMapper.valueToTree(answers);
        ResultadoSimulacion record = simulacionRepository.findByEvaluacion_IdEvaluacion(evaluacionId).orElse(null);
        if (record != null && !record.getRespuestas().equals(answersJson)) {
            throw new IllegalArgumentException("Esta evaluación ya está asociada a otras respuestas. Registra otra evaluación para corregirlas.");
        }
        if (record != null && record.getEstado() == EstadoSimulacion.COMPLETADA) {
            return respuesta(evaluacion, record, "Resultado experimental ya guardado.");
        }
        if (record == null) {
            record = new ResultadoSimulacion();
            record.setEvaluacion(evaluacion);
            record.setRespuestas(answersJson);
        }
        record.setEstado(EstadoSimulacion.PENDIENTE);
        record.setCodigoError(null);
        record = simulacionRepository.saveAndFlush(record);

        try {
            SimulacionMlClient.InferenceResult result = mlClient.inferir(answers);
            record.setIndiceExperimental(result.index());
            record.setModeloVersion(result.modelVersion());
            record.setFechaGeneracion(LocalDateTime.now());
            record.setEstado(EstadoSimulacion.COMPLETADA);
            record.setCodigoError(null);
            evaluacion.setEstadoEvaluacion("SIMULACION_COMPLETADA");
            evaluacion.setVersionModelo(result.modelVersion());
            evaluacion.setFechaResultadoML(record.getFechaGeneracion());
            simulacionRepository.save(record);
            evaluacionRepository.save(evaluacion);
            return respuesta(evaluacion, record, "Índice sintético calculado y guardado.");
        } catch (RestClientException | IllegalStateException exception) {
            // Keep the input and technical failure state so the same operation can be retried safely.
            record.setEstado(EstadoSimulacion.ERROR_TECNICO);
            record.setCodigoError("SERVICIO_ML_NO_DISPONIBLE");
            record.setIndiceExperimental(null);
            record.setFechaGeneracion(null);
            evaluacion.setEstadoEvaluacion("ERROR_TECNICO_ML");
            evaluacionRepository.save(evaluacion);
            simulacionRepository.save(record);
            return respuesta(evaluacion, record, "El servicio experimental no respondió. Las respuestas quedaron guardadas; puedes reintentar.");
        }
    }

    @Transactional(readOnly = true)
    public SimulacionResponse obtener(Long evaluacionId, String username) {
        Evaluacion evaluacion = evaluacionRepository.findByIdEvaluacionAndCaso_Usuario_Usuario(evaluacionId, username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluación", evaluacionId));
        ResultadoSimulacion record = simulacionRepository.findByEvaluacion_IdEvaluacion(evaluacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Simulación para evaluación", evaluacionId));
        return respuesta(evaluacion, record, record.getEstado() == EstadoSimulacion.COMPLETADA
                ? "Resultado experimental guardado." : "La evaluación requiere reintentar el servicio experimental.");
    }

    private Map<String, RespuestaExperimental> validarYOrdenar(Map<String, RespuestaExperimental> answers) {
        if (answers == null || answers.size() != 12) throw new IllegalArgumentException("Debes enviar las 12 respuestas.");
        Map<String, RespuestaExperimental> ordered = new TreeMap<>();
        for (int i = 1; i <= 12; i++) {
            String key = "V" + i;
            if (answers.get(key) == null) throw new IllegalArgumentException("Responde las 12 preguntas seleccionando Sí, No o No estoy segura.");
            ordered.put(key, answers.get(key));
        }
        if (!answers.keySet().equals(ordered.keySet())) throw new IllegalArgumentException("Las respuestas deben contener solo V1–V12.");
        return ordered;
    }

    private SimulacionResponse respuesta(Evaluacion evaluacion, ResultadoSimulacion record, String message) {
        Map<String, RespuestaExperimental> answers = new TreeMap<>();
        record.getRespuestas().properties().forEach(entry -> answers.put(entry.getKey(),
                RespuestaExperimental.valueOf(entry.getValue().stringValue())));
        List<String> uncertain = answers.entrySet().stream().filter(e -> e.getValue() == RespuestaExperimental.NO_ESTOY_SEGURA)
                .map(Map.Entry::getKey).toList();
        List<String> critical = new ArrayList<>();
        if (answers.get("V11") == RespuestaExperimental.SI) critical.add("V11");
        if (answers.get("V12") == RespuestaExperimental.SI) critical.add("V12");
        return new SimulacionResponse(evaluacion.getIdEvaluacion(), evaluacion.getCaso().getIdCaso(),
                record.getEstado(), answers, record.getIndiceExperimental(), INDEX_NAME, record.getModeloVersion(),
                MODEL_TYPE, record.getFechaGeneracion(), uncertain, critical, WARNING, message);
    }
}
