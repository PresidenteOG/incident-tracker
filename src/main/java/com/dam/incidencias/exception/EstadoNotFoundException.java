package com.dam.incidencias.exception;

public class EstadoNotFoundException extends ResourceNotFoundException {

    public EstadoNotFoundException(Long id) {
        super("No existe el estado con id " + id + ".");
    }
}
