package br.com.fiap.incidentmanagement.dto.incident;

import br.com.fiap.incidentmanagement.domain.incident.IncidentStatus;

public record StatusResponse(String name, String description) {
    public StatusResponse(IncidentStatus status) {
        this(status.name(), status.getDescription());
    }
}
