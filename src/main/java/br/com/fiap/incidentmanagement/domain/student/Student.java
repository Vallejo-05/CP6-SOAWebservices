package br.com.fiap.incidentmanagement.domain.student;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "Student")
@Table(name = "alunos")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String name;

    @Column(name = "data_nascimento")
    private LocalDate birthDate;

    // Preenchido somente quando o aluno possui foto (ver StudentPhoto)
    @Column(name = "foto_content_type")
    private String photoContentType;

    @Column(name = "ativo")
    private boolean active = true;

    public Student(String name, LocalDate birthDate) {
        this.name = name;
        this.birthDate = birthDate;
    }

    public void update(String name, LocalDate birthDate) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
    }

    public void definePhoto(String contentType) {
        this.photoContentType = contentType;
    }

    public boolean hasPhoto() {
        return photoContentType != null;
    }

    // Exclusão lógica: o aluno é inativado, preservando seu histórico de ocorrências
    public void inactivate() {
        this.active = false;
    }
}
