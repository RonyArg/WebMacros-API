package com.ronyarg.webmacros.exception;

public record ErrorResponse(
    int status,
    String error,
    String message
) {

}
