package cl.acis.usuarios.dto;

import cl.acis.usuarios.model.Rol;
import jakarta.validation.constraints.*;

public record CambiarRolRequest(@NotNull Rol rol) {
}
