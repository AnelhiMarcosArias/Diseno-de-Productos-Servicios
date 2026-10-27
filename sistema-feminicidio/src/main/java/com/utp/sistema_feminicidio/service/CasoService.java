package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.CasoRequest;
import com.utp.sistema_feminicidio.dto.CasoResponse;
import com.utp.sistema_feminicidio.dto.CasoUpdateRequest;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.sistema_feminicidio.util.Paginacion;

@Service
public class CasoService {
    private final CasoRepository casoRepository;
    private final UsuarioRepository usuarioRepository;

    public CasoService(CasoRepository casoRepository, UsuarioRepository usuarioRepository) {
        this.casoRepository = casoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<CasoResponse> listar(EstadoCaso estado, String departamento, String provincia,
            String distrito, LocalDateTime desde, LocalDateTime hasta, String usuarioAutenticado, Pageable pageable) {
        validarRango(desde, hasta);
        return casoRepository.buscar(estado, blancoANull(departamento), blancoANull(provincia),
                blancoANull(distrito), desde, hasta, usuarioAutenticado, Paginacion.limitar(pageable)).map(this::respuesta);
    }

    @Transactional(readOnly = true)
    public CasoResponse obtener(Long id, String usuarioAutenticado) {
        return respuesta(casoRepository.findByIdCasoAndUsuario_Usuario(id, usuarioAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso", id)));
    }

    @Transactional
    public CasoResponse crear(CasoRequest solicitud, String nombreUsuarioAutenticado) {
        if (casoRepository.existsByCodigoCaso(solicitud.codigoCaso())) {
            throw new org.springframework.dao.DuplicateKeyException("El código de caso ya existe");
        }
        Usuario usuario = usuarioRepository.findByUsuario(nombreUsuarioAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado", nombreUsuarioAutenticado));
        Caso caso = new Caso();
        actualizar(caso, solicitud, usuario);
        caso.setFechaRegistro(LocalDateTime.now());
        return respuesta(casoRepository.save(caso));
    }

    @Transactional
    public CasoResponse actualizar(Long id, CasoUpdateRequest solicitud, String usuarioAutenticado) {
        Caso caso = casoRepository.findByIdCasoAndUsuario_Usuario(id, usuarioAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso", id));
        if (!caso.getCodigoCaso().equals(solicitud.codigoCaso())
                && casoRepository.existsByCodigoCaso(solicitud.codigoCaso())) {
            throw new org.springframework.dao.DuplicateKeyException("El código de caso ya existe");
        }
        caso.setCodigoCaso(solicitud.codigoCaso());
        caso.setNombrePersona(solicitud.nombrePersona());
        caso.setApellidoPersona(solicitud.apellidoPersona());
        caso.setFechaNacimiento(solicitud.fechaNacimiento());
        caso.setEdad(solicitud.edad() == null ? 0 : solicitud.edad());
        caso.setDepartamento(solicitud.departamento());
        caso.setProvincia(solicitud.provincia());
        caso.setDistrito(solicitud.distrito());
        caso.setDireccion(solicitud.direccion());
        caso.setEstadoCaso(solicitud.estadoCaso());
        return respuesta(casoRepository.save(caso));
    }

    private void actualizar(Caso caso, CasoRequest dto, Usuario usuario) {
        caso.setCodigoCaso(dto.codigoCaso());
        caso.setNombrePersona(dto.nombrePersona());
        caso.setApellidoPersona(dto.apellidoPersona());
        caso.setFechaNacimiento(dto.fechaNacimiento());
        caso.setEdad(dto.edad() == null ? 0 : dto.edad());
        caso.setDepartamento(dto.departamento());
        caso.setProvincia(dto.provincia());
        caso.setDistrito(dto.distrito());
        caso.setDireccion(dto.direccion());
        caso.setEstadoCaso(dto.estadoCaso());
        caso.setUsuario(usuario);
    }

    private CasoResponse respuesta(Caso c) {
        return new CasoResponse(c.getIdCaso(), c.getCodigoCaso(), c.getFechaRegistro(), c.getNombrePersona(),
                c.getApellidoPersona(), c.getFechaNacimiento(), c.getEdad(), c.getDepartamento(),
                c.getProvincia(), c.getDistrito(), c.getDireccion(), c.getEstadoCaso());
    }

    private void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }
    }

    private String blancoANull(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
