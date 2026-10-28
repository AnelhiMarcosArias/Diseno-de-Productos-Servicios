package com.utp.sistema_feminicidio.controller;

import com.utp.sistema_feminicidio.dto.AuthUserResponse;
import com.utp.sistema_feminicidio.dto.LoginRequest;
import com.utp.sistema_feminicidio.model.Usuario;
import com.utp.sistema_feminicidio.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityContextHolderStrategy contextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    public AuthController(AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.correo(), request.contrasena()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "El correo o la contraseña no son correctos, o la cuenta está desactivada."));
        }

        if (servletRequest.getSession(false) != null) {
            servletRequest.changeSessionId();
        }
        SecurityContext context = contextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);

        Usuario usuario = usuarioRepository.findByUsuario(authentication.getName())
                .orElseThrow(() -> new AuthenticationServiceException("No se pudo cargar la cuenta autenticada"));
        usuario.setUltimoAcceso(LocalDateTime.now());
        return ResponseEntity.ok(respuesta(usuario));
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public AuthUserResponse me(Principal principal) {
        Usuario usuario = usuarioRepository.findByUsuario(principal.getName())
                .orElseThrow(() -> new AuthenticationServiceException("No se pudo cargar la cuenta autenticada"));
        return respuesta(usuario);
    }

    private AuthUserResponse respuesta(Usuario usuario) {
        return new AuthUserResponse(usuario.getIdUsuario(), usuario.getUsuario(), usuario.getCorreo(),
                usuario.getNombre() + " " + usuario.getApellido(), usuario.getRol().name());
    }
}
