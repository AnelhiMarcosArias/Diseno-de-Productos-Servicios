package com.utp.sistema_feminicidio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.utp.sistema_feminicidio.dto.CasoRequest;
import com.utp.sistema_feminicidio.dto.DetalleCasoRequest;
import com.utp.sistema_feminicidio.dto.RegistroCasoRequest;
import com.utp.sistema_feminicidio.dto.RegistroCasoResponse;
import com.utp.sistema_feminicidio.exception.ConflictoRegistroException;
import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.model.DetalleCaso;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import com.utp.sistema_feminicidio.model.Evaluacion;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.DetalleCasoRepository;
import com.utp.sistema_feminicidio.repository.EvaluacionRepository;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import java.util.Optional;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistroCasoServiceTest {
    @Mock CasoRepository casoRepository;
    @Mock DetalleCasoRepository detalleCasoRepository;
    @Mock EvaluacionRepository evaluacionRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks RegistroCasoService service;

    @Test
    void registraCasoCuestionarioYEvaluacionPendienteSinConvertirAusenteEnNo() {
        Usuario usuario = usuario();
        when(casoRepository.findByCodigoCasoAndUsuario_Usuario("C-1", "ana")).thenReturn(Optional.empty());
        when(casoRepository.existsByCodigoCaso("C-1")).thenReturn(false);
        when(usuarioRepository.findByUsuario("ana")).thenReturn(Optional.of(usuario));
        when(casoRepository.save(any(Caso.class))).thenAnswer(invocation -> {
            Caso caso = invocation.getArgument(0);
            caso.setIdCaso(15L);
            return caso;
        });
        when(detalleCasoRepository.save(any(DetalleCaso.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(evaluacionRepository.save(any(Evaluacion.class))).thenAnswer(invocation -> {
            Evaluacion evaluacion = invocation.getArgument(0);
            evaluacion.setIdEvaluacion(30L);
            return evaluacion;
        });

        RegistroCasoResponse response = service.registrar(solicitud("C-1", "Nombre"), "ana");

        ArgumentCaptor<DetalleCaso> detalleCaptor = ArgumentCaptor.forClass(DetalleCaso.class);
        verify(detalleCasoRepository).save(detalleCaptor.capture());
        assertFalse(detalleCaptor.getValue().getVariable2());
        assertNull(detalleCaptor.getValue().getVariable3());

        ArgumentCaptor<Evaluacion> evaluacionCaptor = ArgumentCaptor.forClass(Evaluacion.class);
        verify(evaluacionRepository).save(evaluacionCaptor.capture());
        assertEquals("PENDIENTE_ML", evaluacionCaptor.getValue().getEstadoEvaluacion());
        assertNull(evaluacionCaptor.getValue().getFechaResultadoML());
        assertNull(evaluacionCaptor.getValue().getVersionModelo());
        assertEquals(EstadoCaso.REGISTRO, response.estadoCaso());
        assertEquals("PENDIENTE_ML", response.estadoEvaluacion());
        assertFalse(response.reintento());
    }

    @Test
    void reintentoConMismosDatosDevuelveRegistroExistenteSinDuplicarlo() {
        Caso existente = casoExistente("C-1", "Nombre");
        when(casoRepository.findByCodigoCasoAndUsuario_Usuario("C-1", "ana"))
                .thenReturn(Optional.of(existente));
        when(detalleCasoRepository.findByCaso_IdCaso(15L)).thenReturn(Optional.of(detalleExistente(existente)));
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setIdEvaluacion(30L);
        evaluacion.setEstadoEvaluacion("PENDIENTE_ML");
        when(evaluacionRepository.findFirstByCaso_IdCasoAndTipoEvaluacionOrderByFechaEvaluacionAsc(
                15L, "CUESTIONARIO_REGISTRO")).thenReturn(Optional.of(evaluacion));

        RegistroCasoResponse response = service.registrar(solicitud("C-1", "Nombre"), "ana");

        assertTrue(response.reintento());
        assertEquals(15L, response.idCaso());
        assertEquals(30L, response.idEvaluacion());
        verify(casoRepository, never()).save(any());
        verify(detalleCasoRepository, never()).save(any());
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void codigoExistenteConDatosDiferentesDevuelveConflictoSinReemplazarDatos() {
        when(casoRepository.findByCodigoCasoAndUsuario_Usuario("C-1", "ana"))
                .thenReturn(Optional.of(casoExistente("C-1", "Otra persona")));

        assertThrows(ConflictoRegistroException.class,
                () -> service.registrar(solicitud("C-1", "Nombre"), "ana"));
        verify(detalleCasoRepository, never()).save(any());
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void codigoExistenteConRespuestasDiferentesDevuelveConflictoSinReemplazarlas() {
        Caso existente = casoExistente("C-1", "Nombre");
        when(casoRepository.findByCodigoCasoAndUsuario_Usuario("C-1", "ana"))
                .thenReturn(Optional.of(existente));
        DetalleCaso detalle = detalleExistente(existente);
        detalle.setVariable1(false);
        when(detalleCasoRepository.findByCaso_IdCaso(15L)).thenReturn(Optional.of(detalle));

        assertThrows(ConflictoRegistroException.class,
                () -> service.registrar(solicitud("C-1", "Nombre"), "ana"));
        verify(casoRepository, never()).save(any());
        verify(detalleCasoRepository, never()).save(any());
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void codigoDeOtroUsuarioNoPermiteConsultarNiApropiarseDelCaso() {
        when(casoRepository.findByCodigoCasoAndUsuario_Usuario("C-1", "ana"))
                .thenReturn(Optional.empty());
        when(casoRepository.existsByCodigoCaso("C-1")).thenReturn(true);

        assertThrows(ConflictoRegistroException.class,
                () -> service.registrar(solicitud("C-1", "Nombre"), "ana"));
        verifyNoInteractions(detalleCasoRepository, evaluacionRepository, usuarioRepository);
    }

    @Test
    void completaUnCasoPrevioSinDetalleSinCrearUnCasoDuplicado() {
        Caso existente = casoExistente("C-1", "Nombre");
        when(casoRepository.findByCodigoCasoAndUsuario_Usuario("C-1", "ana"))
                .thenReturn(Optional.of(existente));
        when(detalleCasoRepository.findByCaso_IdCaso(15L)).thenReturn(Optional.empty());
        when(detalleCasoRepository.save(any(DetalleCaso.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(evaluacionRepository.findFirstByCaso_IdCasoAndTipoEvaluacionOrderByFechaEvaluacionAsc(
                15L, "CUESTIONARIO_REGISTRO")).thenReturn(Optional.empty());
        when(evaluacionRepository.save(any(Evaluacion.class))).thenAnswer(invocation -> {
            Evaluacion evaluacion = invocation.getArgument(0);
            evaluacion.setIdEvaluacion(31L);
            return evaluacion;
        });

        RegistroCasoResponse response = service.registrar(solicitud("C-1", "Nombre"), "ana");

        assertTrue(response.reintento());
        assertEquals(15L, response.idCaso());
        assertEquals(31L, response.idEvaluacion());
        verify(casoRepository, never()).save(any());
        verify(detalleCasoRepository).save(any(DetalleCaso.class));
        verify(evaluacionRepository).save(any(Evaluacion.class));
    }

    @Test
    void noPermiteQueElClienteMarqueUnCasoNuevoComoEvaluado() {
        CasoRequest caso = new CasoRequest("C-1", "Nombre", "Apellido", null, 28,
                null, null, null, null, EstadoCaso.EVALUADO);
        RegistroCasoRequest request = new RegistroCasoRequest(caso, respuestas());

        assertThrows(IllegalArgumentException.class, () -> service.registrar(request, "ana"));
        verifyNoInteractions(casoRepository, detalleCasoRepository, evaluacionRepository, usuarioRepository);
    }

    @Test
    void validaDatosDeCasoYPermiteNullEnIndicadoresHastaAprobarReglasFuncionales() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(solicitud("C-1", "Nombre")).isEmpty());
            DetalleCasoRequest sinRespuestas = new DetalleCasoRequest(null, null, null, null,
                    null, null, null, null, null, null, null, null);
            assertTrue(validator.validate(new RegistroCasoRequest(
                    new CasoRequest("C-2", "Nombre", "Apellido", null, 28,
                            null, null, null, null, EstadoCaso.REGISTRO), sinRespuestas)).isEmpty());

            RegistroCasoRequest sinCaso = new RegistroCasoRequest(null, respuestas());
            assertFalse(validator.validate(sinCaso).isEmpty());

            CasoRequest casoInvalido = new CasoRequest(" ", " ", " ", null, -1,
                    null, null, null, null, EstadoCaso.REGISTRO);
            RegistroCasoRequest invalido = new RegistroCasoRequest(casoInvalido, respuestas());
            assertFalse(validator.validate(invalido).isEmpty());
        }
    }

    private RegistroCasoRequest solicitud(String codigo, String nombre) {
        return new RegistroCasoRequest(new CasoRequest(codigo, nombre, "Apellido", null, 28,
                null, null, null, null, EstadoCaso.REGISTRO), respuestas());
    }

    private DetalleCasoRequest respuestas() {
        return new DetalleCasoRequest(true, false, null, true, null, false,
                true, null, false, true, false, null);
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setUsuario("ana");
        return usuario;
    }

    private Caso casoExistente(String codigo, String nombre) {
        Caso caso = new Caso();
        caso.setIdCaso(15L);
        caso.setCodigoCaso(codigo);
        caso.setNombrePersona(nombre);
        caso.setApellidoPersona("Apellido");
        caso.setEdad(28);
        caso.setEstadoCaso(EstadoCaso.REGISTRO);
        caso.setUsuario(usuario());
        return caso;
    }

    private DetalleCaso detalleExistente(Caso caso) {
        DetalleCaso detalle = new DetalleCaso();
        detalle.setCaso(caso);
        detalle.setVariable1(true);
        detalle.setVariable2(false);
        detalle.setVariable3(null);
        detalle.setVariable4(true);
        detalle.setVariable5(null);
        detalle.setVariable6(false);
        detalle.setVariable7(true);
        detalle.setVariable8(null);
        detalle.setVariable9(false);
        detalle.setVariable10(true);
        detalle.setVariable11(false);
        detalle.setVariable12(null);
        return detalle;
    }
}
