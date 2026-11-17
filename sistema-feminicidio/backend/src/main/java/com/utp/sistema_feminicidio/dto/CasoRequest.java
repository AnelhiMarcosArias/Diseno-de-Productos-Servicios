package com.utp.sistema_feminicidio.dto;

import com.utp.sistema_feminicidio.model.EstadoCaso;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CasoRequest(
        @NotBlank @Size(max = 50) String codigoCaso,
        @NotBlank @Size(max = 100) String nombrePersona,
        @NotBlank @Size(max = 100) String apellidoPersona,
        @Past LocalDate fechaNacimiento,
        @PositiveOrZero Integer edad,
        @Size(max = 100) String departamento,
        @Size(max = 100) String provincia,
        @Size(max = 100) String distrito,
        @Size(max = 300) String direccion,
        @NotNull EstadoCaso estadoCaso) {}
