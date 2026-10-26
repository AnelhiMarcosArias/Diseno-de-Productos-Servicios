package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.EvaluacionResponse;
import com.utp.sistema_feminicidio.service.EvaluacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/casos/{casoId}/evaluaciones")
public class EvaluacionController {
    private final EvaluacionService service;
    public EvaluacionController(EvaluacionService service) { this.service = service; }

    @GetMapping
    public Page<EvaluacionResponse> listar(@PathVariable Long casoId,
            @PageableDefault(size = 20, sort = "fechaEvaluacion") Pageable pageable, Principal principal) {
        return service.listarPorCaso(casoId, principal.getName(), pageable);
    }
}
