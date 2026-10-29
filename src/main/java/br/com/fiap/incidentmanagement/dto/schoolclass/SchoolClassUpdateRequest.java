package br.com.fiap.incidentmanagement.dto.schoolclass;

import br.com.fiap.incidentmanagement.domain.schoolclass.Semester;
import br.com.fiap.incidentmanagement.domain.schoolclass.Shift;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SchoolClassUpdateRequest(
        @NotNull
        Long id,

        @Size(max = 100)
        String name,
        Shift shift,

        @Min(2000)
        @Max(2100)
        Integer year,
        Semester semester) {
}
