package com.utp.sistema_feminicidio.repository;

import com.utp.sistema_feminicidio.model.ResultadoEvaluacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoEvaluacionRepository extends JpaRepository<ResultadoEvaluacion, Long> {
    Optional<ResultadoEvaluacion> findByEvaluacion_IdEvaluacion(Long idEvaluacion);
}
