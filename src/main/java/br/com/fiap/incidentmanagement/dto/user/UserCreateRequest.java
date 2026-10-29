package br.com.fiap.incidentmanagement.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Email
        @Size(max = 100)
        String login,

        @NotBlank
        @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres")
        String password,

        @NotBlank
        String role) {
}
