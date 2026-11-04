package com.dam.incidencias.exception;

public class IncidenciaNotFoundException extends ResourceNotFoundException {

    public IncidenciaNotFoundException(Long id) {
        super("No existe la incidencia con id " + id + ".");
    }
}
