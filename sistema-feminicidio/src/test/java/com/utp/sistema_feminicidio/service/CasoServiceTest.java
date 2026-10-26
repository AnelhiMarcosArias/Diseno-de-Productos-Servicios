package com.utp.sistema_feminicidio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.utp.sistema_feminicidio.dto.CasoRequest;
import com.utp.sistema_feminicidio.dto.CasoResponse;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CasoServiceTest {
    @Mock CasoRepository casoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks CasoService service;

    @Test
    void crearAsociaUsuarioYGeneraFechaEnServidor() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7L);
        when(casoRepository.existsByCodigoCaso("C-1")).thenReturn(false);
        when(usuarioRepository.findByUsuario("ana")).thenReturn(Optional.of(usuario));
        when(casoRepository.save(any())).thenAnswer(invocation -> {
            var caso = invocation.<com.utp.sistema_feminicidio.model.Caso>getArgument(0);
            caso.setIdCaso(21L);
            return caso;
        });

        CasoRequest request = new CasoRequest("C-1", "Nombre", "Apellido", LocalDate.of(1990, 1, 1),
                36, "Lima", "Lima", "Centro", null, EstadoCaso.REGISTRO);
        CasoResponse response = service.crear(request, "ana");

        assertEquals(21L, response.idCaso());
        assertNotNull(response.fechaRegistro());
        verify(casoRepository).save(any());
    }

    @Test
    void obtenerLanzaNotFoundSiNoExiste() {
        when(casoRepository.findByIdCasoAndUsuario_Usuario(404L, "ana")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(404L, "ana"));
    }
}
