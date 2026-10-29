package br.com.fiap.incidentmanagement.dto.student;

import br.com.fiap.incidentmanagement.domain.student.Student;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String name,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate birthDate,

        // URL absoluta da foto, para ser usada diretamente em <img src="...">
        String imageUrl,
        boolean active) {
    public StudentResponse(Student student) {
        this(
                student.getId(),
                student.getName(),
                student.getBirthDate(),
                student.hasPhoto() ? imageUrlOf(student.getId()) : null,
                student.isActive()
        );
    }

    private static String imageUrlOf(Long id) {
        try {
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/students/{id}/image")
                    .buildAndExpand(id)
                    .toUriString();
        } catch (IllegalStateException e) {
            return "/students/" + id + "/image";
        }
    }
}
