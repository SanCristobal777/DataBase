package cl.acis.usuarios.dto; import jakarta.validation.constraints.NotBlank; public record CambiarPasswordObligatorioRequest(@NotBlank String nuevaPassword) {}
