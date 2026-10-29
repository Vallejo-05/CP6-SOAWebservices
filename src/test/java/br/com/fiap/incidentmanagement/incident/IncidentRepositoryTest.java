package br.com.fiap.incidentmanagement.incident;

import br.com.fiap.incidentmanagement.domain.category.IncidentType;
import br.com.fiap.incidentmanagement.domain.incident.Incident;
import br.com.fiap.incidentmanagement.domain.incident.IncidentStatus;
import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.schoolclass.Semester;
import br.com.fiap.incidentmanagement.domain.schoolclass.Shift;
import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.domain.user.User;
import br.com.fiap.incidentmanagement.repository.IncidentRepository;
import br.com.fiap.incidentmanagement.repository.IncidentTypeRepository;
import br.com.fiap.incidentmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class IncidentRepositoryTest {
    @Autowired
    IncidentRepository repository;

    @Autowired
    IncidentTypeRepository typeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestEntityManager entityManager;

    private SchoolClass schoolClass;
    private Student ana;
    private Student bruno;
    private User professor;

    @BeforeEach
    void setUp() {
        ana = entityManager.persist(new Student("Ana", LocalDate.of(2012, 1, 1)));
        bruno = entityManager.persist(new Student("Bruno", LocalDate.of(2011, 1, 1)));
        schoolClass = new SchoolClass("1A", Shift.MANHA, 2026, Semester.SEGUNDO);
        schoolClass.addStudent(ana);
        schoolClass.addStudent(bruno);
        entityManager.persist(schoolClass);
        professor = userRepository.findByLogin("professor@escola.com").orElseThrow();
    }

    private Incident register(Student student, String type, String category) {
        IncidentType incidentType = typeRepository.findByNameIgnoreCaseAndCategoryNameIgnoreCase(type, category).orElseThrow();
        return entityManager.persist(new Incident(
                schoolClass, student, professor, incidentType, LocalDateTime.of(2026, 10, 20, 9, 0), "Teste"));
    }

    @Test
    @DisplayName("Dashboard: totaliza por categoria e ranqueia alunos reincidentes, ignorando excluídas")
    void dashboardQueries() {
        register(ana, "INDISCIPLINA", "DISCIPLINAR");
        register(ana, "RENDIMENTO", "PEDAGOGICA");
        register(bruno, "BRIGA", "DISCIPLINAR");
        register(bruno, "ATRASO", "ADMINISTRATIVA").delete();
        entityManager.flush();

        List<Object[]> byCategory = repository.countByCategory();
        assertThat(byCategory.getFirst()[0]).isEqualTo("DISCIPLINAR");
        assertThat(((Number) byCategory.getFirst()[1]).longValue()).isEqualTo(2);

        List<Object[]> ranking = repository.rankingByStudent(PageRequest.of(0, 10));
        assertThat(ranking.getFirst()[0]).isEqualTo("Ana");
        assertThat(((Number) ranking.getFirst()[1]).longValue()).isEqualTo(2);
        assertThat(((Number) ranking.get(1)[1]).longValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lista por status considerando apenas ocorrências não excluídas")
    void findByStatus() {
        Incident waiting = register(ana, "INDISCIPLINA", "DISCIPLINAR");
        Incident solved = register(bruno, "BRIGA", "DISCIPLINAR");
        solved.changeStatus(IncidentStatus.SOLVED, LocalDateTime.of(2026, 10, 21, 9, 0));
        register(ana, "RENDIMENTO", "PEDAGOGICA").delete();
        entityManager.flush();

        var active = repository.findAllByStatusInAndDeletedFalse(IncidentStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(active.getContent()).containsExactly(waiting);
        assertThat(repository.countByStatusInAndDeletedFalse(List.of(IncidentStatus.SOLVED))).isEqualTo(1);
    }
}
