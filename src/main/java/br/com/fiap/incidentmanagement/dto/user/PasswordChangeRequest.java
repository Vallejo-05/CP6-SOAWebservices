package br.com.fiap.incidentmanagement.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        // Opcional: se informado, deve ser o ID do próprio usuário autenticado
        Long id,

        @NotBlank
        String oldPassword,

        @NotBlank
        @Size(min = 6, message = "A nova senha deve conter no mínimo 6 caracteres")
        String newPassword) {
}
