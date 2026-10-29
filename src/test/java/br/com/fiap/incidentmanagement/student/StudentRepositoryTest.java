package br.com.fiap.incidentmanagement.student;

import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.domain.student.StudentPhoto;
import br.com.fiap.incidentmanagement.repository.StudentPhotoRepository;
import br.com.fiap.incidentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class StudentRepositoryTest {
    @Autowired
    StudentRepository repository;

    @Autowired
    StudentPhotoRepository photoRepository;

    @Test
    @DisplayName("A listagem traz apenas alunos ativos")
    void findAllByActiveTrue() {
        repository.save(new Student("Bruno", LocalDate.of(2011, 1, 1)));
        repository.save(new Student("Ana", LocalDate.of(2012, 1, 1)));
        Student inactive = new Student("Carlos", LocalDate.of(2010, 1, 1));
        inactive.inactivate();
        repository.save(inactive);

        var page = repository.findAllByActiveTrue(PageRequest.of(0, 10, Sort.by("name")));

        assertThat(page.getContent()).extracting(Student::getName).containsExactly("Ana", "Bruno");
    }

    @Test
    @DisplayName("Armazena e recupera a foto do aluno")
    void savePhoto() {
        Student student = repository.save(new Student("Ana", LocalDate.of(2012, 1, 1)));
        photoRepository.save(new StudentPhoto(student.getId(), new byte[]{1, 2, 3}));

        assertThat(photoRepository.findById(student.getId()))
                .hasValueSatisfying(photo -> assertThat(photo.getContent()).containsExactly(1, 2, 3));
    }
}
