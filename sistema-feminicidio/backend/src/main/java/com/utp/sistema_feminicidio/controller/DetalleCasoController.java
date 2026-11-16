package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.DetalleCasoRequest;
import com.utp.sistema_feminicidio.dto.DetalleCasoResponse;
import com.utp.sistema_feminicidio.service.DetalleCasoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/casos/{casoId}/detalle")
public class DetalleCasoController {
    private final DetalleCasoService service;
    public DetalleCasoController(DetalleCasoService service) { this.service = service; }

    @GetMapping
    public DetalleCasoResponse obtener(@PathVariable Long casoId, Principal principal) { return service.obtenerPorCaso(casoId, principal.getName()); }

    @PutMapping
    public ResponseEntity<DetalleCasoResponse> guardar(@PathVariable Long casoId,
            @Valid @RequestBody DetalleCasoRequest solicitud, Principal principal) {
        return ResponseEntity.ok(service.guardar(casoId, solicitud, principal.getName()));
    }
}
