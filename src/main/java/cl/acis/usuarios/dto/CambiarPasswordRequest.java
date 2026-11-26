package cl.acis.usuarios.dto; import jakarta.validation.constraints.NotBlank; public record CambiarPasswordRequest(@NotBlank String passwordActual,@NotBlank String nuevaPassword) {}
