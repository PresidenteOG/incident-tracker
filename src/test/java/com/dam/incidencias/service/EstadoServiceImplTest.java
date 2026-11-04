package com.dam.incidencias.service;

import com.dam.incidencias.dao.EstadoDAO;
import com.dam.incidencias.domain.Estado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the status service — a thin pass-through over the DAO. */
@ExtendWith(MockitoExtension.class)
class EstadoServiceImplTest {

    @Mock
    private EstadoDAO estadoDAO;

    @InjectMocks
    private EstadoServiceImpl service;

    private static Estado estado(long id, String nombre) {
        Estado e = new Estado();
        e.setId(id);
        e.setName(nombre);
        return e;
    }

    @Test
    void getAllEstadosDelegatesToDao() {
        List<Estado> stored = List.of(estado(1L, "ABIERTO"), estado(3L, "CERRADO"));
        when(estadoDAO.findAll()).thenReturn(stored);

        assertThat(service.getAllEstados()).isEqualTo(stored);
    }

    @Test
    void getEstadoByIdReturnsTheRowWhenPresent() {
        Estado row = estado(2L, "EN CURSO");
        when(estadoDAO.findById(2L)).thenReturn(Optional.of(row));

        assertThat(service.getEstadoById(2L)).isSameAs(row);
    }

    @Test
    void getEstadoByIdReturnsNullWhenAbsent() {
        when(estadoDAO.findById(9L)).thenReturn(Optional.empty());

        assertThat(service.getEstadoById(9L)).isNull();
    }

    @Test
    void saveEstadoReturnsThePersistedRow() {
        Estado toSave = estado(0L, "NUEVO");
        Estado persisted = estado(4L, "NUEVO");
        when(estadoDAO.save(toSave)).thenReturn(persisted);

        assertThat(service.saveEstado(toSave)).isSameAs(persisted);
    }

    @Test
    void deleteEstadoDelegatesToDao() {
        service.deleteEstado(5L);

        verify(estadoDAO).deleteById(5L);
    }
}
