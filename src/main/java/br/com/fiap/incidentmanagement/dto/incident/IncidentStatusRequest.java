package br.com.fiap.incidentmanagement.dto.incident;

import br.com.fiap.incidentmanagement.domain.incident.IncidentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record IncidentStatusRequest(
        @NotNull
        Long id,

        @NotNull
        IncidentStatus status,

        // Se não informada, assume o momento da alteração
        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        LocalDateTime updateDate) {
}
