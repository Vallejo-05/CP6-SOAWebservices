package br.com.fiap.incidentmanagement.category;

import br.com.fiap.incidentmanagement.domain.category.IncidentType;
import br.com.fiap.incidentmanagement.repository.IncidentTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class IncidentTypeRepositoryTest {
    @Autowired
    IncidentTypeRepository repository;

    @Test
    @DisplayName("Os tipos são agrupados por categoria (dados iniciais da migration)")
    void typesByCategory() {
        assertThat(repository.findAllByCategoryNameIgnoreCaseOrderByName("disciplinar"))
                .extracting(IncidentType::getName)
                .containsExactly("BRIGA", "INDISCIPLINA", "USO_DE_CELULAR");
    }

    @Test
    @DisplayName("Localiza o tipo somente dentro da categoria correta")
    void findByNameAndCategory() {
        assertThat(repository.findByNameIgnoreCaseAndCategoryNameIgnoreCase("ATRASO", "ADMINISTRATIVA")).isPresent();
        assertThat(repository.findByNameIgnoreCaseAndCategoryNameIgnoreCase("ATRASO", "PEDAGOGICA")).isEmpty();
    }
}
