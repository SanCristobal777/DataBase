package cl.acis.catalogos.service;

import cl.acis.catalogos.dto.FotoResponse;
import cl.acis.catalogos.exception.ReglaNegocioException;
import cl.acis.catalogos.exception.RecursoNoEncontradoException;
import cl.acis.catalogos.model.FotoEvidencia;
import cl.acis.catalogos.model.TipoFoto;
import cl.acis.catalogos.repository.FotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FotoService {
    private final FotoRepository repo;
    private final Path dir;

    public FotoService(FotoRepository repo, @Value("${acis.uploads-dir:uploads}") String directory) {
        this.repo = repo;
        dir = Paths.get(directory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear uploads", e);
        }
    }

    public FotoResponse subir(MultipartFile archivo, Long orden, TipoFoto tipo, Long actor, Double lat,
            Double lon) {
        if (archivo.isEmpty()) {
            throw new ReglaNegocioException("El archivo está vacío");
        }
        String nombre = Objects.requireNonNullElse(archivo.getOriginalFilename(), "").toLowerCase();
        if (!(nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".png"))) {
            throw new ReglaNegocioException("Solo se permiten archivos JPG y PNG");
        }
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new ReglaNegocioException("La imagen supera el límite de 5 MB");
        }
        String extension = detectarTipo(archivo);
        String guardado = UUID.randomUUID() + extension;
        Path destino = dir.resolve(guardado).normalize();
        try {
            if (!destino.getParent().equals(dir)) {
                throw new ReglaNegocioException("Ruta de archivo no válida");
            }
            archivo.transferTo(destino);
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo guardar la imagen");
        }
        FotoEvidencia foto = new FotoEvidencia();
        foto.setOrdenId(orden);
        foto.setNombreArchivo(archivo.getOriginalFilename());
        foto.setRutaArchivo(guardado);
        foto.setTipo(tipo);
        foto.setSubidaPorId(actor);
        foto.setLatitud(lat);
        foto.setLongitud(lon);
        return dto(repo.save(foto));
    }

    public List<FotoResponse> listar(Long orden) {
        return repo.findByOrdenId(orden).stream().map(this::dto).toList();
    }

    public Resource archivo(Long id) {
        FotoEvidencia foto = get(id);
        Resource resource = new FileSystemResource(dir.resolve(Paths.get(foto.getRutaArchivo())).normalize());
        if (!resource.exists()) {
            throw new RecursoNoEncontradoException("Archivo no encontrado");
        }
        return resource;
    }

    public MediaType tipoContenido(Long id) {
        return get(id).getRutaArchivo().toLowerCase().endsWith(".png")
                ? MediaType.IMAGE_PNG
                : MediaType.IMAGE_JPEG;
    }

    public String nombre(Long id) {
        return get(id).getNombreArchivo();
    }

    public void borrar(Long id) {
        FotoEvidencia foto = get(id);
        try {
            Files.deleteIfExists(dir.resolve(Paths.get(foto.getRutaArchivo())).normalize());
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo borrar el archivo");
        }
        repo.delete(foto);
    }

    private String detectarTipo(MultipartFile archivo) {
        try (InputStream input = archivo.getInputStream()) {
            byte[] bytes = input.readNBytes(8);
            boolean png = bytes.length >= 8
                    && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
                    && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a;
            if (png) {
                return ".png";
            }
            if (bytes.length >= 3 && bytes[0] == (byte) 0xff && bytes[1] == (byte) 0xd8
                    && bytes[2] == (byte) 0xff) {
                return ".jpg";
            }
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo leer la imagen");
        }
        throw new ReglaNegocioException("El contenido no corresponde a una imagen JPG o PNG");
    }

    private FotoEvidencia get(Long id) {
        return repo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Foto no encontrada"));
    }

    private FotoResponse dto(FotoEvidencia foto) {
        return new FotoResponse(foto.getId(), foto.getOrdenId(), foto.getNombreArchivo(), foto.getTipo(),
                foto.getLatitud(), foto.getLongitud(), foto.getSubidaPorId(), foto.getCreadaEn());
    }
}
