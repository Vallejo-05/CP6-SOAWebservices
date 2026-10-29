package br.com.fiap.incidentmanagement.dto.schoolclass;

import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.schoolclass.Semester;
import br.com.fiap.incidentmanagement.domain.schoolclass.Shift;

public record SchoolClassResponse(
        Long id,
        String name,
        Shift shift,
        Integer year,
        Semester semester,
        boolean canceled) {
    public SchoolClassResponse(SchoolClass schoolClass) {
        this(
                schoolClass.getId(),
                schoolClass.getName(),
                schoolClass.getShift(),
                schoolClass.getYear(),
                schoolClass.getSemester(),
                schoolClass.isCanceled()
        );
    }
}
