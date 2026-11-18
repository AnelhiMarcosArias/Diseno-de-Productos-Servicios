package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.AuditoriaResponse;
import com.utp.sistema_feminicidio.dto.EstadoUsuarioRequest;
import com.utp.sistema_feminicidio.dto.RolUsuarioRequest;
import com.utp.sistema_feminicidio.dto.UsuarioAdminRequest;
import com.utp.sistema_feminicidio.dto.UsuarioAdminResponse;
import com.utp.sistema_feminicidio.exception.ConflictoRegistroException;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.model.Auditoria;
import com.utp.sistema_feminicidio.model.RolUsuario;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.AuditoriaRepository;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministracionService {
    private final UsuarioRepository usuarios;
    private final AuditoriaRepository auditoria;
    private final PasswordEncoder passwordEncoder;

    public AdministracionService(UsuarioRepository usuarios, AuditoriaRepository auditoria,
            PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuarios() {
        return usuarios.findAll(Sort.by(Sort.Direction.ASC, "idUsuario")).stream().map(this::userResponse).toList();
    }

    @Transactional
    public UsuarioAdminResponse crearUsuario(UsuarioAdminRequest request, String actorName) {
        if (request.rol() == RolUsuario.PACIENTE) throw new IllegalArgumentException("Solo se pueden crear cuentas de PROFESIONAL o ADMINISTRADOR.");
        String normalizedUsername = request.usuario().trim();
        String normalizedEmail = request.correo().trim().toLowerCase(java.util.Locale.ROOT);
        if (request.contrasena().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("La contraseña excede el máximo compatible con el hash seguro.");
        if (usuarios.existsByUsuario(normalizedUsername) || usuarios.existsByCorreo(normalizedEmail))
            throw new ConflictoRegistroException("El usuario o correo ya está registrado.");
        Usuario user = new Usuario();
        user.setUsuario(normalizedUsername);
        user.setContrasena(passwordEncoder.encode(request.contrasena()));
        user.setNombre(request.nombre().trim());
        user.setApellido(request.apellido().trim());
        user.setCorreo(normalizedEmail);
        user.setRol(request.rol());
        user.setEstado(true);
        user.setFechaCreacion(LocalDateTime.now());
        user = usuarios.save(user);
        registrar(actorName, "CREAR_CUENTA", "USUARIO", user.getIdUsuario());
        return userResponse(user);
    }

    @Transactional
    public UsuarioAdminResponse cambiarEstado(Long id, EstadoUsuarioRequest request, String actorName) {
        Usuario user = buscar(id);
        if (!request.activo() && user.getUsuario().equals(actorName))
            throw new ConflictoRegistroException("No puedes desactivar tu propia cuenta durante esta sesión.");
        if (!request.activo() && user.getRol() == RolUsuario.ADMINISTRADOR && Boolean.TRUE.equals(user.getEstado())
                && usuarios.countByRolAndEstadoTrue(RolUsuario.ADMINISTRADOR) <= 1)
            throw new ConflictoRegistroException("Debe quedar al menos una cuenta administradora activa.");
        user.setEstado(request.activo());
        registrar(actorName, request.activo() ? "ACTIVAR_CUENTA" : "DESACTIVAR_CUENTA", "USUARIO", id);
        return userResponse(user);
    }

    @Transactional
    public UsuarioAdminResponse cambiarRol(Long id, RolUsuarioRequest request, String actorName) {
        Usuario user = buscar(id);
        if (request.rol() == RolUsuario.PACIENTE) throw new IllegalArgumentException("El rol de cuenta de paciente no se administra desde este módulo.");
        if (user.getRol() == RolUsuario.ADMINISTRADOR && user.getRol() != request.rol()
                && Boolean.TRUE.equals(user.getEstado())
                && usuarios.countByRolAndEstadoTrue(RolUsuario.ADMINISTRADOR) <= 1)
            throw new ConflictoRegistroException("Debe quedar al menos una cuenta administradora activa.");
        user.setRol(request.rol());
        registrar(actorName, "CAMBIAR_ROL", "USUARIO", id);
        return userResponse(user);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listarAuditoria() {
        return auditoria.findAll(PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "fechaEvento")))
                .stream().map(entry -> new AuditoriaResponse(entry.getIdAuditoria(),
                        entry.getUsuario() == null ? "desconocido" : entry.getUsuario().getUsuario(),
                        entry.getAccion(), entry.getRecurso(), entry.getReferencia(), entry.getFechaEvento())).toList();
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private void registrar(String actorName, String action, String resource, Long reference) {
        Auditoria entry = new Auditoria();
        entry.setUsuario(usuarios.findByUsuario(actorName).orElse(null));
        entry.setAccion(action);
        entry.setRecurso(resource);
        entry.setReferencia(String.valueOf(reference));
        entry.setFechaEvento(LocalDateTime.now());
        auditoria.save(entry);
    }

    private UsuarioAdminResponse userResponse(Usuario user) {
        return new UsuarioAdminResponse(user.getIdUsuario(), user.getUsuario(), user.getNombre(), user.getApellido(),
                user.getCorreo(), user.getRol(), user.getEstado(), user.getFechaCreacion(), user.getUltimoAcceso());
    }
}
