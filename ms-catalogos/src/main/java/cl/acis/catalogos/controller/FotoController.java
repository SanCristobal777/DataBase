package cl.acis.catalogos.controller;

import cl.acis.catalogos.config.ActorHeaders;
import cl.acis.catalogos.dto.FotoResponse;
import cl.acis.catalogos.model.TipoFoto;
import cl.acis.catalogos.service.FotoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/fotos")
public class FotoController {
    private final FotoService service;
    private final ActorHeaders headers;

    public FotoController(FotoService service, ActorHeaders headers) {
        this.service = service;
        this.headers = headers;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FotoResponse subir(@RequestPart("archivo") MultipartFile archivo, @RequestParam Long ordenId,
            @RequestParam TipoFoto tipo, @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud, HttpServletRequest request) {
        ActorHeaders.Actor actor = headers.leer(request);
        headers.exigir(actor, "ADMINISTRADOR", "JEFATURA", "TECNICO");
        return service.subir(archivo, ordenId, tipo, actor.id(), latitud, longitud);
    }

    @GetMapping("/orden/{ordenId}")
    List<FotoResponse> listar(@PathVariable Long ordenId, HttpServletRequest request) {
        headers.leer(request);
        return service.listar(ordenId);
    }

    @GetMapping("/{id}/archivo")
    ResponseEntity<Resource> archivo(@PathVariable Long id, HttpServletRequest request) {
        headers.leer(request);
        return ResponseEntity.ok().contentType(service.tipoContenido(id)).body(service.archivo(id));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> borrar(@PathVariable Long id, HttpServletRequest request) {
        ActorHeaders.Actor actor = headers.leer(request);
        headers.exigir(actor, "ADMINISTRADOR", "JEFATURA");
        service.borrar(id);
        return ResponseEntity.noContent().build();
    }
}
