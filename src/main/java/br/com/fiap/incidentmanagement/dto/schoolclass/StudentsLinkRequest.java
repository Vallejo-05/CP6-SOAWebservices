package br.com.fiap.incidentmanagement.dto.schoolclass;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StudentsLinkRequest(
        @NotEmpty
        List<Long> studentIds) {
}
