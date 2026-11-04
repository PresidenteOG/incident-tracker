package com.dam.incidencias.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncidenciaTest {

    private static Incidencia closedAfter(Long minutes) {
        Incidencia incidencia = new Incidencia();
        incidencia.setResolutionMinutes(minutes);
        return incidencia;
    }

    @Test
    void resolutionTimeIsFormattedAsHoursAndMinutes() {
        assertThat(closedAfter(150L).getResolutionTime()).isEqualTo("2h 30m");
    }

    @Test
    void resolutionTimeOfWholeHoursKeepsTheZeroMinutes() {
        assertThat(closedAfter(60L).getResolutionTime()).isEqualTo("1h 0m");
    }

    @Test
    void resolutionTimeUnderAnHourStartsAtZeroHours() {
        assertThat(closedAfter(45L).getResolutionTime()).isEqualTo("0h 45m");
    }

    @Test
    void resolutionTimeIsNullUntilTheIncidentIsClosed() {
        assertThat(closedAfter(null).getResolutionTime()).isNull();
    }
}
