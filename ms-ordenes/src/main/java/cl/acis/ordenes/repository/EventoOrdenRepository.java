package cl.acis.ordenes.repository;

import cl.acis.ordenes.model.EventoOrden;
import cl.acis.ordenes.model.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoOrdenRepository extends JpaRepository<EventoOrden, Long> {
    List<EventoOrden> findByOrdenOrderByFechaAsc(OrdenTrabajo orden);

    void deleteByOrden(OrdenTrabajo orden);
}
