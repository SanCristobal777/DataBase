package cl.acis.usuarios.dto;

import cl.acis.usuarios.model.Rol;
import jakarta.validation.constraints.*;

public record CrearUsuarioRequest(@NotBlank @Email String correo, @NotBlank String nombre, @NotNull Rol rol) {
}
