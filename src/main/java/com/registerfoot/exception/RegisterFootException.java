package com.registerfoot.exception;

/** Excepcion base de negocio del sistema. */
public class RegisterFootException extends RuntimeException {
    public RegisterFootException(String message) { super(message); }
    public RegisterFootException(String message, Throwable cause) { super(message, cause); }
}
