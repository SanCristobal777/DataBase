package cl.acis.ordenes.service;

import cl.acis.ordenes.dto.*;
import cl.acis.ordenes.exception.*;
import cl.acis.ordenes.model.*;
import cl.acis.ordenes.repository.EventoOrdenRepository;
import cl.acis.ordenes.repository.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrdenService {
    private static final List<EstadoOrden> ESTADOS_NO_OCUPADOS = List.of(EstadoOrden.CANCELADA,
            EstadoOrden.VALIDADA);

    private final OrdenRepository repo;
    private final EventoOrdenRepository eventos;
    private final ReglasOrden reglas;

    public OrdenService(OrdenRepository repo, EventoOrdenRepository eventos, ReglasOrden reglas) {
        this.repo = repo;
        this.eventos = eventos;
        this.reglas = reglas;
    }

    public OrdenResponse crear(OrdenRequest request, Long actor) {
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setCodigo(siguiente());
        orden.setClienteId(request.clienteId());
        orden.setEquipoId(request.equipoId());
        orden.setTipoServicio(request.tipoServicio());
        orden.setPrioridad(request.prioridad());
        orden.setSlaHoras(sla(request.prioridad()));
        orden.setFechaProgramada(request.fechaProgramada());
        orden.setDescripcion(request.descripcion());
        orden.setComentarioCliente(request.comentarioCliente());
        repo.save(orden);
        eventos.save(new EventoOrden(orden, TipoEvento.CREACION, null, EstadoOrden.CREADA, actor, "Orden creada"));
        return dto(orden);
    }

    public List<OrdenResponse> listar(EstadoOrden estado, Long tecnico, Long cliente, LocalDate fecha,
            Prioridad prioridad, ActorFilter actor) {
        LocalDateTime inicio = fecha == null ? null : fecha.atStartOfDay();
        LocalDateTime fin = fecha == null ? null : fecha.plusDays(1).atStartOfDay();
        boolean admin = actor.rol().equals("ADMINISTRADOR");
        boolean jefatura = actor.rol().equals("JEFATURA");
        boolean clienteActor = actor.rol().equals("CLIENTE");
        boolean tecnicoActor = actor.rol().equals("TECNICO");
        return repo.buscar(estado, tecnico, cliente, prioridad, inicio, fin, admin, jefatura, clienteActor,
                tecnicoActor, actor.id()).stream().map(this::dto).toList();
    }

    public OrdenResponse obtener(Long id, ActorFilter actor) {
        OrdenTrabajo orden = get(id);
        if (!visible(orden, actor)) {
            throw new ProhibidoException("No puedes ver esta orden");
        }
        return dto(orden);
    }

    public List<EventoResponse> eventos(Long id) {
        return eventos.findByOrdenOrderByFechaAsc(get(id)).stream()
                .map(e -> new EventoResponse(e.getId(), e.getTipo(), e.getEstadoAnterior(), e.getEstadoNuevo(),
                        e.getActorId(), e.getDetalle(), e.getFecha()))
                .toList();
    }

    public Set<AccionOrden> disponibles(Long id, String rol, Long actor) {
        return reglas.disponibles(get(id), rol, actor);
    }

    public OrdenResponse editar(Long id, OrdenRequest request, Long actor, String rol) {
        if (!rol.equals("ADMINISTRADOR") && !rol.equals("JEFATURA")) {
            throw new ProhibidoException("Solo JEFATURA o ADMINISTRADOR puede editar");
        }
        OrdenTrabajo orden = get(id);
        orden.setClienteId(request.clienteId());
        orden.setEquipoId(request.equipoId());
        orden.setTipoServicio(request.tipoServicio());
        orden.setPrioridad(request.prioridad());
        orden.setSlaHoras(sla(request.prioridad()));
        orden.setFechaProgramada(request.fechaProgramada());
        orden.setDescripcion(request.descripcion());
        orden.setComentarioCliente(request.comentarioCliente());
        eventos.save(new EventoOrden(orden, TipoEvento.EDICION, orden.getEstado(), orden.getEstado(), actor,
                "Datos editados"));
        return dto(orden);
    }

    public void eliminar(Long id, Long actor, String rol) {
        if (!rol.equals("ADMINISTRADOR") && !rol.equals("JEFATURA")) {
            throw new ProhibidoException("Solo JEFATURA o ADMINISTRADOR puede eliminar");
        }
        OrdenTrabajo orden = get(id);
        if (orden.getEstado() != EstadoOrden.CREADA) {
            throw new ReglaNegocioException("Solo se puede eliminar una OT CREADA");
        }
        eventos.deleteByOrden(orden);
        repo.delete(orden);
    }

    public OrdenResponse accion(Long id, AccionRequest request, String rol, Long actor) {
        OrdenTrabajo orden = get(id);
        reglas.exigir(orden, request.accion(), rol, actor);
        EstadoOrden antes = orden.getEstado();
        String detalle = request.motivo();
        switch (request.accion()) {
            case ASIGNAR -> asignar(orden, request, actor);
            case ACEPTAR -> orden.setEstado(EstadoOrden.ACEPTADA);
            case RECHAZAR -> {
                orden.setEstado(EstadoOrden.CREADA);
                orden.setTecnicoId(null);
            }
            case CHECK_IN -> {
                orden.setEstado(EstadoOrden.EN_EJECUCION);
                orden.setCheckInEn(LocalDateTime.now());
                orden.setCheckInLat(request.lat());
                orden.setCheckInLon(request.lon());
            }
            case CHECK_OUT -> checkOut(orden, request);
            case VALIDAR -> orden.setEstado(EstadoOrden.VALIDADA);
            case OBSERVAR -> {
                exigirMotivo(request, "OBSERVAR exige un comentario");
                orden.setEstado(EstadoOrden.OBSERVADA);
            }
            case REABRIR -> {
                detalle = "Atención anterior: diagnóstico=" + orden.getDiagnostico() + ", actividades="
                        + orden.getActividades() + ", materiales=" + orden.getMateriales();
                orden.setEstado(EstadoOrden.ASIGNADA);
                orden.setDiagnostico(null);
                orden.setActividades(null);
                orden.setMateriales(null);
            }
            case REPROGRAMAR -> reprogramar(orden, request);
            case CANCELAR -> {
                exigirMotivo(request, "CANCELAR exige un motivo");
                orden.setEstado(EstadoOrden.CANCELADA);
            }
        }
        repo.save(orden);
        eventos.save(new EventoOrden(orden, TipoEvento.valueOf(request.accion().name()), antes, orden.getEstado(), actor,
                detalle));
        return dto(orden);
    }

    private void asignar(OrdenTrabajo orden, AccionRequest request, Long actor) {
        if (request.tecnicoId() == null) {
            throw new ReglaNegocioException("ASIGNAR exige técnico");
        }
        LocalDate fecha = Optional.ofNullable(orden.getFechaProgramada()).orElse(LocalDateTime.now()).toLocalDate();
        long ocupadas = repo.contarOcupadas(request.tecnicoId(), fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(),
                ESTADOS_NO_OCUPADOS, null);
        if (ocupadas >= 6) {
            throw new ReglaNegocioException("El técnico ya tiene el máximo de 6 OT para ese día");
        }
        orden.setTecnicoId(request.tecnicoId());
        orden.setEstado(EstadoOrden.ASIGNADA);
    }

    private void reprogramar(OrdenTrabajo orden, AccionRequest request) {
        exigirMotivo(request, "REPROGRAMAR exige un motivo");
        if (request.fecha() == null || request.fecha().toLocalDate().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La nueva fecha no puede ser anterior a hoy");
        }
        if (orden.getEstado() == EstadoOrden.ASIGNADA || orden.getEstado() == EstadoOrden.ACEPTADA) {
            orden.setEstado(EstadoOrden.ASIGNADA);
        }
        if (orden.getTecnicoId() != null) {
            LocalDate fecha = request.fecha().toLocalDate();
            long ocupadas = repo.contarOcupadas(orden.getTecnicoId(), fecha.atStartOfDay(),
                    fecha.plusDays(1).atStartOfDay(), ESTADOS_NO_OCUPADOS, orden.getId());
            if (ocupadas >= 6) {
                throw new ReglaNegocioException("El técnico ya tiene el máximo de 6 OT para ese día");
            }
        }
        orden.setFechaProgramada(request.fecha());
    }

    private void checkOut(OrdenTrabajo orden, AccionRequest request) {
        if (request.diagnostico() == null || request.diagnostico().isBlank() || request.actividades() == null
                || request.actividades().isBlank() || request.cantidadFotosEvidencia() == null
                || request.cantidadFotosEvidencia() < 1) {
            throw new ReglaNegocioException("CHECK_OUT exige diagnóstico, actividades y al menos una foto de evidencia");
        }
        orden.setEstado(EstadoOrden.CERRADA);
        orden.setCheckOutEn(LocalDateTime.now());
        orden.setCheckOutLat(request.lat());
        orden.setCheckOutLon(request.lon());
        orden.setDiagnostico(request.diagnostico());
        orden.setActividades(request.actividades());
        orden.setMateriales(request.materiales());
    }

    private void exigirMotivo(AccionRequest request, String mensaje) {
        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    private int sla(Prioridad prioridad) {
        return switch (prioridad) {
            case URGENTE -> 4;
            case ALTA -> 8;
            case MEDIA -> 24;
            case BAJA -> 72;
        };
    }

    private String siguiente() {
        long siguiente = repo.findTopByOrderByIdDesc().map(o -> o.getId() + 1).orElse(1L);
        return String.format("OT-%04d", siguiente);
    }

    private OrdenTrabajo get(Long id) {
        return repo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
    }

    private boolean visible(OrdenTrabajo orden, ActorFilter actor) {
        return actor.rol().equals("ADMINISTRADOR") || actor.rol().equals("JEFATURA")
                || actor.rol().equals("CLIENTE") && actor.id().equals(orden.getClienteId())
                || actor.rol().equals("TECNICO") && actor.id().equals(orden.getTecnicoId());
    }

    public OrdenResponse dto(OrdenTrabajo orden) {
        return new OrdenResponse(orden.getId(), orden.getCodigo(), orden.getClienteId(), orden.getEquipoId(),
                orden.getTipoServicio(), orden.getPrioridad(), orden.getEstado(), orden.getTecnicoId(),
                orden.getCreadoPorId(), orden.getFechaProgramada(), orden.getDescripcion(), orden.getComentarioCliente(),
                orden.getSlaHoras(), orden.getCheckInEn(), orden.getCheckOutEn(), orden.getDiagnostico(),
                orden.getActividades(), orden.getMateriales(), orden.getCreadoEn(), orden.getModificadoEn());
    }

    public MetricaResponse metricas() {
        List<OrdenTrabajo> ordenes = repo.findAll();
        Map<String, Long> estados = ordenes.stream()
                .collect(Collectors.groupingBy(o -> o.getEstado().name(), Collectors.counting()));
        long fuera = ordenes.stream().filter(o -> o.getCreadoEn() != null
                && Duration.between(o.getCreadoEn(), LocalDateTime.now()).toHours() > o.getSlaHoras()
                && o.getEstado() != EstadoOrden.VALIDADA && o.getEstado() != EstadoOrden.CANCELADA).count();
        double promedio = ordenes.stream().filter(o -> o.getCheckInEn() != null && o.getCheckOutEn() != null)
                .mapToLong(o -> Duration.between(o.getCheckInEn(), o.getCheckOutEn()).toMinutes()).average()
                .orElse(0) / 60.0;
        return new MetricaResponse(estados, fuera, promedio);
    }

    public record ActorFilter(Long id, String rol) {
    }
}
