package cl.acis.catalogos.service;

import cl.acis.catalogos.exception.ReglaNegocioException;
import cl.acis.catalogos.model.TipoFoto;
import cl.acis.catalogos.repository.FotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FotoServiceTest {
    @TempDir
    Path uploads;

    @Test
    void rechazaArchivoConExtensionPermitidaPeroContenidoInvalido() {
        FotoRepository repository = Mockito.mock(FotoRepository.class);
        FotoService service = new FotoService(repository, uploads.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.png", "image/png",
                "no es una imagen".getBytes());

        assertThrows(ReglaNegocioException.class,
                () -> service.subir(archivo, 1L, TipoFoto.ANTES, 2L, null, null));
    }
}
