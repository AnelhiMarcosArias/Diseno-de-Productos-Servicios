package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.ResumenDashboardResponse;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.EvaluacionRepository;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final CasoRepository casos;
    private final EvaluacionRepository evaluaciones;

    public DashboardController(CasoRepository casos, EvaluacionRepository evaluaciones) {
        this.casos = casos;
        this.evaluaciones = evaluaciones;
    }

    @GetMapping("/resumen")
    public ResumenDashboardResponse resumen(Principal principal) {
        String username = principal.getName();
        return new ResumenDashboardResponse(casos.countByUsuario_Usuario(username),
                evaluaciones.countByCaso_Usuario_Usuario(username),
                evaluaciones.countByCaso_Usuario_UsuarioAndEstadoEvaluacion(username, "SIMULACION_COMPLETADA"),
                evaluaciones.countByCaso_Usuario_UsuarioAndEstadoEvaluacion(username, "PENDIENTE_ML"),
                evaluaciones.countByCaso_Usuario_UsuarioAndEstadoEvaluacion(username, "ERROR_TECNICO_ML"));
    }
}
