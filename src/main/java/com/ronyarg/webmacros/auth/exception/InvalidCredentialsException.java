package com.ronyarg.webmacros.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("El email o la contraseña son incorrectos");
    }

}
