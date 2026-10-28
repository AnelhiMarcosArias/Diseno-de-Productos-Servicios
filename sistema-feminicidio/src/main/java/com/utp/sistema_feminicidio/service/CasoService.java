package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.CasoRequest;
import com.utp.sistema_feminicidio.dto.CasoResumenResponse;
import com.utp.sistema_feminicidio.dto.CasoUpdateRequest;
import com.utp.sistema_feminicidio.exception.RecursoNoEncontradoException;
import com.utp.sistema_feminicidio.model.Caso;
import com.utp.sistema_feminicidio.model.EstadoCaso;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.CasoRepository;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;
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
    public Page<CasoResumenResponse> listar(EstadoCaso estado, String departamento, String provincia,
            String distrito, LocalDateTime desde, LocalDateTime hasta, String usuarioAutenticado, Pageable pageable) {
        validarRango(desde, hasta);
        String departamentoFiltro = blancoANull(departamento);
        String provinciaFiltro = blancoANull(provincia);
        String distritoFiltro = blancoANull(distrito);
        var specification = (org.springframework.data.jpa.domain.Specification<Caso>) (root, query, criteria) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(criteria.equal(root.get("usuario").get("usuario"), usuarioAutenticado));
            if (estado != null) predicates.add(criteria.equal(root.get("estadoCaso"), estado));
            if (departamentoFiltro != null) predicates.add(criteria.equal(
                    criteria.lower(root.get("departamento")), departamentoFiltro.toLowerCase(Locale.ROOT)));
            if (provinciaFiltro != null) predicates.add(criteria.equal(
                    criteria.lower(root.get("provincia")), provinciaFiltro.toLowerCase(Locale.ROOT)));
            if (distritoFiltro != null) predicates.add(criteria.equal(
                    criteria.lower(root.get("distrito")), distritoFiltro.toLowerCase(Locale.ROOT)));
            if (desde != null) predicates.add(criteria.greaterThanOrEqualTo(root.get("fechaRegistro"), desde));
            if (hasta != null) predicates.add(criteria.lessThanOrEqualTo(root.get("fechaRegistro"), hasta));
            return criteria.and(predicates.toArray(Predicate[]::new));
        };
        return casoRepository.findAll(specification, Paginacion.limitar(pageable)).map(this::respuesta);
    }

    @Transactional(readOnly = true)
    public CasoResumenResponse obtener(Long id, String usuarioAutenticado) {
        return respuesta(casoRepository.findByIdCasoAndUsuario_Usuario(id, usuarioAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caso", id)));
    }

    @Transactional
    public CasoResumenResponse crear(CasoRequest solicitud, String nombreUsuarioAutenticado) {
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
    public CasoResumenResponse actualizar(Long id, CasoUpdateRequest solicitud, String usuarioAutenticado) {
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

    private CasoResumenResponse respuesta(Caso c) {
        return new CasoResumenResponse(c.getIdCaso(), c.getCodigoCaso(), c.getFechaRegistro(), c.getEstadoCaso());
    }

    private void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }
    }

    private String blancoANull(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
