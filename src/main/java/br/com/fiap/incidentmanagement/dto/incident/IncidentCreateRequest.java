package br.com.fiap.incidentmanagement.dto.incident;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record IncidentCreateRequest(
        @NotNull
        Long idSchoolClass,

        @NotNull
        Long idStudent,

        // Data e hora do ocorrido; se não informada, assume o momento do registro
        @PastOrPresent
        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        LocalDateTime registerDate,

        @NotBlank
        String category,

        @NotBlank
        String type,

        @NotBlank
        @Size(max = 400)
        String description) {
}
