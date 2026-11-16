package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.ResultadoEvaluacionResponse;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.repository.ResultadoEvaluacionRepository;
import com.utp.sistema_feminicidio.repository.EvaluacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultadoEvaluacionService {
    private final ResultadoEvaluacionRepository repository;
    private final EvaluacionRepository evaluacionRepository;

    public ResultadoEvaluacionService(ResultadoEvaluacionRepository repository, EvaluacionRepository evaluacionRepository) {
        this.repository = repository;
        this.evaluacionRepository = evaluacionRepository;
    }

    @Transactional(readOnly = true)
    public ResultadoEvaluacionResponse obtenerPorEvaluacion(Long evaluacionId, String usuarioAutenticado) {
        if (!evaluacionRepository.existsByIdEvaluacionAndCaso_Usuario_Usuario(evaluacionId, usuarioAutenticado))
            throw new RecursoNoEncontradoException("Evaluación", evaluacionId);
        return repository.findByEvaluacion_IdEvaluacion(evaluacionId)
                .map(r -> new ResultadoEvaluacionResponse(r.getIdResultadoEvaluacion(),
                        r.getEvaluacion().getIdEvaluacion(), r.getNivelRiesgo(), r.getProbabilidadEstimada(),
                        r.getModeloUtilizado(), r.getFechaGeneracion(), r.getFactoresRelevantes(),
                        r.getRecomendaciones(), r.isRequiereRevisionProfesional(), r.getObservaciones()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Resultado para evaluación", evaluacionId));
    }
}
