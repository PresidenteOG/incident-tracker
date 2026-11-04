package com.dam.incidencias.service;

import com.dam.incidencias.dao.EstadoDAO;
import com.dam.incidencias.dao.IncidenciaDAO;
import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.domain.Incidencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the incident service. The two DAOs are mocked, so these run with no
 * Spring context and no database — they pin down the branching in {@code saveIncidencia}
 * (new vs. update, default status, closing math) which is the only real logic in the app.
 */
@ExtendWith(MockitoExtension.class)
class IncidenciaServiceImplTest {

    @Mock
    private IncidenciaDAO incidenciaDAO;

    @Mock
    private EstadoDAO estadoDAO;

    @InjectMocks
    private IncidenciaServiceImpl service;

    private static Estado estado(long id, String nombre) {
        Estado e = new Estado();
        e.setId(id);
        e.setName(nombre);
        return e;
    }

    private static final Estado ABIERTO = estado(Estado.OPEN_ID, "ABIERTO");
    private static final Estado EN_CURSO = estado(2L, "EN CURSO");
    private static final Estado CERRADO = estado(Estado.CLOSED_ID, "CERRADO");

    // --- reads -------------------------------------------------------------

    @Test
    void getAllIssuesDelegatesToDao() {
        List<Incidencia> stored = List.of(new Incidencia(), new Incidencia());
        when(incidenciaDAO.findAll()).thenReturn(stored);

        assertThat(service.getAllIssues()).isEqualTo(stored);
    }

    @Test
    void findIdReturnsTheRowWhenPresent() {
        Incidencia row = new Incidencia();
        row.setId(7L);
        when(incidenciaDAO.findById(7L)).thenReturn(Optional.of(row));

        assertThat(service.findID(7L)).isSameAs(row);
    }

    @Test
    void findIdReturnsNullWhenAbsent() {
        when(incidenciaDAO.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.findID(99L)).isNull();
    }

    @Test
    void deleteIncidenciaDelegatesToDao() {
        service.deleteIncidencia(4L);

        verify(incidenciaDAO).deleteById(4L);
    }

    // --- create ----------------------------------------------------------

    @Test
    void savingANewIncidentStampsTheOpeningDate() {
        Incidencia fresh = new Incidencia();
        fresh.setStatus(EN_CURSO);
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(fresh);

        assertThat(saved.getOpenedAt()).isCloseTo(LocalDateTime.now(),
                within(5L, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    void aNewIncidentWithNoStatusDefaultsToAbierto() {
        Incidencia fresh = new Incidencia();
        when(estadoDAO.findById(Estado.OPEN_ID)).thenReturn(Optional.of(ABIERTO));
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(fresh);

        assertThat(saved.getStatus()).isSameAs(ABIERTO);
        verify(estadoDAO).findById(Estado.OPEN_ID);
    }

    @Test
    void aNewIncidentKeepsAStatusThatCameFromTheForm() {
        Incidencia fresh = new Incidencia();
        fresh.setStatus(EN_CURSO);
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(fresh);

        assertThat(saved.getStatus()).isSameAs(EN_CURSO);
        verify(estadoDAO, never()).findById(anyLong());
    }

    @Test
    void anOpenIncidentHasNoClosingDateOrResolutionTime() {
        Incidencia fresh = new Incidencia();
        fresh.setStatus(EN_CURSO);
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(fresh);

        assertThat(saved.getClosedAt()).isNull();
        assertThat(saved.getResolutionMinutes()).isNull();
    }

    // --- update --------------------------------------------------------

    @Test
    void updatingAnIncidentPreservesTheOriginalOpeningDate() {
        LocalDateTime opened = LocalDateTime.now().minusDays(3);
        Incidencia original = new Incidencia();
        original.setId(10L);
        original.setOpenedAt(opened);

        Incidencia edited = new Incidencia();
        edited.setId(10L);
        edited.setStatus(EN_CURSO);
        edited.setOpenedAt(LocalDateTime.now()); // stale value from the form

        when(incidenciaDAO.findById(10L)).thenReturn(Optional.of(original));
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(edited);

        assertThat(saved.getOpenedAt()).isEqualTo(opened);
    }

    @Test
    void updatingAnIncidentThatNoLongerExistsDoesNotThrow() {
        Incidencia edited = new Incidencia();
        edited.setId(404L);
        edited.setStatus(EN_CURSO);
        when(incidenciaDAO.findById(404L)).thenReturn(Optional.empty());
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(edited);

        assertThat(saved).isNotNull();
    }

    // --- closing math -----------------------------------------------------

    @Test
    void closingAnIncidentStampsTheClosingDate() {
        Incidencia edited = new Incidencia();
        edited.setId(10L);
        edited.setStatus(CERRADO);
        Incidencia original = new Incidencia();
        original.setId(10L);
        original.setOpenedAt(LocalDateTime.now().minusMinutes(90));
        when(incidenciaDAO.findById(10L)).thenReturn(Optional.of(original));
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(edited);

        assertThat(saved.getClosedAt()).isCloseTo(LocalDateTime.now(),
                within(5L, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    void closingAnIncidentRecordsTheMinutesSinceItOpened() {
        Incidencia edited = new Incidencia();
        edited.setId(10L);
        edited.setStatus(CERRADO);
        Incidencia original = new Incidencia();
        original.setId(10L);
        original.setOpenedAt(LocalDateTime.now().minusMinutes(150));
        when(incidenciaDAO.findById(10L)).thenReturn(Optional.of(original));
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(edited);

        assertThat(saved.getResolutionMinutes()).isEqualTo(150L);
        assertThat(saved.getResolutionTime()).isEqualTo("2h 30m");
    }

    @Test
    void reopeningAClosedIncidentClearsTheClosingDateAndResolutionTime() {
        Incidencia edited = new Incidencia();
        edited.setId(10L);
        edited.setStatus(EN_CURSO);
        edited.setClosedAt(LocalDateTime.now().minusDays(1));
        edited.setResolutionMinutes(720L);
        Incidencia original = new Incidencia();
        original.setId(10L);
        original.setOpenedAt(LocalDateTime.now().minusDays(2));
        when(incidenciaDAO.findById(10L)).thenReturn(Optional.of(original));
        when(incidenciaDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incidencia saved = service.saveIncidencia(edited);

        assertThat(saved.getClosedAt()).isNull();
        assertThat(saved.getResolutionMinutes()).isNull();
    }

    // --- login ---------------------------------------------------------

    @Test
    void loginAcceptsTheDemoAccount() {
        assertThat(service.checkLogin("admin", "admin")).isTrue();
    }

    @Test
    void loginRejectsAWrongPassword() {
        assertThat(service.checkLogin("admin", "nope")).isFalse();
    }

    @Test
    void loginRejectsAnUnknownUser() {
        assertThat(service.checkLogin("root", "admin")).isFalse();
    }

    @Test
    void loginRejectsNulls() {
        assertThat(service.checkLogin(null, null)).isFalse();
    }
}
