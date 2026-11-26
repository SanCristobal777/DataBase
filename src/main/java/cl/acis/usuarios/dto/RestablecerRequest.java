package cl.acis.usuarios.dto;

import jakarta.validation.constraints.*;

public record RestablecerRequest(@NotBlank @Email String correo, @NotBlank @Size(min = 6, max = 6) String codigo,
        @NotBlank String nuevaPassword) {
}
