package com.utp.sistema_feminicidio.repository;

import com.utp.sistema_feminicidio.model.ResultadoSimulacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoSimulacionRepository extends JpaRepository<ResultadoSimulacion, Long> {
    Optional<ResultadoSimulacion> findByEvaluacion_IdEvaluacion(Long evaluacionId);
    boolean existsByEvaluacion_IdEvaluacionAndEstado(Long evaluacionId,
            com.utp.sistema_feminicidio.model.EstadoSimulacion estado);
}
