package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.DetalleCasoRequest;
import com.utp.sistema_feminicidio.dto.DetalleCasoResponse;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.model.DetalleCaso;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.DetalleCasoRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetalleCasoService {
    private final DetalleCasoRepository detalleRepository;
    private final CasoRepository casoRepository;

    public DetalleCasoService(DetalleCasoRepository detalleRepository, CasoRepository casoRepository) {
        this.detalleRepository = detalleRepository;
        this.casoRepository = casoRepository;
    }

    @Transactional(readOnly = true)
    public DetalleCasoResponse obtenerPorCaso(Long casoId, String usuarioAutenticado) {
        casoRepository.findByIdCasoAndUsuario_Usuario(casoId, usuarioAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso", casoId));
        return detalleRepository.findByCaso_IdCaso(casoId).map(this::respuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Detalle de caso", casoId));
    }

    @Transactional
    public DetalleCasoResponse guardar(Long casoId, DetalleCasoRequest dto, String usuarioAutenticado) {
        Caso caso = casoRepository.findByIdCasoAndUsuario_Usuario(casoId, usuarioAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso", casoId));
        DetalleCaso detalle = detalleRepository.findByCaso_IdCaso(casoId).orElseGet(DetalleCaso::new);
        detalle.setCaso(caso);
        detalle.setVariable1(dto.variable1()); detalle.setVariable2(dto.variable2());
        detalle.setVariable3(dto.variable3()); detalle.setVariable4(dto.variable4());
        detalle.setVariable5(dto.variable5()); detalle.setVariable6(dto.variable6());
        detalle.setVariable7(dto.variable7()); detalle.setVariable8(dto.variable8());
        detalle.setVariable9(dto.variable9()); detalle.setVariable10(dto.variable10());
        detalle.setVariable11(dto.variable11()); detalle.setVariable12(dto.variable12());
        detalle.setFechaActualizacion(LocalDateTime.now());
        return respuesta(detalleRepository.save(detalle));
    }

    private DetalleCasoResponse respuesta(DetalleCaso d) {
        return new DetalleCasoResponse(d.getIdDetalleCaso(), d.getCaso().getIdCaso(),
                d.getVariable1(), d.getVariable2(), d.getVariable3(), d.getVariable4(),
                d.getVariable5(), d.getVariable6(), d.getVariable7(), d.getVariable8(),
                d.getVariable9(), d.getVariable10(), d.getVariable11(), d.getVariable12(), d.getFechaActualizacion());
    }
}
