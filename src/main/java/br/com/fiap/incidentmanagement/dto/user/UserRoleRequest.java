package br.com.fiap.incidentmanagement.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRoleRequest(
        @NotNull
        Long id,

        @NotBlank
        String role) {
}
