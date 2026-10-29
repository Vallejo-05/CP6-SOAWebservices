package br.com.fiap.incidentmanagement.student;

import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.domain.student.StudentPhoto;
import br.com.fiap.incidentmanagement.dto.student.StudentUpdateRequest;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.infra.exception.NotFoundException;
import br.com.fiap.incidentmanagement.repository.StudentPhotoRepository;
import br.com.fiap.incidentmanagement.repository.StudentRepository;
import br.com.fiap.incidentmanagement.service.StudentService;
import br.com.fiap.incidentmanagement.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    StudentRepository repository;

    @Mock
    StudentPhotoRepository photoRepository;

    @InjectMocks
    StudentService service;

    @Test
    @DisplayName("Exclusão apenas inativa o aluno (exclusão lógica)")
    void inactivate() {
        Student student = TestData.student(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(student));

        service.inactivate(1L);

        assertThat(student.isActive()).isFalse();
    }

    @Test
    @DisplayName("Atualização mantém os campos não informados")
    void updateKeepsMissingFields() {
        Student student = TestData.student(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(student));

        var response = service.update(new StudentUpdateRequest(1L, "Novo Nome", null));

        assertThat(response.name()).isEqualTo("Novo Nome");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(2012, 5, 10));
    }

    @Test
    @DisplayName("Salva a foto e passa a informar a URL da imagem")
    void uploadPhoto() {
        Student student = TestData.student(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(student));

        var response = service.uploadPhoto(1L, new MockMultipartFile("image", "f.png", "image/png", new byte[]{1}));

        verify(photoRepository).save(any(StudentPhoto.class));
        assertThat(student.hasPhoto()).isTrue();
        assertThat(response.imageUrl()).endsWith("/students/1/image");
    }

    @Test
    @DisplayName("Rejeita arquivo que não é imagem")
    void uploadNonImage() {
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.student(1L)));

        assertThatThrownBy(() -> service.uploadPhoto(
                1L, new MockMultipartFile("image", "f.pdf", "application/pdf", new byte[]{1})))
                .isInstanceOf(BusinessException.class);
        verify(photoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Aluno sem foto retorna 404 na consulta da imagem")
    void photoNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.student(1L)));
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPhoto(1L)).isInstanceOf(NotFoundException.class);
    }
}
