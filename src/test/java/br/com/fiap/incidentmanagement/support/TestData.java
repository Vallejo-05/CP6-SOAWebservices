package br.com.fiap.incidentmanagement.support;

import br.com.fiap.incidentmanagement.domain.category.Category;
import br.com.fiap.incidentmanagement.domain.category.IncidentType;
import br.com.fiap.incidentmanagement.domain.incident.Incident;
import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.schoolclass.Semester;
import br.com.fiap.incidentmanagement.domain.schoolclass.Shift;
import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.domain.user.Role;
import br.com.fiap.incidentmanagement.domain.user.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Massa de dados para os testes de unidade (os IDs são definidos por reflexão, simulando entidades salvas)
public final class TestData {
    private TestData() {
    }

    public static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    public static User user(Long id, String login, String role) {
        return withId(new User("Usuário " + id, login, "senha-criptografada", new Role(1L, role)), id);
    }

    public static Student student(Long id) {
        return withId(new Student("Aluno " + id, LocalDate.of(2012, 5, 10)), id);
    }

    public static SchoolClass schoolClass(Long id, Student... students) {
        SchoolClass schoolClass = withId(new SchoolClass("Turma " + id, Shift.MANHA, 2026, Semester.SEGUNDO), id);
        for (Student student : students) {
            schoolClass.addStudent(student);
        }
        return schoolClass;
    }

    public static IncidentType type(Long id, String name, String category) {
        return withId(new IncidentType(name, withId(new Category(category), id)), id);
    }

    public static Incident incident(Long id, User author) {
        Student student = student(1L);
        return withId(new Incident(
                schoolClass(1L, student),
                student,
                author,
                type(1L, "INDISCIPLINA", "DISCIPLINAR"),
                LocalDateTime.now().minusHours(1),
                "Descrição de teste"), id);
    }
}
