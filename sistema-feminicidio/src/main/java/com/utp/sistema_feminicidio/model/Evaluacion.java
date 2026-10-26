package com.utp.sistema_feminicidio.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "evaluacion")
public class Evaluacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_evaluacion")
    private Long idEvaluacion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_caso", nullable = false, foreignKey = @ForeignKey(name = "fk_evaluacion_caso"))
    private Caso caso;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_usuario_evaluador", nullable = false, foreignKey = @ForeignKey(name = "fk_evaluacion_usuario"))
    private Usuario usuarioEvaluador;
    @NotNull @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDateTime fechaEvaluacion;
    @NotBlank @Size(max = 50) @Column(name = "tipo_evaluacion", nullable = false, length = 50)
    private String tipoEvaluacion;
    @Size(max = 50) @Column(name = "version_modelo", length = 50)
    private String versionModelo;
    @NotBlank @Size(max = 30) @Column(name = "estado_evaluacion", nullable = false, length = 30)
    private String estadoEvaluacion;
    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;
    @Column(name = "fecha_resultado_ml")
    private LocalDateTime fechaResultadoML;

    public Evaluacion() {
    }
    public Evaluacion(Long idEvaluacion, Caso caso, Usuario usuarioEvaluador, LocalDateTime fechaEvaluacion, String tipoEvaluacion, String versionModelo, String estadoEvaluacion, String observaciones, LocalDateTime fechaResultadoML) {
        this.idEvaluacion = idEvaluacion;
        this.caso = caso;
        this.usuarioEvaluador = usuarioEvaluador;
        this.fechaEvaluacion = fechaEvaluacion;
        this.tipoEvaluacion = tipoEvaluacion;
        this.versionModelo = versionModelo;
        this.estadoEvaluacion = estadoEvaluacion;
        this.observaciones = observaciones;
        this.fechaResultadoML = fechaResultadoML;
    }

    public Long getIdEvaluacion() {
        return idEvaluacion;
    }

    public void setIdEvaluacion(Long idEvaluacion) {
        this.idEvaluacion = idEvaluacion;
    }
    public Caso getCaso() { return caso; } public void setCaso(Caso caso) { this.caso = caso; }
    public Usuario getUsuarioEvaluador() { return usuarioEvaluador; } public void setUsuarioEvaluador(Usuario usuarioEvaluador) { this.usuarioEvaluador = usuarioEvaluador; }
    public LocalDateTime getFechaEvaluacion() {
        return fechaEvaluacion;
    }
    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }
    public String getTipoEvaluacion() {
        return tipoEvaluacion;
    }
    public void setTipoEvaluacion(String tipoEvaluacion) {
        this.tipoEvaluacion = tipoEvaluacion;
    }
    public String getVersionModelo() {
        return versionModelo;
    }
    public void setVersionModelo(String versionModelo) {
        this.versionModelo = versionModelo;
    }
    public String getEstadoEvaluacion() {
        return estadoEvaluacion;
    }
    public void setEstadoEvaluacion(String estadoEvaluacion) {
        this.estadoEvaluacion = estadoEvaluacion;
    }
    public String getObservaciones() {
        return observaciones;
    }
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    public LocalDateTime getFechaResultadoML() {
        return fechaResultadoML;
    }
    public void setFechaResultadoML(LocalDateTime fechaResultadoML) {
        this.fechaResultadoML = fechaResultadoML;
    }
}
