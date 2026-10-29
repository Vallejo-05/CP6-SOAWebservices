package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.domain.category.IncidentType;
import br.com.fiap.incidentmanagement.domain.incident.Incident;
import br.com.fiap.incidentmanagement.domain.incident.IncidentStatus;
import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.domain.user.User;
import br.com.fiap.incidentmanagement.dto.incident.*;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.infra.exception.ForbiddenOperationException;
import br.com.fiap.incidentmanagement.infra.exception.NotFoundException;
import br.com.fiap.incidentmanagement.repository.IncidentRepository;
import br.com.fiap.incidentmanagement.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {
    private static final Map<String, String> SORT_ALIASES = Map.of(
            "category", "category.name",
            "type", "type.name");

    private final IncidentRepository repository;
    private final UserRepository userRepository;
    private final SchoolClassService schoolClassService;
    private final StudentService studentService;
    private final CategoryService categoryService;

    @Transactional
    public IncidentResponse create(String authenticatedLogin, IncidentCreateRequest request) {
        User author = findUser(authenticatedLogin);
        SchoolClass schoolClass = activeSchoolClass(request.idSchoolClass());
        Student student = studentOfSchoolClass(schoolClass, request.idStudent());
        IncidentType type = categoryService.findType(request.type(), request.category());
        LocalDateTime registerDate = request.registerDate() != null ? request.registerDate() : LocalDateTime.now();

        Incident incident = new Incident(schoolClass, student, author, type, registerDate, request.description());
        return new IncidentResponse(repository.save(incident));
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> list(IncidentFilter filter, Pageable pageable) {
        return repository
                .findAll(specification(filter), PageableTranslator.translate(pageable, SORT_ALIASES))
                .map(IncidentResponse::new);
    }

    // statusKey: waiting, progressing, solved, unsolved, closed ou active (aguardando + em atendimento)
    @Transactional(readOnly = true)
    public Page<IncidentResponse> listByStatus(String statusKey, Pageable pageable) {
        Set<IncidentStatus> status = "active".equalsIgnoreCase(statusKey)
                ? IncidentStatus.ACTIVE
                : Set.of(parseStatus(statusKey));
        return repository
                .findAllByStatusInAndDeletedFalse(status, PageableTranslator.translate(pageable, SORT_ALIASES))
                .map(IncidentResponse::new);
    }

    @Transactional(readOnly = true)
    public IncidentResponse get(Long id) {
        return new IncidentResponse(find(id));
    }

    // Histórico completo do aluno, contemplando todas as turmas em que esteve matriculado
    @Transactional(readOnly = true)
    public Page<IncidentResponse> studentHistory(Long studentId, Pageable pageable) {
        studentService.find(studentId);
        IncidentFilter filter = new IncidentFilter(studentId, null, null, null, null, null, null, false);
        return list(filter, pageable);
    }

    // O professor só pode alterar as próprias ocorrências
    @Transactional
    public IncidentResponse update(String authenticatedLogin, IncidentUpdateRequest request) {
        Incident incident = findEditable(request.id(), authenticatedLogin);

        SchoolClass schoolClass = request.idSchoolClass() != null
                ? activeSchoolClass(request.idSchoolClass())
                : incident.getSchoolClass();
        Long studentId = request.idStudent() != null ? request.idStudent() : incident.getStudent().getId();
        Student student = studentOfSchoolClass(schoolClass, studentId);
        IncidentType type = categoryService.findType(
                isBlank(request.type()) ? incident.getType().getName() : request.type(),
                isBlank(request.category()) ? incident.getCategory().getName() : request.category());

        incident.update(schoolClass, student, type, request.description());
        return new IncidentResponse(incident);
    }

    @Transactional
    public IncidentResponse changeStatus(IncidentStatusRequest request) {
        Incident incident = find(request.id());
        if (incident.isDeleted()) {
            throw new BusinessException("Não é possível alterar o status de uma ocorrência excluída.");
        }
        if (incident.getStatus() == IncidentStatus.CLOSED) {
            throw new BusinessException("A ocorrência está fechada e não pode mais ter o status alterado.");
        }
        LocalDateTime updateDate = request.updateDate() != null ? request.updateDate() : LocalDateTime.now();
        if (updateDate.isBefore(incident.getRegisterDate())) {
            throw new BusinessException("A data de atualização não pode ser anterior à data de registro.");
        }
        incident.changeStatus(request.status(), updateDate);
        return new IncidentResponse(incident);
    }

    // O professor só pode excluir as próprias ocorrências (exclusão lógica)
    @Transactional
    public void delete(String authenticatedLogin, Long id) {
        findEditable(id, authenticatedLogin).delete();
    }

    public List<StatusResponse> statuses() {
        return Arrays.stream(IncidentStatus.values()).map(StatusResponse::new).toList();
    }

    // Dashboard: totais por status, categoria, tipo, turma, ranking de reincidentes e volume por período
    @Transactional(readOnly = true)
    public IncidentSummaryResponse summary() {
        Map<YearMonth, Long> byMonth = repository.findAllRegisterDates().stream()
                .collect(Collectors.groupingBy(YearMonth::from, TreeMap::new, Collectors.counting()));

        return new IncidentSummaryResponse(
                repository.countByStatusInAndDeletedFalse(EnumSet.allOf(IncidentStatus.class)),
                repository.countByStatusInAndDeletedFalse(IncidentStatus.ACTIVE),
                count(IncidentStatus.WAITING),
                count(IncidentStatus.PROGRESSING),
                count(IncidentStatus.SOLVED),
                count(IncidentStatus.UNSOLVED),
                count(IncidentStatus.CLOSED),
                toKeyValue(repository.countByCategory()),
                toKeyValue(repository.countByType()),
                toKeyValue(repository.countBySchoolClass()),
                toKeyValue(repository.rankingByStudent(PageRequest.of(0, 10))),
                byMonth.entrySet().stream().map(e -> new KeyValue(e.getKey().toString(), e.getValue())).toList()
        );
    }

    private long count(IncidentStatus status) {
        return repository.countByStatusInAndDeletedFalse(Set.of(status));
    }

    private List<KeyValue> toKeyValue(List<Object[]> rows) {
        return rows.stream().map(row -> new KeyValue((String) row[0], ((Number) row[1]).longValue())).toList();
    }

    private Incident find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Ocorrência não encontrada."));
    }

    private Incident findEditable(Long id, String authenticatedLogin) {
        Incident incident = find(id);
        if (!incident.isAuthor(findUser(authenticatedLogin))) {
            throw new ForbiddenOperationException("O professor só pode alterar ou excluir as próprias ocorrências.");
        }
        if (incident.isDeleted()) {
            throw new BusinessException("A ocorrência já foi excluída.");
        }
        return incident;
    }

    private User findUser(String login) {
        return userRepository.findByLogin(login).orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
    }

    private SchoolClass activeSchoolClass(Long id) {
        SchoolClass schoolClass = schoolClassService.find(id);
        if (schoolClass.isCanceled()) {
            throw new BusinessException("Não é possível registrar ocorrências em uma turma cancelada.");
        }
        return schoolClass;
    }

    // Somente alunos pertencentes à turma selecionada podem ser escolhidos
    private Student studentOfSchoolClass(SchoolClass schoolClass, Long studentId) {
        Student student = studentService.find(studentId);
        if (!schoolClass.hasStudent(studentId)) {
            throw new BusinessException("O aluno não pertence à turma informada.");
        }
        if (!student.isActive()) {
            throw new BusinessException("O aluno está inativo.");
        }
        return student;
    }

    private IncidentStatus parseStatus(String value) {
        try {
            return IncidentStatus.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Status inexistente: " + value);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Specification<Incident> specification(IncidentFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.studentId() != null) {
                predicates.add(cb.equal(root.get("student").get("id"), filter.studentId()));
            }
            if (filter.schoolClassId() != null) {
                predicates.add(cb.equal(root.get("schoolClass").get("id"), filter.schoolClassId()));
            }
            if (!isBlank(filter.category())) {
                predicates.add(cb.equal(root.get("category").get("name"), Names.normalize(filter.category())));
            }
            if (!isBlank(filter.type())) {
                predicates.add(cb.equal(root.get("type").get("name"), Names.normalize(filter.type())));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("registerDate"), filter.startDate().atStartOfDay()));
            }
            if (filter.endDate() != null) {
                predicates.add(cb.lessThan(root.get("registerDate"), filter.endDate().plusDays(1).atStartOfDay()));
            }
            if (filter.deleted() != null) {
                predicates.add(cb.equal(root.get("deleted"), filter.deleted()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
