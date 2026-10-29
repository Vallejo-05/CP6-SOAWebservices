package br.com.fiap.incidentmanagement.dto.incident;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Campos não informados mantêm o valor atual (demais campos do JSON são ignorados)
@JsonIgnoreProperties(ignoreUnknown = true)
public record IncidentUpdateRequest(
        @NotNull
        Long id,
        Long idSchoolClass,
        Long idStudent,
        String category,
        String type,

        @Size(max = 400)
        String description) {
}
