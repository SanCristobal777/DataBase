package cl.acis.usuarios.dto;

import cl.acis.usuarios.model.*;
import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String correo, String nombre, Rol rol, boolean activo,
        boolean debeCambiarContrasena, boolean requiereAceptarTerminos, LocalDateTime aceptoTerminosEn,
        String versionTerminos, LocalDateTime creadoEn, LocalDateTime modificadoEn) {
}
