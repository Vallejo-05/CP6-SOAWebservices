package br.com.fiap.incidentmanagement.incident;

import br.com.fiap.incidentmanagement.domain.incident.Incident;
import br.com.fiap.incidentmanagement.domain.incident.IncidentStatus;
import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.domain.user.User;
import br.com.fiap.incidentmanagement.dto.incident.IncidentCreateRequest;
import br.com.fiap.incidentmanagement.dto.incident.IncidentStatusRequest;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.infra.exception.ForbiddenOperationException;
import br.com.fiap.incidentmanagement.repository.IncidentRepository;
import br.com.fiap.incidentmanagement.repository.UserRepository;
import br.com.fiap.incidentmanagement.service.CategoryService;
import br.com.fiap.incidentmanagement.service.IncidentService;
import br.com.fiap.incidentmanagement.service.SchoolClassService;
import br.com.fiap.incidentmanagement.service.StudentService;
import br.com.fiap.incidentmanagement.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {
    @Mock
    IncidentRepository repository;

    @Mock
    UserRepository userRepository;

    @Mock
    SchoolClassService schoolClassService;

    @Mock
    StudentService studentService;

    @Mock
    CategoryService categoryService;

    @InjectMocks
    IncidentService service;

    private final User professor = TestData.user(4L, "professor@escola.com", "PROFESSOR");

    private IncidentCreateRequest request() {
        return new IncidentCreateRequest(1L, 1L, null, "DISCIPLINAR", "INDISCIPLINA", "Conversa excessiva");
    }

    @Test
    @DisplayName("Registra a ocorrência com status inicial 'aguardando' e data atual quando não informada")
    void create() {
        Student student = TestData.student(1L);
        when(userRepository.findByLogin("professor@escola.com")).thenReturn(Optional.of(professor));
        when(schoolClassService.find(1L)).thenReturn(TestData.schoolClass(1L, student));
        when(studentService.find(1L)).thenReturn(student);
        when(categoryService.findType("INDISCIPLINA", "DISCIPLINAR"))
                .thenReturn(TestData.type(1L, "INDISCIPLINA", "DISCIPLINAR"));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create("professor@escola.com", request());

        assertThat(response.status()).isEqualTo("WAITING");
        assertThat(response.category()).isEqualTo("DISCIPLINAR");
        assertThat(response.idUser()).isEqualTo(4L);
        assertThat(response.registerDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Rejeita aluno que não pertence à turma selecionada")
    void createWithStudentOutsideClass() {
        when(userRepository.findByLogin("professor@escola.com")).thenReturn(Optional.of(professor));
        when(schoolClassService.find(1L)).thenReturn(TestData.schoolClass(1L));
        when(studentService.find(1L)).thenReturn(TestData.student(1L));

        assertThatThrownBy(() -> service.create("professor@escola.com", request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não pertence à turma");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita ocorrência em turma cancelada")
    void createInCanceledClass() {
        SchoolClass canceled = TestData.schoolClass(1L, TestData.student(1L));
        canceled.cancel();
        when(userRepository.findByLogin("professor@escola.com")).thenReturn(Optional.of(professor));
        when(schoolClassService.find(1L)).thenReturn(canceled);

        assertThatThrownBy(() -> service.create("professor@escola.com", request()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Professor não pode excluir a ocorrência de outro professor")
    void deleteOthersIncident() {
        User other = TestData.user(9L, "outro@escola.com", "PROFESSOR");
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.incident(1L, other)));
        when(userRepository.findByLogin("professor@escola.com")).thenReturn(Optional.of(professor));

        assertThatThrownBy(() -> service.delete("professor@escola.com", 1L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    @DisplayName("Professor exclui a própria ocorrência (exclusão lógica)")
    void deleteOwnIncident() {
        Incident incident = TestData.incident(1L, professor);
        when(repository.findById(1L)).thenReturn(Optional.of(incident));
        when(userRepository.findByLogin("professor@escola.com")).thenReturn(Optional.of(professor));

        service.delete("professor@escola.com", 1L);

        assertThat(incident.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Ocorrência fechada não pode ter o status alterado")
    void changeStatusOfClosedIncident() {
        Incident incident = TestData.incident(1L, professor);
        incident.changeStatus(IncidentStatus.CLOSED, LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.changeStatus(new IncidentStatusRequest(1L, IncidentStatus.SOLVED, null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Data de atualização não pode ser anterior ao registro")
    void changeStatusBeforeRegisterDate() {
        Incident incident = TestData.incident(1L, professor);
        when(repository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.changeStatus(new IncidentStatusRequest(
                1L, IncidentStatus.PROGRESSING, incident.getRegisterDate().minusDays(1))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Status inexistente na URL retorna 404")
    void listByInvalidStatus() {
        assertThatThrownBy(() -> service.listByStatus("pending", null))
                .hasMessageContaining("Status inexistente");
    }
}
