package com.dam.incidencias.exception;

/** Base for "no row with that id" errors; the web layer turns it into a 404 page. */
public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
