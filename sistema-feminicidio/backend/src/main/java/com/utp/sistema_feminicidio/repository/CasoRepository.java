package com.utp.sistema_feminicidio.repository;

import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CasoRepository extends JpaRepository<Caso, Long>, JpaSpecificationExecutor<Caso> {
    boolean existsByCodigoCaso(String codigoCaso);
    java.util.Optional<Caso> findByCodigoCasoAndUsuario_Usuario(String codigoCaso, String usuario);
    java.util.Optional<Caso> findByIdCasoAndUsuario_Usuario(Long idCaso, String usuario);
    Page<Caso> findByEstadoCaso(EstadoCaso estadoCaso, Pageable pageable);
    Page<Caso> findByDepartamentoIgnoreCase(String departamento, Pageable pageable);
    Page<Caso> findByProvinciaIgnoreCase(String provincia, Pageable pageable);
    Page<Caso> findByDistritoIgnoreCase(String distrito, Pageable pageable);
    Page<Caso> findByFechaRegistroBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
