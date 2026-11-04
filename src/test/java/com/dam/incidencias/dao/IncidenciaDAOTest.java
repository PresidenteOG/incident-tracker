package com.dam.incidencias.dao;

import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.domain.Incidencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistence slice test. {@code @DataJpaTest} spins up an in-memory H2 and the JPA
 * layer only — no web, no service beans — and runs {@code data.sql}, so this also
 * checks that the seed loads and the {@code Incidencia → Estado} mapping resolves.
 */
@DataJpaTest
class IncidenciaDAOTest {

    @Autowired
    private IncidenciaDAO incidenciaDAO;

    @Autowired
    private EstadoDAO estadoDAO;

    @Test
    void theSeedDataIsLoaded() {
        assertThat(estadoDAO.count()).isEqualTo(3);
        assertThat(incidenciaDAO.count()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void theStatusIdsUsedByTheServiceMatchTheSeed() {
        assertThat(estadoDAO.findById(Estado.OPEN_ID)).get().extracting(Estado::getName).isEqualTo("ABIERTO");
        assertThat(estadoDAO.findById(Estado.CLOSED_ID)).get().extracting(Estado::getName).isEqualTo("CERRADO");
    }

    @Test
    void everySeededIncidentResolvesItsStatus() {
        assertThat(incidenciaDAO.findAll())
                .allSatisfy(i -> assertThat(i.getStatus()).isNotNull());
    }

    @Test
    void aSavedIncidentComesBackWithAGeneratedIdAndItsStatus() {
        Estado abierto = estadoDAO.findById(Estado.OPEN_ID).orElseThrow();
        Incidencia i = new Incidencia();
        i.setTitle("Disco lleno en el nodo de logs");
        i.setCategory("Infra");
        i.setPriority("ALTA");
        i.setOpenedAt(LocalDateTime.now());
        i.setStatus(abierto);

        Incidencia saved = incidenciaDAO.save(i);

        assertThat(saved.getId()).isNotNull();
        Incidencia reloaded = incidenciaDAO.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Disco lleno en el nodo de logs");
        assertThat(reloaded.getStatus().getName()).isEqualTo("ABIERTO");
    }

    @Test
    void deletingAnIncidentRemovesItFromTheTable() {
        long before = incidenciaDAO.count();
        Incidencia first = incidenciaDAO.findAll().get(0);

        incidenciaDAO.deleteById(first.getId());

        assertThat(incidenciaDAO.count()).isEqualTo(before - 1);
        assertThat(incidenciaDAO.findById(first.getId())).isEmpty();
    }
}
