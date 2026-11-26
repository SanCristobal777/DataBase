package cl.acis.usuarios.config;

import cl.acis.usuarios.exception.*;
import cl.acis.usuarios.model.*;
import cl.acis.usuarios.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ActorComponent {
    public static final String HEADER = "X-Usuario-Id";
    private final UsuarioRepository repo;

    public ActorComponent(UsuarioRepository r) {
        repo = r;
    }

    public Usuario admin(HttpServletRequest req) {
        Usuario u = buscar(req);
        if (!u.isActivo())
            throw new NoAutorizadoException("El usuario actor está inactivo");
        if (u.getRol() != Rol.ADMINISTRADOR)
            throw new ProhibidoException("Se requiere rol ADMINISTRADOR");
        return u;
    }

    public Usuario propio(HttpServletRequest req, Long id) {
        Usuario u = buscar(req);
        if (!u.getId().equals(id))
            throw new ProhibidoException("Solo puedes operar sobre tu propia cuenta");
        return u;
    }

    private Usuario buscar(HttpServletRequest req) {
        String h = req.getHeader(HEADER);
        if (h == null || h.isBlank())
            throw new NoAutorizadoException("Falta el header X-Usuario-Id");
        try {
            return repo.findById(Long.valueOf(h))
                    .orElseThrow(() -> new NoAutorizadoException("El usuario actor no existe"));
        } catch (NumberFormatException e) {
            throw new NoAutorizadoException("X-Usuario-Id debe ser numérico");
        }
    }
}
