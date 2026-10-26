package com.utp.sistema_feminicidio.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_usuario")
    private Long  idUsuario;
    @NotBlank @Size(max = 50) @Column(name = "usuario", nullable = false, unique = true, length = 50)
    private String usuario;
    /** Debe contener un hash seguro, nunca la contraseña en texto plano. */
    @NotBlank @Size(max = 255) @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;
    @NotBlank @Size(max = 100) @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    @NotBlank @Size(max = 100) @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;
    @NotBlank @Email @Size(max = 254) @Column(name = "correo", nullable = false, unique = true, length = 254)
    private String correo;
    @NotNull @Enumerated(EnumType.STRING) @Column(name = "rol", nullable = false, length = 20)
    private RolUsuario rol;
    @NotNull @Column(name = "estado", nullable = false)
    private Boolean estado;
    @NotNull @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    public Usuario() {
    }
    public Usuario(Long idUsuario, String usuario, String contrasena, String nombre, String apellido, String correo, RolUsuario rol, Boolean estado, LocalDateTime fechaCreacion, LocalDateTime ultimoAcceso) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.ultimoAcceso = ultimoAcceso;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
    public String getUsuario() {
        return usuario;
    }
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellido() {
        return apellido;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    public RolUsuario getRol() {
        return rol;
    }
    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }
    public Boolean getEstado() {
        return estado;
    }
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }
}
