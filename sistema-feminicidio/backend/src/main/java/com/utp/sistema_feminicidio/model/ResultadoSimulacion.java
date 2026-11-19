package com.utp.sistema_feminicidio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

/** Persists the synthetic demonstration separately from any validated/legacy result. */
@Entity
@Table(name = "resultado_simulacion")
public class ResultadoSimulacion {
    @Id
    @Column(name = "id_evaluacion", nullable = false)
    private Long evaluacionId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false,
            foreignKey = @ForeignKey(name = "fk_simulacion_evaluacion"))
    private Evaluacion evaluacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "respuestas", nullable = false, columnDefinition = "jsonb")
    private JsonNode respuestas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 24)
    private EstadoSimulacion estado;

    @Column(name = "indice_experimental", precision = 5, scale = 2)
    private BigDecimal indiceExperimental;

    @Column(name = "modelo_version", length = 100)
    private String modeloVersion;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "codigo_error", length = 40)
    private String codigoError;

    public Long getEvaluacionId() { return evaluacionId; }
    public Evaluacion getEvaluacion() { return evaluacion; }
    public void setEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }
    public JsonNode getRespuestas() { return respuestas; }
    public void setRespuestas(JsonNode respuestas) { this.respuestas = respuestas; }
    public EstadoSimulacion getEstado() { return estado; }
    public void setEstado(EstadoSimulacion estado) { this.estado = estado; }
    public BigDecimal getIndiceExperimental() { return indiceExperimental; }
    public void setIndiceExperimental(BigDecimal indiceExperimental) { this.indiceExperimental = indiceExperimental; }
    public String getModeloVersion() { return modeloVersion; }
    public void setModeloVersion(String modeloVersion) { this.modeloVersion = modeloVersion; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public String getCodigoError() { return codigoError; }
    public void setCodigoError(String codigoError) { this.codigoError = codigoError; }
}
