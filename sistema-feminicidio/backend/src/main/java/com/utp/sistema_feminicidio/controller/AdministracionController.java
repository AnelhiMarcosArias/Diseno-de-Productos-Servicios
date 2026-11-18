package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.AuditoriaResponse;
import com.utp.sistema_feminicidio.dto.EstadoUsuarioRequest;
import com.utp.sistema_feminicidio.dto.RolUsuarioRequest;
import com.utp.sistema_feminicidio.dto.UsuarioAdminRequest;
import com.utp.sistema_feminicidio.dto.UsuarioAdminResponse;
import com.utp.sistema_feminicidio.service.AdministracionService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdministracionController {
    private final AdministracionService service;
    public AdministracionController(AdministracionService service) { this.service = service; }

    @GetMapping("/usuarios")
    public List<UsuarioAdminResponse> usuarios() { return service.listarUsuarios(); }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdminResponse crear(@Valid @RequestBody UsuarioAdminRequest request, Principal principal) {
        return service.crearUsuario(request, principal.getName());
    }

    @PatchMapping("/usuarios/{id}/estado")
    public UsuarioAdminResponse cambiarEstado(@PathVariable Long id,
            @Valid @RequestBody EstadoUsuarioRequest request, Principal principal) {
        return service.cambiarEstado(id, request, principal.getName());
    }

    @PatchMapping("/usuarios/{id}/rol")
    public UsuarioAdminResponse cambiarRol(@PathVariable Long id,
            @Valid @RequestBody RolUsuarioRequest request, Principal principal) {
        return service.cambiarRol(id, request, principal.getName());
    }

    @GetMapping("/auditoria")
    public List<AuditoriaResponse> auditoria() { return service.listarAuditoria(); }
}
