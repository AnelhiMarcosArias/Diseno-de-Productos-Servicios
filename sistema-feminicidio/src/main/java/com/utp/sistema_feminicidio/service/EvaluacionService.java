package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.EvaluacionResponse;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.EvaluacionRepository;
import com.utp.sistema_feminicidio.repository.ResultadoEvaluacionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.sistema_feminicidio.util.Paginacion;

@Service
public class EvaluacionService {
    private final EvaluacionRepository evaluacionRepository;
    private final CasoRepository casoRepository;
    private final ResultadoEvaluacionRepository resultadoRepository;

    public EvaluacionService(EvaluacionRepository evaluacionRepository, CasoRepository casoRepository,
            ResultadoEvaluacionRepository resultadoRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.casoRepository = casoRepository;
        this.resultadoRepository = resultadoRepository;
    }

    @Transactional(readOnly = true)
    public Page<EvaluacionResponse> listarPorCaso(Long casoId, String usuarioAutenticado, Pageable pageable) {
        if (casoRepository.findByIdCasoAndUsuario_Usuario(casoId, usuarioAutenticado).isEmpty())
            throw new RecursoNoEncontradoException("Caso", casoId);
        return evaluacionRepository.findByCaso_IdCaso(casoId, Paginacion.limitar(pageable)).map(e ->
                new EvaluacionResponse(e.getIdEvaluacion(), e.getCaso().getIdCaso(), e.getFechaEvaluacion(),
                        e.getTipoEvaluacion(), e.getVersionModelo(), e.getEstadoEvaluacion(), e.getFechaResultadoML(),
                        resultadoRepository.existsByEvaluacion_IdEvaluacion(e.getIdEvaluacion())));
    }
}
