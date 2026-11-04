package com.dam.incidencias.controller;

import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.domain.Incidencia;
import com.dam.incidencias.exception.IncidenciaNotFoundException;
import com.dam.incidencias.service.EstadoService;
import com.dam.incidencias.service.IncidenciaService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Web slice: real controller, interceptor and Thymeleaf views; the services are mocked. */
@WebMvcTest(IncidenciaController.class)
class IncidenciaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IncidenciaService incidenciaService;

    @MockBean
    private EstadoService estadoService;

    private static Estado estado(long id, String name) {
        Estado e = new Estado();
        e.setId(id);
        e.setName(name);
        return e;
    }

    private static Incidencia closedIncident() {
        Incidencia i = new Incidencia();
        i.setId(7L);
        i.setTitle("Impresora sin red");
        i.setDescription("No aparece en la red");
        i.setPriority("ALTA");
        i.setStatus(estado(Estado.CLOSED_ID, "CERRADO"));
        i.setResolutionMinutes(150L);
        return i;
    }

    @Test
    void anAnonymousRequestIsSentToTheLogin() throws Exception {
        mvc.perform(get("/Incidencia"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/LoginIncidencia"));
    }

    @Test
    void theListShowsTheStatusAndTheFormattedResolutionTime() throws Exception {
        when(incidenciaService.getAllIssues()).thenReturn(List.of(closedIncident()));

        mvc.perform(get("/Incidencia").sessionAttr("usuario", "admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("Incidencia-list"))
                .andExpect(content().string(containsString("Impresora sin red")))
                .andExpect(content().string(containsString("CERRADO")))
                .andExpect(content().string(containsString("2h 30m")));
    }

    @Test
    void theEditFormIsFilledFromTheStoredIncident() throws Exception {
        when(incidenciaService.findID(7L)).thenReturn(closedIncident());
        when(estadoService.getAllEstados()).thenReturn(List.of(estado(1L, "ABIERTO"), estado(3L, "CERRADO")));

        mvc.perform(get("/Incidencia/updateData/7").sessionAttr("usuario", "admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("Incidencia-update"))
                .andExpect(content().string(containsString("Impresora sin red")));
    }

    @Test
    void editingAnUnknownIncidentShowsThe404Page() throws Exception {
        when(incidenciaService.findID(99L)).thenThrow(new IncidenciaNotFoundException(99L));

        mvc.perform(get("/Incidencia/updateData/99").sessionAttr("usuario", "admin"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("Error-not-found"))
                .andExpect(content().string(containsString("No existe la incidencia con id 99")));
    }

    @Test
    void deletingAnUnknownIncidentShowsThe404Page() throws Exception {
        doThrow(new IncidenciaNotFoundException(99L)).when(incidenciaService).deleteIncidencia(99L);

        mvc.perform(get("/Incidencia/deleteData/99").sessionAttr("usuario", "admin"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("Error-not-found"));
    }

    @Test
    void theFormFieldsBindToTheRenamedProperties() throws Exception {
        mvc.perform(post("/Incidencia").sessionAttr("usuario", "admin")
                        .param("title", "Disco lleno")
                        .param("description", "Logs sin rotar")
                        .param("priority", "ALTA")
                        .param("status.id", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/Incidencia"));

        ArgumentCaptor<Incidencia> saved = ArgumentCaptor.forClass(Incidencia.class);
        verify(incidenciaService).saveIncidencia(saved.capture());
        assertThat(saved.getValue().getTitle()).isEqualTo("Disco lleno");
        assertThat(saved.getValue().getDescription()).isEqualTo("Logs sin rotar");
        assertThat(saved.getValue().getPriority()).isEqualTo("ALTA");
        assertThat(saved.getValue().getStatus().getId()).isEqualTo(3L);
    }

    @Test
    void aGoodLoginRedirectsToTheList() throws Exception {
        when(incidenciaService.checkLogin("admin", "admin")).thenReturn(true);

        mvc.perform(post("/Incidencia/login").param("Name", "admin").param("Password", "admin"))
                .andExpect(redirectedUrl("/Incidencia"));
    }

    @Test
    void aBadLoginGoesBackToTheLoginPage() throws Exception {
        when(incidenciaService.checkLogin(any(), any())).thenReturn(false);

        mvc.perform(post("/Incidencia/login").param("Name", "admin").param("Password", "x"))
                .andExpect(redirectedUrl("/LoginIncidencia?error"));
    }
}
