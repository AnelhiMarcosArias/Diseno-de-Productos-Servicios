package com.utp.sistema_feminicidio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.utp.sistema_feminicidio.dto.DetalleCasoRequest;
import com.utp.sistema_feminicidio.dto.DetalleCasoResponse;
import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.DetalleCasoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DetalleCasoServiceTest {
    @Mock DetalleCasoRepository detalleRepository;
    @Mock CasoRepository casoRepository;
    @InjectMocks DetalleCasoService service;

    @Test
    void guardarPersisteLasDoceVariablesParaElCaso() {
        Caso caso = new Caso();
        caso.setIdCaso(9L);
        when(casoRepository.findByIdCasoAndUsuario_Usuario(9L, "ana")).thenReturn(Optional.of(caso));
        when(detalleRepository.findByCaso_IdCaso(9L)).thenReturn(Optional.empty());
        when(detalleRepository.save(any())).thenAnswer(invocation -> {
            var detalle = invocation.<com.utp.sistema_feminicidio.model.DetalleCaso>getArgument(0);
            return detalle;
        });

        DetalleCasoRequest request = new DetalleCasoRequest(true, false, true, false, true, false,
                true, false, true, false, true, false);
        DetalleCasoResponse response = service.guardar(9L, request, "ana");

        assertEquals(9L, response.casoId());
        assertTrue(response.variable1());
        assertFalse(response.variable12());
        assertNotNull(response.fechaActualizacion());
    }
}
