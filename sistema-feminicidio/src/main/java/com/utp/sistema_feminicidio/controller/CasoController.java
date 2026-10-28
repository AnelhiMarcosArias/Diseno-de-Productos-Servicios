package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.CasoRequest;
import com.utp.sistema_feminicidio.dto.CasoResumenResponse;
import com.utp.sistema_feminicidio.dto.CasoUpdateRequest;
import com.utp.sistema_feminicidio.dto.RegistroCasoRequest;
import com.utp.sistema_feminicidio.dto.RegistroCasoResponse;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import com.utp.sistema_feminicidio.service.CasoService;
import com.utp.sistema_feminicidio.service.RegistroCasoService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.security.Principal;

@RestController
@RequestMapping("/api/casos")
public class CasoController {
    private final CasoService casoService;
    private final RegistroCasoService registroCasoService;
    public CasoController(CasoService casoService, RegistroCasoService registroCasoService) {
        this.casoService = casoService;
        this.registroCasoService = registroCasoService;
    }

    @GetMapping
    public Page<CasoResumenResponse> listar(@RequestParam(required = false) EstadoCaso estado,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String provincia,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) LocalDateTime desde,
            @RequestParam(required = false) LocalDateTime hasta,
            @PageableDefault(size = 20, sort = "fechaRegistro") Pageable pageable, Principal principal) {
        return casoService.listar(estado, departamento, provincia, distrito, desde, hasta, principal.getName(), pageable);
    }

    @GetMapping("/{id}")
    public CasoResumenResponse obtener(@PathVariable Long id, Principal principal) { return casoService.obtener(id, principal.getName()); }

    @PostMapping
    public ResponseEntity<CasoResumenResponse> crear(@Valid @RequestBody CasoRequest solicitud,
            UriComponentsBuilder uriBuilder, Principal principal) {
        CasoResumenResponse creado = casoService.crear(solicitud, principal.getName());
        return ResponseEntity.created(uriBuilder.path("/api/casos/{id}").buildAndExpand(creado.idCaso()).toUri())
                .body(creado);
    }

    @PutMapping("/{id}")
    public CasoResumenResponse actualizar(@PathVariable Long id, @Valid @RequestBody CasoUpdateRequest solicitud, Principal principal) {
        return casoService.actualizar(id, solicitud, principal.getName());
    }

    @PostMapping("/registro-completo")
    public ResponseEntity<RegistroCasoResponse> registrarCompleto(
            @Valid @RequestBody RegistroCasoRequest solicitud, Principal principal,
            UriComponentsBuilder uriBuilder) {
        RegistroCasoResponse registro = registroCasoService.registrar(solicitud, principal.getName());
        if (registro.reintento()) {
            return ResponseEntity.ok(registro);
        }
        return ResponseEntity.created(uriBuilder.path("/api/casos/{id}")
                .buildAndExpand(registro.idCaso()).toUri()).body(registro);
    }
}
