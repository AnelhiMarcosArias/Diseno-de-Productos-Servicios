package com.utp.sistema_feminicidio.repository;

import com.utp.sistema_feminicidio.model.Evaluacion;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    Page<Evaluacion> findByCaso_IdCaso(Long idCaso, Pageable pageable);
    java.util.Optional<Evaluacion> findFirstByCaso_IdCasoAndTipoEvaluacionOrderByFechaEvaluacionAsc(
            Long idCaso, String tipoEvaluacion);
    Page<Evaluacion> findByFechaEvaluacionBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
    boolean existsByIdEvaluacionAndCaso_Usuario_Usuario(Long idEvaluacion, String usuario);
}
