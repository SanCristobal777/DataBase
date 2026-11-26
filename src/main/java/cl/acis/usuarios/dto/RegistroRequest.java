package cl.acis.usuarios.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String correo,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "La contraseña es obligatoria") String password,
        @AssertTrue(message = "Debes aceptar los términos y condiciones") boolean aceptoTerminos) {
}
