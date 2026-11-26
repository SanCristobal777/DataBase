package cl.acis.usuarios.dto;

import jakarta.validation.constraints.*;

public record EditarUsuarioRequest(@NotBlank @Email String correo, @NotBlank String nombre) {
}
