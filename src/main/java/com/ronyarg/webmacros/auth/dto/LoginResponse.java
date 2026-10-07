package com.ronyarg.webmacros.auth.dto;

public record LoginResponse(
    String token,
    String name,
    String email
) {

}
