package br.com.fiap.incidentmanagement.dto.student;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StudentUpdateRequest(
        @NotNull
        Long id,

        @Size(max = 100)
        String name,

        @Past
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate birthDate) {
}
