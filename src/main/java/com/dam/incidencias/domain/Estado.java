package com.dam.incidencias.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "estado")
@Data
public class Estado {

    // Ids of the rows seeded in data.sql. The service logic depends on these two.
    public static final long OPEN_ID = 1L;
    public static final long CLOSED_ID = 3L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Status label, e.g. ABIERTO, EN CURSO, CERRADO
    @Column(name = "nombre")
    private String name;
}
