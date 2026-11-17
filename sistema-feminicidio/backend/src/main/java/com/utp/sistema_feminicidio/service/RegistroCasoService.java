package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.CasoRequest;
import com.utp.sistema_feminicidio.dto.DetalleCasoRequest;
import com.utp.sistema_feminicidio.dto.RegistroCasoRequest;
import com.utp.sistema_feminicidio.dto.RegistroCasoResponse;
import com.utp.sistema_feminicidio.exception.ConflictoRegistroException;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.model.DetalleCaso;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import com.utp.sistema_feminicidio.model.Evaluacion;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.DetalleCasoRepository;
import com.utp.sistema_feminicidio.repository.EvaluacionRepository;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroCasoService {
    static final String TIPO_EVALUACION_REGISTRO = "CUESTIONARIO_REGISTRO";
    static final String ESTADO_PENDIENTE_ML = "PENDIENTE_ML";

    private final CasoRepository casoRepository;
    private final DetalleCasoRepository detalleCasoRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final UsuarioRepository usuarioRepository;

    public RegistroCasoService(CasoRepository casoRepository,
            DetalleCasoRepository detalleCasoRepository,
            EvaluacionRepository evaluacionRepository,
            UsuarioRepository usuarioRepository) {
        this.casoRepository = casoRepository;
        this.detalleCasoRepository = detalleCasoRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RegistroCasoResponse registrar(RegistroCasoRequest solicitud, String username) {
        CasoRequest datosCaso = solicitud.caso();
        if (datosCaso.estadoCaso() != EstadoCaso.REGISTRO) {
            throw new IllegalArgumentException("Un caso nuevo debe iniciar en estado REGISTRO");
        }

        var existenteDelUsuario = casoRepository.findByCodigoCasoAndUsuario_Usuario(
                datosCaso.codigoCaso(), username);
        if (existenteDelUsuario.isPresent()) {
            return recuperarRegistroExistente(existenteDelUsuario.get(), solicitud);
        }
        if (casoRepository.existsByCodigoCaso(datosCaso.codigoCaso())) {
            throw conflictoCodigo();
        }

        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado", username));
        LocalDateTime ahora = LocalDateTime.now();
        Caso caso = crearCaso(datosCaso, usuario, ahora);
        caso = casoRepository.save(caso);

        DetalleCaso detalle = crearDetalle(solicitud.cuestionario(), caso, ahora);
        detalleCasoRepository.save(detalle);

        Evaluacion evaluacion = crearEvaluacion(caso, usuario, ahora);
        evaluacion = evaluacionRepository.save(evaluacion);
        return respuesta(caso, evaluacion, false);
    }

    private RegistroCasoResponse recuperarRegistroExistente(Caso caso, RegistroCasoRequest solicitud) {
        if (!mismosDatosCaso(caso, solicitud.caso())) {
            throw conflictoCodigo();
        }

        LocalDateTime ahora = LocalDateTime.now();
        var detalleExistente = detalleCasoRepository.findByCaso_IdCaso(caso.getIdCaso());
        if (detalleExistente.isPresent()) {
            if (!mismasRespuestas(detalleExistente.get(), solicitud.cuestionario())) {
                throw conflictoCodigo();
            }
        } else {
            detalleCasoRepository.save(crearDetalle(solicitud.cuestionario(), caso, ahora));
        }

        var evaluacionExistente = evaluacionRepository
                .findFirstByCaso_IdCasoAndTipoEvaluacionOrderByFechaEvaluacionAsc(
                        caso.getIdCaso(), TIPO_EVALUACION_REGISTRO);
        if (evaluacionExistente.isPresent()) {
            return respuesta(caso, evaluacionExistente.get(), true);
        }

        Usuario usuario = caso.getUsuario();
        Evaluacion evaluacion = evaluacionRepository.save(crearEvaluacion(caso, usuario, ahora));
        return respuesta(caso, evaluacion, true);
    }

    private Caso crearCaso(CasoRequest datos, Usuario usuario, LocalDateTime ahora) {
        Caso caso = new Caso();
        caso.setCodigoCaso(datos.codigoCaso());
        caso.setFechaRegistro(ahora);
        caso.setNombrePersona(datos.nombrePersona());
        caso.setApellidoPersona(datos.apellidoPersona());
        caso.setFechaNacimiento(datos.fechaNacimiento());
        caso.setEdad(datos.edad() == null ? 0 : datos.edad());
        caso.setDepartamento(datos.departamento());
        caso.setProvincia(datos.provincia());
        caso.setDistrito(datos.distrito());
        caso.setDireccion(datos.direccion());
        caso.setEstadoCaso(EstadoCaso.REGISTRO);
        caso.setUsuario(usuario);
        return caso;
    }

    private DetalleCaso crearDetalle(DetalleCasoRequest respuestas, Caso caso, LocalDateTime ahora) {
        DetalleCaso detalle = new DetalleCaso();
        detalle.setCaso(caso);
        detalle.setVariable1(respuestas.variable1());
        detalle.setVariable2(respuestas.variable2());
        detalle.setVariable3(respuestas.variable3());
        detalle.setVariable4(respuestas.variable4());
        detalle.setVariable5(respuestas.variable5());
        detalle.setVariable6(respuestas.variable6());
        detalle.setVariable7(respuestas.variable7());
        detalle.setVariable8(respuestas.variable8());
        detalle.setVariable9(respuestas.variable9());
        detalle.setVariable10(respuestas.variable10());
        detalle.setVariable11(respuestas.variable11());
        detalle.setVariable12(respuestas.variable12());
        detalle.setFechaActualizacion(ahora);
        return detalle;
    }

    private Evaluacion crearEvaluacion(Caso caso, Usuario evaluador, LocalDateTime ahora) {
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setCaso(caso);
        evaluacion.setUsuarioEvaluador(evaluador);
        evaluacion.setFechaEvaluacion(ahora);
        evaluacion.setTipoEvaluacion(TIPO_EVALUACION_REGISTRO);
        evaluacion.setEstadoEvaluacion(ESTADO_PENDIENTE_ML);
        return evaluacion;
    }

    private boolean mismosDatosCaso(Caso caso, CasoRequest datos) {
        return caso.getEstadoCaso() == EstadoCaso.REGISTRO
                && Objects.equals(caso.getCodigoCaso(), datos.codigoCaso())
                && Objects.equals(caso.getNombrePersona(), datos.nombrePersona())
                && Objects.equals(caso.getApellidoPersona(), datos.apellidoPersona())
                && Objects.equals(caso.getFechaNacimiento(), datos.fechaNacimiento())
                && caso.getEdad() == (datos.edad() == null ? 0 : datos.edad())
                && Objects.equals(caso.getDepartamento(), datos.departamento())
                && Objects.equals(caso.getProvincia(), datos.provincia())
                && Objects.equals(caso.getDistrito(), datos.distrito())
                && Objects.equals(caso.getDireccion(), datos.direccion());
    }

    private boolean mismasRespuestas(DetalleCaso actual, DetalleCasoRequest recibidas) {
        return respuestas(actual).equals(respuestas(recibidas));
    }

    private List<Boolean> respuestas(DetalleCaso d) {
        return Arrays.asList(d.getVariable1(), d.getVariable2(), d.getVariable3(), d.getVariable4(),
                d.getVariable5(), d.getVariable6(), d.getVariable7(), d.getVariable8(),
                d.getVariable9(), d.getVariable10(), d.getVariable11(), d.getVariable12());
    }

    private List<Boolean> respuestas(DetalleCasoRequest d) {
        return Arrays.asList(d.variable1(), d.variable2(), d.variable3(), d.variable4(),
                d.variable5(), d.variable6(), d.variable7(), d.variable8(),
                d.variable9(), d.variable10(), d.variable11(), d.variable12());
    }

    private RegistroCasoResponse respuesta(Caso caso, Evaluacion evaluacion, boolean reintento) {
        return new RegistroCasoResponse(caso.getIdCaso(), caso.getCodigoCaso(), caso.getEstadoCaso(),
                evaluacion.getIdEvaluacion(), evaluacion.getEstadoEvaluacion(), caso.getFechaRegistro(), reintento);
    }

    private ConflictoRegistroException conflictoCodigo() {
        return new ConflictoRegistroException(
                "No se pudo completar el registro porque el código ya está utilizado o sus datos no coinciden. Verifica el código en el historial.");
    }
}
