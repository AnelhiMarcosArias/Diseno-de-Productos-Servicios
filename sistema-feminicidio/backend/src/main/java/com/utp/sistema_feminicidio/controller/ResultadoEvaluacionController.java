package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.ResultadoEvaluacionResponse;
import com.utp.sistema_feminicidio.service.ResultadoEvaluacionService;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/evaluaciones")
public class ResultadoEvaluacionController {
    private final ResultadoEvaluacionService service;
    public ResultadoEvaluacionController(ResultadoEvaluacionService service) { this.service = service; }

    @GetMapping("/{evaluacionId}/resultado")
    public ResultadoEvaluacionResponse obtener(@PathVariable Long evaluacionId, Principal principal) {
        return service.obtenerPorEvaluacion(evaluacionId, principal.getName());
    }
}
