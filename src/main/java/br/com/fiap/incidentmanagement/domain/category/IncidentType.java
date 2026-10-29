package br.com.fiap.incidentmanagement.domain.category;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Tipo de ocorrência (ex.: INDISCIPLINA, ATRASO, RENDIMENTO); cada categoria possui vários tipos (1:N)
@Entity(name = "IncidentType")
@Table(name = "tipos_ocorrencia")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class IncidentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Category category;

    public IncidentType(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public void update(String name, Category category) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (category != null) {
            this.category = category;
        }
    }
}
