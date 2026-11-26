package cl.acis.usuarios.controller;

import cl.acis.usuarios.config.ActorComponent;
import cl.acis.usuarios.dto.*;
import cl.acis.usuarios.model.Rol;
import cl.acis.usuarios.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;
    private final ActorComponent actor;

    @PostMapping("/registro")
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest r) {
        return service.registrar(r);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) {
        return service.login(r);
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean activo, HttpServletRequest q) {
        return service.listar(actor.admin(q) == null ? null : rol, activo);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public CrearUsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest r, HttpServletRequest q) {
        actor.admin(q);
        return service.crear(r);
    }

    @PutMapping("/{id}")
    public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody EditarUsuarioRequest r,
            HttpServletRequest q) {
        actor.admin(q);
        return service.editar(id, r);
    }

    @PatchMapping("/{id}/rol")
    public UsuarioResponse rol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest r, HttpServletRequest q) {
        return service.cambiarRol(id, r.rol(), actor.admin(q));
    }

    @PatchMapping("/{id}/activo")
    public UsuarioResponse activo(@PathVariable Long id, @Valid @RequestBody EstadoRequest r, HttpServletRequest q) {
        return service.cambiarActivo(id, r.activo(), actor.admin(q));
    }

    @PostMapping("/recuperar")
    public RecuperarResponse recuperar(@Valid @RequestBody RecuperarRequest r) {
        return service.recuperar(r.correo());
    }

    @PostMapping("/restablecer")
    public ResponseEntity<Void> restablecer(@Valid @RequestBody RestablecerRequest r) {
        service.restablecer(r);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cambiar-password")
    public ResponseEntity<Void> cambiar(@PathVariable Long id, @Valid @RequestBody CambiarPasswordRequest r,
            HttpServletRequest q) {
        actor.propio(q, id);
        service.cambiarPassword(id, r);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cambiar-password-obligatorio")
    public ResponseEntity<Void> obligatorio(@PathVariable Long id,
            @Valid @RequestBody CambiarPasswordObligatorioRequest r, HttpServletRequest q) {
        actor.propio(q, id);
        service.cambiarObligatorio(id, r);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/aceptar-terminos")
    public UsuarioResponse terminos(@PathVariable Long id, @Valid @RequestBody AceptarTerminosRequest r,
            HttpServletRequest q) {
        actor.propio(q, id);
        return service.aceptar(id, r.version());
    }
}
