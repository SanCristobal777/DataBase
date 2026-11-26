package cl.acis.usuarios.repository;

import cl.acis.usuarios.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    long countByRolAndActivo(Rol rol, boolean activo);

    List<Usuario> findByRolAndActivo(Rol rol, boolean activo);

    List<Usuario> findByRol(Rol rol);

    List<Usuario> findByActivo(boolean activo);
}
