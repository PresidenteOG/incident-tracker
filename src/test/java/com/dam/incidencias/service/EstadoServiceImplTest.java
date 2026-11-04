package com.dam.incidencias.service;

import com.dam.incidencias.dao.EstadoDAO;
import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.exception.EstadoNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
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
    void getEstadoByIdThrowsWhenAbsent() {
        when(estadoDAO.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEstadoById(9L))
                .isInstanceOf(EstadoNotFoundException.class)
                .hasMessageContaining("9");
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
        when(estadoDAO.existsById(5L)).thenReturn(true);

        service.deleteEstado(5L);

        verify(estadoDAO).deleteById(5L);
    }

    @Test
    void deleteEstadoThrowsWhenAbsent() {
        when(estadoDAO.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteEstado(5L))
                .isInstanceOf(EstadoNotFoundException.class);
        verify(estadoDAO, never()).deleteById(anyLong());
    }
}
