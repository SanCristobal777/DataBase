package cl.acis.ordenes.repository;

import cl.acis.ordenes.model.EstadoOrden;
import cl.acis.ordenes.model.OrdenTrabajo;
import cl.acis.ordenes.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<OrdenTrabajo, Long> {
    Optional<OrdenTrabajo> findTopByOrderByIdDesc();

    List<OrdenTrabajo> findByEstado(EstadoOrden estado);

    List<OrdenTrabajo> findByTecnicoId(Long id);

    List<OrdenTrabajo> findByClienteId(Long id);

    @Query("""
            select o from OrdenTrabajo o
            where (:estado is null or o.estado = :estado)
              and (:tecnico is null or o.tecnicoId = :tecnico)
              and (:cliente is null or o.clienteId = :cliente)
              and (:prioridad is null or o.prioridad = :prioridad)
              and (:fechaInicio is null or (o.fechaProgramada >= :fechaInicio and o.fechaProgramada < :fechaFin))
              and (:admin = true or (:jefatura = true) or (:clienteActor = true and o.clienteId = :actorId)
                   or (:tecnicoActor = true and o.tecnicoId = :actorId))
            """)
    List<OrdenTrabajo> buscar(@Param("estado") EstadoOrden estado, @Param("tecnico") Long tecnico,
            @Param("cliente") Long cliente, @Param("prioridad") Prioridad prioridad,
            @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin,
            @Param("admin") boolean admin, @Param("jefatura") boolean jefatura,
            @Param("clienteActor") boolean clienteActor, @Param("tecnicoActor") boolean tecnicoActor,
            @Param("actorId") Long actorId);

    @Query("""
            select count(o) from OrdenTrabajo o
            where o.tecnicoId = :tecnico
              and o.fechaProgramada >= :inicio and o.fechaProgramada < :fin
              and o.estado not in :estados
              and (:ordenId is null or o.id <> :ordenId)
            """)
    long contarOcupadas(@Param("tecnico") Long tecnico, @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin, @Param("estados") List<EstadoOrden> estados,
            @Param("ordenId") Long ordenId);
}
