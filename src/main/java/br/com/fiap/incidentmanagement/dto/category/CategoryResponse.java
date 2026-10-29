package br.com.fiap.incidentmanagement.dto.category;

import br.com.fiap.incidentmanagement.domain.category.Category;

public record CategoryResponse(Long id, String name) {
    public CategoryResponse(Category category) {
        this(category.getId(), category.getName());
    }
}
