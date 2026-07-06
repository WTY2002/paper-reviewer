package com.paper.reviewer.auth.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 72) String password) {
    public com.paper.reviewer.auth.service.LoginCommand toCommand() {
        return new com.paper.reviewer.auth.service.LoginCommand(email, password);
    }
}
