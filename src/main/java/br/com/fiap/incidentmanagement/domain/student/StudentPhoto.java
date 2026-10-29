package br.com.fiap.incidentmanagement.domain.student;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Foto do aluno (atributo "foto" do MER), armazenada separadamente para não pesar nas listagens
@Entity(name = "StudentPhoto")
@Table(name = "alunos_fotos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StudentPhoto {
    @Id
    @Column(name = "aluno_id")
    private Long studentId;

    @Lob
    @Column(name = "conteudo")
    private byte[] content;
}
