package com.paper.reviewer.auth.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 100) String displayName) {
    public com.paper.reviewer.auth.service.RegisterCommand toCommand() {
        return new com.paper.reviewer.auth.service.RegisterCommand(email, password, displayName);
    }
}
