package br.com.fiap.incidentmanagement.repository;

import br.com.fiap.incidentmanagement.domain.category.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentTypeRepository extends JpaRepository<IncidentType, Long> {
    List<IncidentType> findAllByCategoryNameIgnoreCaseOrderByName(String categoryName);

    Optional<IncidentType> findByNameIgnoreCaseAndCategoryNameIgnoreCase(String name, String categoryName);

    boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);

    boolean existsByCategoryId(Long categoryId);
}
