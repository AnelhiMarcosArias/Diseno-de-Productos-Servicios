package com.utp.sistema_feminicidio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Variables de entrada tabulares utilizadas por el modelo de machine learning. */
@Entity
@Table(name = "detalle_caso")
public class DetalleCaso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_caso")
    private Long idDetalleCaso;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_caso", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_detalle_caso_caso"))
    private Caso caso;

    // Nombres provisionales hasta que se definan las 12 variables funcionales.
    @Column(name = "variable1") private Boolean variable1;
    @Column(name = "variable2") private Boolean variable2;
    @Column(name = "variable3") private Boolean variable3;
    @Column(name = "variable4") private Boolean variable4;
    @Column(name = "variable5") private Boolean variable5;
    @Column(name = "variable6") private Boolean variable6;
    @Column(name = "variable7") private Boolean variable7;
    @Column(name = "variable8") private Boolean variable8;
    @Column(name = "variable9") private Boolean variable9;
    @Column(name = "variable10") private Boolean variable10;
    @Column(name = "variable11") private Boolean variable11;
    @Column(name = "variable12") private Boolean variable12;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public DetalleCaso() {}
    public Long getIdDetalleCaso() { return idDetalleCaso; }
    public Caso getCaso() { return caso; }
    public void setCaso(Caso caso) { this.caso = caso; }
    public Boolean getVariable1() { return variable1; } public void setVariable1(Boolean v) { variable1 = v; }
    public Boolean getVariable2() { return variable2; } public void setVariable2(Boolean v) { variable2 = v; }
    public Boolean getVariable3() { return variable3; } public void setVariable3(Boolean v) { variable3 = v; }
    public Boolean getVariable4() { return variable4; } public void setVariable4(Boolean v) { variable4 = v; }
    public Boolean getVariable5() { return variable5; } public void setVariable5(Boolean v) { variable5 = v; }
    public Boolean getVariable6() { return variable6; } public void setVariable6(Boolean v) { variable6 = v; }
    public Boolean getVariable7() { return variable7; } public void setVariable7(Boolean v) { variable7 = v; }
    public Boolean getVariable8() { return variable8; } public void setVariable8(Boolean v) { variable8 = v; }
    public Boolean getVariable9() { return variable9; } public void setVariable9(Boolean v) { variable9 = v; }
    public Boolean getVariable10() { return variable10; } public void setVariable10(Boolean v) { variable10 = v; }
    public Boolean getVariable11() { return variable11; } public void setVariable11(Boolean v) { variable11 = v; }
    public Boolean getVariable12() { return variable12; } public void setVariable12(Boolean v) { variable12 = v; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
