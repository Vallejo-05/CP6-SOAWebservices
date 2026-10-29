package br.com.fiap.incidentmanagement.dto.category;

import br.com.fiap.incidentmanagement.domain.category.IncidentType;

public record IncidentTypeResponse(Long id, String name, String category) {
    public IncidentTypeResponse(IncidentType type) {
        this(type.getId(), type.getName(), type.getCategory().getName());
    }
}
