package cl.acis.usuarios.config;

import cl.acis.usuarios.model.*;
import cl.acis.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Configuration
public class DatosDemoConfig {
    @Bean
    CommandLineRunner datosDemo(UsuarioRepository r, PasswordEncoder e) {
        return a -> {
            if (r.count() > 0)
                return;
            crear(r, e, "admin@acis.cl", "Administrador ACIS", Rol.ADMINISTRADOR);
            crear(r, e, "jefe@acis.cl", "Jefatura ACIS", Rol.JEFATURA);
            crear(r, e, "tecnico@acis.cl", "Técnico ACIS", Rol.TECNICO);
            crear(r, e, "tecnico2@acis.cl", "Técnico 2 ACIS", Rol.TECNICO);
            crear(r, e, "cliente@acis.cl", "Cliente ACIS", Rol.CLIENTE);
        };
    }

    private void crear(UsuarioRepository r, PasswordEncoder e, String c, String n, Rol rol) {
        Usuario u = new Usuario();
        u.setCorreo(c);
        u.setNombre(n);
        u.setRol(rol);
        u.setPasswordHash(e.encode("Acis2026"));
        u.setAceptoTerminosEn(LocalDateTime.now());
        u.setVersionTerminos("1.0");
        r.save(u);
    }
}
