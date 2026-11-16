package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.MlServiceStatusResponse;
import com.utp.sistema_feminicidio.service.MlServiceStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class MlServiceStatusController {
    private final MlServiceStatusService service;

    public MlServiceStatusController(MlServiceStatusService service) {
        this.service = service;
    }

    @GetMapping("/ml-service")
    public MlServiceStatusResponse consultarEstadoMl() {
        return service.consultarEstado();
    }
}
