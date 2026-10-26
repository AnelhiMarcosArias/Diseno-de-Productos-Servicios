package com.utp.sistema_feminicidio.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "resultado_evaluacion")
public class ResultadoEvaluacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_resultado_evaluacion")
    private Long idResultadoEvaluacion;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_evaluacion", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_resultado_evaluacion_evaluacion"))
    private Evaluacion evaluacion;
    @NotNull @Enumerated(EnumType.STRING) @Column(name = "nivel_riesgo", nullable = false, length = 10)
    private NivelRiesgo nivelRiesgo;
    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") @Column(name = "probabilidad_estimada", nullable = false, precision = 5, scale = 4)
    private BigDecimal probabilidadEstimada;
    @NotBlank @Size(max = 100) @Column(name = "modelo_utilizado", nullable = false, length = 100)
    private String modeloUtilizado;
    @NotNull @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;
    /** Documento JSON con los factores relevantes del resultado ML. */
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "factores_relevantes", columnDefinition = "jsonb")
    private JsonNode factoresRelevantes;
    @Column(name = "recomendaciones", columnDefinition = "text")
    private String recomendaciones;
    @Column(name = "requiere_revision_profesional", nullable = false)
    private boolean requiereRevisionProfesional;
    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    public ResultadoEvaluacion() {
    }
    public ResultadoEvaluacion(Long idResultadoEvaluacion, Evaluacion evaluacion, NivelRiesgo nivelRiesgo, BigDecimal probabilidadEstimada, String modeloUtilizado, LocalDateTime fechaGeneracion, JsonNode factoresRelevantes, String recomendaciones, boolean requiereRevisionProfesional, String observaciones) {
        this.idResultadoEvaluacion = idResultadoEvaluacion;
        this.evaluacion = evaluacion;
        this.nivelRiesgo = nivelRiesgo;
        this.probabilidadEstimada = probabilidadEstimada;
        this.modeloUtilizado = modeloUtilizado;
        this.fechaGeneracion = fechaGeneracion;
        this.factoresRelevantes = factoresRelevantes;
        this.recomendaciones = recomendaciones;
        this.requiereRevisionProfesional = requiereRevisionProfesional;
        this.observaciones = observaciones;
    }

    public Long getIdResultadoEvaluacion() {
        return idResultadoEvaluacion;
    }
    public void setIdResultadoEvaluacion(Long idResultadoEvaluacion) {
        this.idResultadoEvaluacion = idResultadoEvaluacion;
    }
    public Evaluacion getEvaluacion() { return evaluacion; }
    public void setEvaluacion(Evaluacion evaluacion) { this.evaluacion = evaluacion; }
    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }
    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }
    public BigDecimal getProbabilidadEstimada() {
        return probabilidadEstimada;
    }
    public void setProbabilidadEstimada(BigDecimal probabilidadEstimada) {
        this.probabilidadEstimada = probabilidadEstimada;
    }
    public String getModeloUtilizado() {
        return modeloUtilizado;
    }
    public void setModeloUtilizado(String modeloUtilizado) {
        this.modeloUtilizado = modeloUtilizado;
    }
    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
    public JsonNode getFactoresRelevantes() {
        return factoresRelevantes;
    }
    public void setFactoresRelevantes(JsonNode factoresRelevantes) {
        this.factoresRelevantes = factoresRelevantes;
    }
    public String getRecomendaciones() {
        return recomendaciones;
    }
    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }
    public boolean isRequiereRevisionProfesional() {
        return requiereRevisionProfesional;
    }
    public void setRequiereRevisionProfesional(boolean requiereRevisionProfesional) {
        this.requiereRevisionProfesional = requiereRevisionProfesional;
    }
    public String getObservaciones() {
        return observaciones;
    }
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
