package com.utp.sistema_feminicidio.repository;

import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CasoRepository extends JpaRepository<Caso, Long> {
    boolean existsByCodigoCaso(String codigoCaso);
    @Query("""
            select c from Caso c
            where c.usuario.usuario = :username
              and (:estado is null or c.estadoCaso = :estado)
              and (:departamento is null or lower(c.departamento) = lower(:departamento))
              and (:provincia is null or lower(c.provincia) = lower(:provincia))
              and (:distrito is null or lower(c.distrito) = lower(:distrito))
              and (:desde is null or c.fechaRegistro >= :desde)
              and (:hasta is null or c.fechaRegistro <= :hasta)
            """)
    Page<Caso> buscar(@Param("estado") EstadoCaso estado,
                      @Param("departamento") String departamento,
                      @Param("provincia") String provincia,
                      @Param("distrito") String distrito,
                      @Param("desde") LocalDateTime desde,
                      @Param("hasta") LocalDateTime hasta,
                      @Param("username") String username,
                      Pageable pageable);
    java.util.Optional<Caso> findByIdCasoAndUsuario_Usuario(Long idCaso, String usuario);
    Page<Caso> findByEstadoCaso(EstadoCaso estadoCaso, Pageable pageable);
    Page<Caso> findByDepartamentoIgnoreCase(String departamento, Pageable pageable);
    Page<Caso> findByProvinciaIgnoreCase(String provincia, Pageable pageable);
    Page<Caso> findByDistritoIgnoreCase(String distrito, Pageable pageable);
    Page<Caso> findByFechaRegistroBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
