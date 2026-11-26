package cl.acis.usuarios.controller;

import cl.acis.usuarios.config.ActorComponent;
import cl.acis.usuarios.dto.*;
import cl.acis.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    UsuarioService service;
    @MockBean
    ActorComponent actor;

    @Test
    void registroValidoResponde200() throws Exception {
        when(service.registrar(any())).thenReturn(
                new UsuarioResponse(1L, "a@acis.cl", "Ana", null, true, false, false, null, null, null, null));
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content(
                "{\"correo\":\"a@acis.cl\",\"nombre\":\"Ana\",\"password\":\"Acis2026\",\"aceptoTerminos\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void loginValidoResponde200() throws Exception {
        when(service.login(any())).thenReturn(new LoginResponse(null, false, false, null));
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"a@acis.cl\",\"password\":\"Acis2026\"}")).andExpect(status().isOk());
    }

    @Test
    void listarSinHeaderResponde401() throws Exception {
        when(actor.admin(any()))
                .thenThrow(new cl.acis.usuarios.exception.NoAutorizadoException("Falta el header X-Usuario-Id"));
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void editarPerfilAjenoResponde403() throws Exception {
        when(actor.propioOAdmin(any(), eq(2L)))
                .thenThrow(new cl.acis.usuarios.exception.ProhibidoException("Solo puedes operar sobre tu propia cuenta"));

        mvc.perform(put("/api/usuarios/2")
                .header("X-Usuario-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"otro@acis.cl\",\"nombre\":\"Otro\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerUsuarioAjenoResponde403() throws Exception {
        when(actor.propioOAdmin(any(), eq(2L)))
                .thenThrow(new cl.acis.usuarios.exception.ProhibidoException("Solo puedes operar sobre tu propia cuenta"));

        mvc.perform(get("/api/usuarios/2").header("X-Usuario-Id", "1"))
                .andExpect(status().isForbidden());
    }
}
