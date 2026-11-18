package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.SimulacionRequest;
import com.utp.sistema_feminicidio.dto.SimulacionResponse;
import com.utp.sistema_feminicidio.model.EstadoSimulacion;
import com.utp.sistema_feminicidio.service.SimulacionService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluaciones/{evaluacionId}/simulacion")
public class SimulacionController {
    private final SimulacionService service;

    public SimulacionController(SimulacionService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<SimulacionResponse> ejecutar(@PathVariable Long evaluacionId,
            @Valid @RequestBody SimulacionRequest request, Principal principal) {
        SimulacionResponse response = service.ejecutar(evaluacionId, request, principal.getName());
        return ResponseEntity.status(response.estado() == EstadoSimulacion.ERROR_TECNICO
                ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.OK).body(response);
    }

    @GetMapping
    public SimulacionResponse obtener(@PathVariable Long evaluacionId, Principal principal) {
        return service.obtener(evaluacionId, principal.getName());
    }
}
