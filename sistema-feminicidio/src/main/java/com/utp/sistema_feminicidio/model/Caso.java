package com.utp.sistema_feminicidio.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;


@Entity
@Table(name = "caso")
public class Caso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caso")
    private Long idCaso;
    @NotBlank @Size(max = 50) @Column(name = "codigo_caso", nullable = false, unique = true, length = 50)
    private String codigoCaso;
    @NotNull @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    @NotBlank @Size(max = 100) @Column(name = "nombre_persona", nullable = false, length = 100)
    private String nombrePersona;
    @NotBlank @Size(max = 100) @Column(name = "apellido_persona", nullable = false, length = 100)
    private String apellidoPersona;
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    @PositiveOrZero @Column(name = "edad")
    private int edad;
    @Size(max = 100) @Column(name = "departamento", length = 100)
    private String departamento;
    @Size(max = 100) @Column(name = "provincia", length = 100)
    private String provincia;
    @Size(max = 100) @Column(name = "distrito", length = 100)
    private String distrito;
    @Size(max = 300) @Column(name = "direccion", length = 300)
    private String direccion;
    @NotNull @Enumerated(EnumType.STRING) @Column(name = "estado_caso", nullable = false, length = 20)
    private EstadoCaso estadoCaso;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "fk_caso_usuario"))
    private Usuario usuario;

    public Caso() {
    }

    public Caso(Long idCaso, String codigoCaso, LocalDateTime fechaRegistro, String nombrePersona, String apellidoPersona, LocalDate fechaNacimiento, int edad, String departamento, String provincia, String distrito, String direccion, EstadoCaso estadoCaso, Usuario usuario) {
        this.idCaso = idCaso;
        this.codigoCaso = codigoCaso;
        this.fechaRegistro = fechaRegistro;
        this.nombrePersona = nombrePersona;
        this.apellidoPersona = apellidoPersona;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.departamento = departamento;
        this.provincia = provincia;
        this.distrito = distrito;
        this.direccion = direccion;
        this.estadoCaso = estadoCaso;
        this.usuario = usuario;
    }

    public Long getIdCaso() {
        return idCaso;
    }
    public void setIdCaso(Long idCaso) {
        this.idCaso = idCaso;
    }
    public String getCodigoCaso() {
        return codigoCaso;
    }
    public void setCodigoCaso(String codigoCaso) {
        this.codigoCaso = codigoCaso;
    }
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public String getNombrePersona() {
        return nombrePersona;
    }
    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }
    public String getApellidoPersona() {
        return apellidoPersona;
    }
    public void setApellidoPersona(String apellidoPersona) {
        this.apellidoPersona = apellidoPersona;
    }
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    public int getEdad() {
        return edad;
    }
    public void setEdad(int edad) {
        this.edad = edad;
    }
    public String getDepartamento() {
        return departamento;
    }
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
    public String getProvincia() {
        return provincia;
    }
    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }
    public String getDistrito() {
        return distrito;
    }
    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public EstadoCaso getEstadoCaso() {
        return estadoCaso;
    }
    public void setEstadoCaso(EstadoCaso estadoCaso) {
        this.estadoCaso = estadoCaso;
    }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
