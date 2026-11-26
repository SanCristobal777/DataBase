package cl.acis.usuarios.dto;

public record LoginResponse(UsuarioResponse usuario, boolean debeCambiarContrasena, boolean requiereAceptarTerminos,
        String token) {
}
