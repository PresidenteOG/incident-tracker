package com.dam.incidencias.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "incidencia")
@Data
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "labor")
    private String category;

    @Column(name = "titol")
    private String title;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "data_obertura")
    private LocalDateTime openedAt;

    @Column(name = "data_tancament")
    private LocalDateTime closedAt;

    @Column(name = "temps_resolucio")
    private Long resolutionMinutes;

    @Column(name = "prioritat")
    private String priority;

    @ManyToOne
    @JoinColumn(name = "estado_id")
    private Estado status;

    /** Resolution time for display, e.g. {@code "2h 30m"}; null while the incident is not closed. */
    public String getResolutionTime() {
        if (resolutionMinutes == null) {
            return null;
        }
        return resolutionMinutes / 60 + "h " + resolutionMinutes % 60 + "m";
    }
}


