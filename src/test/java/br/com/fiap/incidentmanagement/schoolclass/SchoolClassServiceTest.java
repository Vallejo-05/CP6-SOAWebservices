package br.com.fiap.incidentmanagement.schoolclass;

import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.repository.SchoolClassRepository;
import br.com.fiap.incidentmanagement.service.SchoolClassService;
import br.com.fiap.incidentmanagement.service.StudentService;
import br.com.fiap.incidentmanagement.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {
    @Mock
    SchoolClassRepository repository;

    @Mock
    StudentService studentService;

    @InjectMocks
    SchoolClassService service;

    @Test
    @DisplayName("Vincula alunos ativos à turma")
    void linkStudents() {
        SchoolClass schoolClass = TestData.schoolClass(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(studentService.find(1L)).thenReturn(TestData.student(1L));
        when(studentService.find(2L)).thenReturn(TestData.student(2L));

        var students = service.linkStudents(1L, List.of(1L, 2L));

        assertThat(students).hasSize(2);
        assertThat(schoolClass.hasStudent(2L)).isTrue();
    }

    @Test
    @DisplayName("Não vincula aluno inativo")
    void linkInactiveStudent() {
        Student inactive = TestData.student(1L);
        inactive.inactivate();
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.schoolClass(1L)));
        when(studentService.find(1L)).thenReturn(inactive);

        assertThatThrownBy(() -> service.linkStudents(1L, List.of(1L))).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Não vincula alunos a uma turma cancelada")
    void linkToCanceledClass() {
        SchoolClass schoolClass = TestData.schoolClass(1L);
        schoolClass.cancel();
        when(repository.findById(1L)).thenReturn(Optional.of(schoolClass));

        assertThatThrownBy(() -> service.linkStudents(1L, List.of(1L))).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Cancelamento é lógico e não pode ser repetido")
    void cancel() {
        SchoolClass schoolClass = TestData.schoolClass(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(schoolClass));

        service.cancel(1L);

        assertThat(schoolClass.isCanceled()).isTrue();
        assertThatThrownBy(() -> service.cancel(1L)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("A lista de alunos da turma traz apenas os ativos, em ordem alfabética")
    void listStudents() {
        Student bruno = TestData.withId(new Student("Bruno", java.time.LocalDate.of(2011, 1, 1)), 2L);
        Student ana = TestData.withId(new Student("Ana", java.time.LocalDate.of(2012, 1, 1)), 1L);
        Student inactive = TestData.withId(new Student("Carlos", java.time.LocalDate.of(2010, 1, 1)), 3L);
        inactive.inactivate();
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.schoolClass(1L, bruno, ana, inactive)));

        assertThat(service.listStudents(1L)).extracting(s -> s.name()).containsExactly("Ana", "Bruno");
    }
}
