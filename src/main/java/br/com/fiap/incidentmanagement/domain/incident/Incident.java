package br.com.fiap.incidentmanagement.domain.incident;

import br.com.fiap.incidentmanagement.domain.category.Category;
import br.com.fiap.incidentmanagement.domain.category.IncidentType;
import br.com.fiap.incidentmanagement.domain.schoolclass.SchoolClass;
import br.com.fiap.incidentmanagement.domain.student.Student;
import br.com.fiap.incidentmanagement.domain.user.User;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Ocorrência: associada a uma turma, um aluno, um usuário (professor), uma categoria e um tipo
@Entity(name = "Incident")
@Table(name = "ocorrencias")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id")
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id")
    private IncidentType type;

    @Column(name = "data_registro")
    private LocalDateTime registerDate;

    @Column(name = "descricao")
    private String description;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status = IncidentStatus.WAITING;

    @Column(name = "data_atualizacao")
    private LocalDateTime updateDate;

    @Column(name = "excluida")
    private boolean deleted = false;

    public Incident(
            SchoolClass schoolClass,
            Student student,
            User user,
            IncidentType type,
            LocalDateTime registerDate,
            String description) {
        this.schoolClass = schoolClass;
        this.student = student;
        this.user = user;
        this.category = type.getCategory();
        this.type = type;
        this.registerDate = registerDate;
        this.description = description;
        this.updateDate = registerDate;
    }

    public void update(SchoolClass schoolClass, Student student, IncidentType type, String description) {
        this.schoolClass = schoolClass;
        this.student = student;
        this.type = type;
        this.category = type.getCategory();
        if (description != null && !description.isBlank()) {
            this.description = description;
        }
        this.updateDate = LocalDateTime.now();
    }

    public void changeStatus(IncidentStatus status, LocalDateTime updateDate) {
        this.status = status;
        this.updateDate = updateDate;
    }

    public boolean isAuthor(User user) {
        return this.user.getId().equals(user.getId());
    }

    // Exclusão lógica: a ocorrência é marcada como excluída
    public void delete() {
        this.deleted = true;
        this.updateDate = LocalDateTime.now();
    }
}
