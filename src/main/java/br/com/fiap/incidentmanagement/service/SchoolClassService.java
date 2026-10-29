package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.schoolclass.Semester;
import br.com.fiap.incidentmanagement.domain.schoolclass.Shift;
import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.dto.schoolclass.SchoolClassCreateRequest;
import br.com.fiap.incidentmanagement.dto.schoolclass.SchoolClassResponse;
import br.com.fiap.incidentmanagement.dto.schoolclass.SchoolClassUpdateRequest;
import br.com.fiap.incidentmanagement.dto.student.StudentResponse;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.infra.exception.NotFoundException;
import br.com.fiap.incidentmanagement.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolClassService {
    private final SchoolClassRepository repository;
    private final StudentService studentService;

    @Transactional
    public SchoolClassResponse create(SchoolClassCreateRequest request) {
        SchoolClass schoolClass = new SchoolClass(request.name(), request.shift(), request.year(), request.semester());
        return new SchoolClassResponse(repository.save(schoolClass));
    }

    @Transactional(readOnly = true)
    public Page<SchoolClassResponse> list(boolean canceled, Pageable pageable) {
        return repository.findAllByCanceled(canceled, pageable).map(SchoolClassResponse::new);
    }

    @Transactional(readOnly = true)
    public SchoolClassResponse get(Long id) {
        return new SchoolClassResponse(find(id));
    }

    @Transactional
    public SchoolClassResponse update(SchoolClassUpdateRequest request) {
        SchoolClass schoolClass = findActive(request.id());
        schoolClass.update(request.name(), request.shift(), request.year(), request.semester());
        return new SchoolClassResponse(schoolClass);
    }

    @Transactional
    public void cancel(Long id) {
        SchoolClass schoolClass = find(id);
        if (schoolClass.isCanceled()) {
            throw new BusinessException("A turma já está cancelada.");
        }
        schoolClass.cancel();
    }

    // Vincula (adiciona) alunos à turma
    @Transactional
    public List<StudentResponse> linkStudents(Long id, List<Long> studentIds) {
        SchoolClass schoolClass = findActive(id);
        for (Long studentId : studentIds) {
            Student student = studentService.find(studentId);
            if (!student.isActive()) {
                throw new BusinessException("O aluno " + studentId + " está inativo e não pode ser vinculado.");
            }
            schoolClass.addStudent(student);
        }
        return toResponse(schoolClass);
    }

    @Transactional
    public void unlinkStudent(Long id, Long studentId) {
        SchoolClass schoolClass = find(id);
        if (!schoolClass.hasStudent(studentId)) {
            throw new NotFoundException("O aluno não está vinculado a esta turma.");
        }
        schoolClass.removeStudent(studentService.find(studentId));
    }

    // Alunos ativos da turma: usado para selecionar o aluno ao registrar uma ocorrência
    @Transactional(readOnly = true)
    public List<StudentResponse> listStudents(Long id) {
        return toResponse(find(id));
    }

    public List<String> shifts() {
        return Arrays.stream(Shift.values()).map(Enum::name).toList();
    }

    public List<String> semesters() {
        return Arrays.stream(Semester.values()).map(Enum::name).toList();
    }

    public SchoolClass find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Turma não encontrada."));
    }

    private SchoolClass findActive(Long id) {
        SchoolClass schoolClass = find(id);
        if (schoolClass.isCanceled()) {
            throw new BusinessException("A turma está cancelada.");
        }
        return schoolClass;
    }

    private List<StudentResponse> toResponse(SchoolClass schoolClass) {
        return schoolClass.getStudents().stream()
                .filter(Student::isActive)
                .sorted(Comparator.comparing(Student::getName))
                .map(StudentResponse::new)
                .toList();
    }
}
