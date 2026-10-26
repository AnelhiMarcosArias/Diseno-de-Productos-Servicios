package com.utp.sistema_feminicidio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.utp.sistema_feminicidio.model.DetalleCaso;
import java.util.Optional;

public interface DetalleCasoRepository extends JpaRepository<DetalleCaso, Long> {
    Optional<DetalleCaso> findByCaso_IdCaso(Long idCaso);
}
