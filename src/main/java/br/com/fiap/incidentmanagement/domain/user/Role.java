package br.com.fiap.incidentmanagement.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Entidade PERFIL do MER: PROFESSOR, ANALISTA, COORDENADOR ou ADMINISTRATIVO
@Entity(name = "Role")
@Table(name = "perfis")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Role {
    public static final String PROFESSOR = "PROFESSOR";
    public static final String ANALISTA = "ANALISTA";
    public static final String COORDENADOR = "COORDENADOR";
    public static final String ADMINISTRATIVO = "ADMINISTRATIVO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String name;
}
