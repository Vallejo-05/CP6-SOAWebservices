package br.com.fiap.incidentmanagement.domain.category;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Categoria da ocorrência (ex.: DISCIPLINAR, PEDAGOGICA, ADMINISTRATIVA)
@Entity(name = "Category")
@Table(name = "categorias")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String name;

    public Category(String name) {
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }
}
