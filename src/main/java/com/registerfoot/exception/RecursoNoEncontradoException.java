package com.registerfoot.exception;

/** El recurso solicitado no existe. */
public class RecursoNoEncontradoException extends RegisterFootException {
    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado: " + id);
    }
    public RecursoNoEncontradoException(String message) { super(message); }
}
