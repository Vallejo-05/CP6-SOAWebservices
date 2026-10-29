package br.com.fiap.incidentmanagement.domain.schoolclass;

import br.com.fiap.incidentmanagement.domain.student.Student;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "SchoolClass")
@Table(name = "turmas")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno")
    private Shift shift;

    @Column(name = "ano")
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "semestre")
    private Semester semester;

    @Column(name = "cancelada")
    private boolean canceled = false;

    // Um aluno pode estar em várias turmas e uma turma reúne vários alunos (N:M)
    @ManyToMany
    @JoinTable(
            name = "turmas_alunos",
            joinColumns = @JoinColumn(name = "turma_id"),
            inverseJoinColumns = @JoinColumn(name = "aluno_id"))
    private Set<Student> students = new HashSet<>();

    public SchoolClass(String name, Shift shift, Integer year, Semester semester) {
        this.name = name;
        this.shift = shift;
        this.year = year;
        this.semester = semester;
    }

    public void update(String name, Shift shift, Integer year, Semester semester) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (shift != null) {
            this.shift = shift;
        }
        if (year != null) {
            this.year = year;
        }
        if (semester != null) {
            this.semester = semester;
        }
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void removeStudent(Student student) {
        students.remove(student);
    }

    public boolean hasStudent(Long studentId) {
        return students.stream().anyMatch(s -> s.getId().equals(studentId));
    }

    // Exclusão lógica: a turma é cancelada, preservando as ocorrências já registradas
    public void cancel() {
        this.canceled = true;
    }
}
