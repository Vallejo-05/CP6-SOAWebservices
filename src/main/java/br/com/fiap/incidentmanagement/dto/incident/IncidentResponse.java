package br.com.fiap.incidentmanagement.dto.incident;

import br.com.fiap.incidentmanagement.domain.incident.Incident;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record IncidentResponse(
        Long id,
        Long idSchoolClass,
        String schoolClassName,
        Long idStudent,
        String studentName,
        Long idUser,
        String userName,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        LocalDateTime registerDate,
        String category,
        String type,
        String description,
        String status,
        String statusDescription,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        LocalDateTime updateDate,
        boolean deleted) {
    public IncidentResponse(Incident incident) {
        this(
                incident.getId(),
                incident.getSchoolClass().getId(),
                incident.getSchoolClass().getName(),
                incident.getStudent().getId(),
                incident.getStudent().getName(),
                incident.getUser().getId(),
                incident.getUser().getName(),
                incident.getRegisterDate(),
                incident.getCategory().getName(),
                incident.getType().getName(),
                incident.getDescription(),
                incident.getStatus().name(),
                incident.getStatus().getDescription(),
                incident.getUpdateDate(),
                incident.isDeleted()
        );
    }
}
