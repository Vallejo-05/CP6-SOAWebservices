package br.com.fiap.incidentmanagement.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Atualiza nome e login. Perfil e senha possuem endpoints próprios (demais campos são ignorados)
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserUpdateRequest(
        @NotNull
        Long id,

        @Size(max = 100)
        String name,

        @Email
        @Size(max = 100)
        String login) {
}
