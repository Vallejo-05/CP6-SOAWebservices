package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.domain.student.StudentPhoto;
import br.com.fiap.incidentmanagement.dto.student.StudentCreateRequest;
import br.com.fiap.incidentmanagement.dto.student.StudentResponse;
import br.com.fiap.incidentmanagement.dto.student.StudentUpdateRequest;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.infra.exception.NotFoundException;
import br.com.fiap.incidentmanagement.repository.StudentPhotoRepository;
import br.com.fiap.incidentmanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository repository;
    private final StudentPhotoRepository photoRepository;

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        return new StudentResponse(repository.save(new Student(request.name(), request.birthDate())));
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> list(Pageable pageable) {
        return repository.findAllByActiveTrue(pageable).map(StudentResponse::new);
    }

    @Transactional(readOnly = true)
    public StudentResponse get(Long id) {
        return new StudentResponse(find(id));
    }

    @Transactional
    public StudentResponse update(StudentUpdateRequest request) {
        Student student = find(request.id());
        student.update(request.name(), request.birthDate());
        return new StudentResponse(student);
    }

    @Transactional
    public void inactivate(Long id) {
        find(id).inactivate();
    }

    @Transactional
    public StudentResponse uploadPhoto(Long id, MultipartFile image) {
        Student student = find(id);
        if (image == null || image.isEmpty()) {
            throw new BusinessException("Envie uma imagem no campo 'image'.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("O arquivo enviado não é uma imagem.");
        }
        try {
            photoRepository.save(new StudentPhoto(student.getId(), image.getBytes()));
        } catch (IOException e) {
            throw new BusinessException("Não foi possível ler a imagem enviada.");
        }
        student.definePhoto(contentType);
        return new StudentResponse(student);
    }

    @Transactional(readOnly = true)
    public Photo getPhoto(Long id) {
        Student student = find(id);
        StudentPhoto photo = photoRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("O aluno não possui foto."));
        return new Photo(photo.getContent(), student.getPhotoContentType());
    }

    public Student find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Aluno não encontrado."));
    }

    public record Photo(byte[] content, String contentType) {
    }
}
