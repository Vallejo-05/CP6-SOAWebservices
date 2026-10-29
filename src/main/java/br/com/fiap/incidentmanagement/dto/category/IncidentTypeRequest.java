package br.com.fiap.incidentmanagement.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncidentTypeRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        // Nome da categoria à qual o tipo pertence
        @NotBlank
        String category) {
}
