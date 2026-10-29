package br.com.fiap.incidentmanagement.schoolclass;

import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.schoolclass.Semester;
import br.com.fiap.incidentmanagement.domain.schoolclass.Shift;
import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.repository.SchoolClassRepository;
import br.com.fiap.incidentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SchoolClassRepositoryTest {
    @Autowired
    SchoolClassRepository repository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("Separa turmas ativas e canceladas")
    void findAllByCanceled() {
        repository.save(new SchoolClass("1A", Shift.MANHA, 2026, Semester.SEGUNDO));
        SchoolClass canceled = new SchoolClass("2B", Shift.NOITE, 2026, Semester.SEGUNDO);
        canceled.cancel();
        repository.save(canceled);

        assertThat(repository.findAllByCanceled(false, PageRequest.of(0, 10)).getContent())
                .extracting(SchoolClass::getName).containsExactly("1A");
        assertThat(repository.findAllByCanceled(true, PageRequest.of(0, 10)).getContent())
                .extracting(SchoolClass::getName).containsExactly("2B");
    }

    @Test
    @DisplayName("Um aluno pode estar matriculado em várias turmas (N:M)")
    void studentInManyClasses() {
        Student ana = studentRepository.save(new Student("Ana", LocalDate.of(2012, 1, 1)));
        SchoolClass first = new SchoolClass("1A", Shift.MANHA, 2026, Semester.PRIMEIRO);
        SchoolClass second = new SchoolClass("1A", Shift.MANHA, 2026, Semester.SEGUNDO);
        first.addStudent(ana);
        second.addStudent(ana);
        repository.save(first);
        repository.save(second);
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findById(first.getId()).orElseThrow().hasStudent(ana.getId())).isTrue();
        assertThat(repository.findById(second.getId()).orElseThrow().hasStudent(ana.getId())).isTrue();
    }
}
