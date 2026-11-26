package cl.acis.usuarios.service;

import cl.acis.usuarios.dto.*;
import cl.acis.usuarios.exception.*;
import cl.acis.usuarios.model.*;
import cl.acis.usuarios.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    @Mock
    UsuarioRepository usuarios;
    @Mock
    CodigoRecuperacionRepository codigos;
    @Mock
    PasswordEncoder encoder;
    @Mock
    PoliticaContrasena politica;
    UsuarioService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        service = new UsuarioService(usuarios, codigos, encoder, politica);
        when(encoder.encode(anyString())).thenReturn("hash");
        when(usuarios.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void registraClienteYNoExponeHash() {
        when(usuarios.existsByCorreoIgnoreCase(anyString())).thenReturn(false);
        UsuarioResponse r = service.registrar(new RegistroRequest("a@acis.cl", "Ana", "Acis2026", true));
        assertEquals(Rol.CLIENTE, r.rol());
        assertEquals("a@acis.cl", r.correo());
        verify(politica).validar("Acis2026");
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarios.existsByCorreoIgnoreCase("a@acis.cl")).thenReturn(true);
        assertThrows(ConflictoException.class,
                () -> service.registrar(new RegistroRequest("a@acis.cl", "Ana", "Acis2026", true)));
    }

    @Test
    void loginIncorrectoLanzaNoAutorizado() {
        Usuario u = new Usuario();
        u.setCorreo("a@acis.cl");
        u.setPasswordHash("hash");
        u.setActivo(true);
        when(usuarios.findByCorreoIgnoreCase("a@acis.cl")).thenReturn(Optional.of(u));
        when(encoder.matches("mala", "hash")).thenReturn(false);
        assertThrows(NoAutorizadoException.class, () -> service.login(new LoginRequest("a@acis.cl", "mala")));
        assertEquals(1, u.getIntentosFallidos());
    }

    @Test
    void bloqueaEnQuintoIntento() {
        Usuario u = new Usuario();
        u.setCorreo("a@acis.cl");
        u.setPasswordHash("hash");
        u.setActivo(true);
        u.setIntentosFallidos(4);
        when(usuarios.findByCorreoIgnoreCase("a@acis.cl")).thenReturn(Optional.of(u));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);
        assertThrows(NoAutorizadoException.class, () -> service.login(new LoginRequest("a@acis.cl", "mala")));
        assertNotNull(u.getBloqueadoHasta());
        assertEquals(0, u.getIntentosFallidos());
    }

    @Test
    void loginCorrectoReiniciaIntentos() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setCorreo("a@acis.cl");
        u.setPasswordHash("hash");
        u.setActivo(true);
        u.setIntentosFallidos(2);
        when(usuarios.findByCorreoIgnoreCase(anyString())).thenReturn(Optional.of(u));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        assertEquals(1L, service.login(new LoginRequest("a@acis.cl", "Acis2026")).usuario().id());
        assertEquals(0, u.getIntentosFallidos());
    }
}
