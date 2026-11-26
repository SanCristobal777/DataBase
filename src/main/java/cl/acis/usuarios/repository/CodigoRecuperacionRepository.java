package cl.acis.usuarios.repository;

import cl.acis.usuarios.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {
    Optional<CodigoRecuperacion> findTopByUsuarioAndCodigoAndUsadoFalseOrderByIdDesc(Usuario usuario, String codigo);
}
