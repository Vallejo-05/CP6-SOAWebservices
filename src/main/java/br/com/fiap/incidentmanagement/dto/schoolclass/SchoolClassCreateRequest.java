package br.com.fiap.incidentmanagement.dto.schoolclass;

import br.com.fiap.incidentmanagement.domain.schoolclass.Semester;
import br.com.fiap.incidentmanagement.domain.schoolclass.Shift;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SchoolClassCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        Shift shift,

        @NotNull
        @Min(2000)
        @Max(2100)
        Integer year,

        @NotNull
        Semester semester) {
}
