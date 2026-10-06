package com.ronyarg.webmacros.auth.dto;

import com.ronyarg.webmacros.user.Role;

public record RegisterResponse(
    Long id,
    String name,
    String email,
    Role role
) {
}
