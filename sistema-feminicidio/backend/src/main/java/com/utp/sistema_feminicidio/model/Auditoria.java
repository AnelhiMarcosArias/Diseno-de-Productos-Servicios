package com.utp.sistema_feminicidio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
public class Auditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_auditoria")
    private Long idAuditoria;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_usuario", foreignKey = @ForeignKey(name = "fk_auditoria_usuario"))
    private Usuario usuario;
    @Column(name = "accion", nullable = false, length = 60) private String accion;
    @Column(name = "recurso", nullable = false, length = 60) private String recurso;
    @Column(name = "referencia", length = 100) private String referencia;
    @Column(name = "fecha_evento", nullable = false) private LocalDateTime fechaEvento;

    public Long getIdAuditoria() { return idAuditoria; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }
}
