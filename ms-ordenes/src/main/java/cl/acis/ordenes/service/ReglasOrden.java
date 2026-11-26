package cl.acis.ordenes.service;

import cl.acis.ordenes.exception.ReglaNegocioException;
import cl.acis.ordenes.model.AccionOrden;
import cl.acis.ordenes.model.EstadoOrden;
import cl.acis.ordenes.model.OrdenTrabajo;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class ReglasOrden {
    public Set<AccionOrden> disponibles(OrdenTrabajo orden, String rol, Long actor) {
        Set<AccionOrden> acciones = EnumSet.noneOf(AccionOrden.class);
        boolean admin = rol.equals("ADMINISTRADOR");
        boolean jefatura = admin || rol.equals("JEFATURA");

        if (orden.getEstado() == EstadoOrden.CREADA && jefatura) {
            acciones.add(AccionOrden.ASIGNAR);
        }
        if (orden.getEstado() == EstadoOrden.ASIGNADA && rol.equals("TECNICO")
                && actor.equals(orden.getTecnicoId())) {
            acciones.add(AccionOrden.ACEPTAR);
            acciones.add(AccionOrden.RECHAZAR);
        }
        if (orden.getEstado() == EstadoOrden.ACEPTADA && rol.equals("TECNICO")
                && actor.equals(orden.getTecnicoId())) {
            acciones.add(AccionOrden.CHECK_IN);
        }
        if (orden.getEstado() == EstadoOrden.EN_EJECUCION && rol.equals("TECNICO")
                && actor.equals(orden.getTecnicoId())) {
            acciones.add(AccionOrden.CHECK_OUT);
        }
        if (orden.getEstado() == EstadoOrden.CERRADA
                && (jefatura || (rol.equals("CLIENTE") && actor.equals(orden.getClienteId())))) {
            acciones.add(AccionOrden.VALIDAR);
            acciones.add(AccionOrden.OBSERVAR);
        }
        if (orden.getEstado() == EstadoOrden.OBSERVADA && jefatura) {
            acciones.add(AccionOrden.REABRIR);
        }
        if (jefatura && (orden.getEstado() == EstadoOrden.CREADA
                || orden.getEstado() == EstadoOrden.ASIGNADA
                || orden.getEstado() == EstadoOrden.ACEPTADA)) {
            acciones.add(AccionOrden.REPROGRAMAR);
        }
        if (jefatura && !orden.getEstado().equals(EstadoOrden.VALIDADA)
                && !orden.getEstado().equals(EstadoOrden.CANCELADA)
                && orden.getEstado() != EstadoOrden.CERRADA) {
            acciones.add(AccionOrden.CANCELAR);
        }
        return acciones;
    }

    public void exigir(OrdenTrabajo orden, AccionOrden accion, String rol, Long actor) {
        if (!disponibles(orden, rol, actor).contains(accion)) {
            throw new ReglaNegocioException("La acción no está permitida para el estado y rol actuales");
        }
    }
}
