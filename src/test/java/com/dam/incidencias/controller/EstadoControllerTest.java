package com.dam.incidencias.controller;

import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.exception.EstadoNotFoundException;
import com.dam.incidencias.service.EstadoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(EstadoController.class)
class EstadoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EstadoService estadoService;

    private static Estado estado(long id, String name) {
        Estado e = new Estado();
        e.setId(id);
        e.setName(name);
        return e;
    }

    @Test
    void theListShowsEveryStatusName() throws Exception {
        when(estadoService.getAllEstados()).thenReturn(List.of(estado(1L, "ABIERTO"), estado(3L, "CERRADO")));

        mvc.perform(get("/Estado-list").sessionAttr("usuario", "admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("Estado-list"))
                .andExpect(content().string(containsString("ABIERTO")))
                .andExpect(content().string(containsString("CERRADO")));
    }

    @Test
    void theEditFormIsFilledFromTheStoredStatus() throws Exception {
        when(estadoService.getEstadoById(2L)).thenReturn(estado(2L, "EN CURSO"));

        mvc.perform(get("/Estado-edit/2").sessionAttr("usuario", "admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("Estado-edit"))
                .andExpect(content().string(containsString("EN CURSO")));
    }

    @Test
    void editingAnUnknownStatusShowsThe404Page() throws Exception {
        when(estadoService.getEstadoById(9L)).thenThrow(new EstadoNotFoundException(9L));

        mvc.perform(get("/Estado-edit/9").sessionAttr("usuario", "admin"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("Error-not-found"))
                .andExpect(content().string(containsString("No existe el estado con id 9")));
    }

    @Test
    void deletingAnUnknownStatusShowsThe404Page() throws Exception {
        doThrow(new EstadoNotFoundException(9L)).when(estadoService).deleteEstado(9L);

        mvc.perform(get("/Estado-delete/9").sessionAttr("usuario", "admin"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingAStatusStillInUseFlashesAnError() throws Exception {
        doThrow(new DataIntegrityViolationException("in use")).when(estadoService).deleteEstado(1L);

        mvc.perform(get("/Estado-delete/1").sessionAttr("usuario", "admin"))
                .andExpect(redirectedUrl("/Estado-list"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void deletingAnUnusedStatusFlashesAConfirmation() throws Exception {
        mvc.perform(get("/Estado-delete/2").sessionAttr("usuario", "admin"))
                .andExpect(redirectedUrl("/Estado-list"))
                .andExpect(flash().attributeExists("mensaje"));
    }
}
