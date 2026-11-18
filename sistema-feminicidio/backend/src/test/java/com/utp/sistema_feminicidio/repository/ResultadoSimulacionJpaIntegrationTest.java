package com.utp.sistema_feminicidio.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.utp.sistema_feminicidio.dto.CasoRequest;
import com.utp.sistema_feminicidio.dto.DetalleCasoRequest;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import com.utp.sistema_feminicidio.model.EstadoSimulacion;
import com.utp.sistema_feminicidio.dto.RegistroCasoRequest;
import com.utp.sistema_feminicidio.model.RolUsuario;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.dto.RespuestaExperimental;
import com.utp.sistema_feminicidio.dto.SimulacionRequest;
import com.utp.sistema_feminicidio.model.ResultadoSimulacion;
import com.utp.sistema_feminicidio.service.SimulacionMlClient;
import com.utp.sistema_feminicidio.service.SimulacionService;
import com.utp.sistema_feminicidio.service.RegistroCasoService;
import com.utp.sistema_feminicidio.config.MlServiceClientConfig;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Opt-in end-to-end persistence test against PostgreSQL and the running synthetic FastAPI.
 * Its transaction rolls back all inserted rows. Run with -DrunPostgresIntegration=true.
 */
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=none", "spring.flyway.enabled=false"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@EnabledIfSystemProperty(named = "runPostgresIntegration", matches = "true")
@Import({RegistroCasoService.class, SimulacionService.class, SimulacionMlClient.class, MlServiceClientConfig.class,
        ResultadoSimulacionJpaIntegrationTest.MapperConfig.class})
class ResultadoSimulacionJpaIntegrationTest {
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ResultadoSimulacionRepository simulacionRepository;
    @Autowired private RegistroCasoService registroCasoService;
    @Autowired private SimulacionService simulacionService;

    @Test
    void persistsNewResultUsingEvaluationAsItsSharedPrimaryKey() {
        String suffix = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Usuario usuario = new Usuario();
        usuario.setUsuario("jpa-test-" + suffix);
        usuario.setContrasena("not-a-real-password-hash");
        usuario.setNombre("Prueba");
        usuario.setApellido("Temporal");
        usuario.setCorreo("jpa-test-" + suffix + "@example.invalid");
        usuario.setRol(RolUsuario.PROFESIONAL);
        usuario.setEstado(true);
        usuario.setFechaCreacion(now);
        usuario = usuarioRepository.saveAndFlush(usuario);

        Map<String, RespuestaExperimental> answers = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) answers.put("V" + i, RespuestaExperimental.NO);
        RegistroCasoRequest request = new RegistroCasoRequest(
                new CasoRequest("JPA-TEST-" + suffix, "Persona", "Temporal", null, 30,
                        null, null, null, null, EstadoCaso.REGISTRO),
                new DetalleCasoRequest(false, false, false, false, false, false,
                        false, false, false, false, false, false),
                answers);
        var registro = registroCasoService.registrar(request, usuario.getUsuario());
        var evaluacionId = registro.idEvaluacion();

        ResultadoSimulacion pendiente = simulacionRepository
                .findByEvaluacion_IdEvaluacion(evaluacionId).orElseThrow();
        assertEquals(EstadoSimulacion.PENDIENTE, pendiente.getEstado());
        assertEquals(12, pendiente.getRespuestas().size());

        var response = simulacionService.ejecutar(evaluacionId,
                new SimulacionRequest(answers), usuario.getUsuario());

        assertEquals(EstadoSimulacion.COMPLETADA, response.estado());
        assertNotNull(response.indiceExperimental());
        assertEquals(evaluacionId, response.evaluacionId());
        ResultadoSimulacion guardado = simulacionRepository
                .findByEvaluacion_IdEvaluacion(evaluacionId).orElseThrow();
        assertEquals(EstadoSimulacion.COMPLETADA, guardado.getEstado());
        assertEquals(evaluacionId, guardado.getEvaluacionId());
        assertEquals(response.indiceExperimental(), guardado.getIndiceExperimental());
        assertEquals("synthetic-v1-v12-xgb-demo-v1", guardado.getModeloVersion());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class MapperConfig {
        @Bean ObjectMapper testObjectMapper() { return JsonMapper.builder().build(); }
    }
}
