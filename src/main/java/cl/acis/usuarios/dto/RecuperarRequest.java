package cl.acis.usuarios.dto;

import jakarta.validation.constraints.*;

public record RecuperarRequest(@NotBlank @Email String correo) {
}
