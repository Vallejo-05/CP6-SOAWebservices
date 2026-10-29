package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.domain.category.Category;
import br.com.fiap.incidentmanagement.domain.category.IncidentType;
import br.com.fiap.incidentmanagement.dto.category.CategoryRequest;
import br.com.fiap.incidentmanagement.dto.category.CategoryResponse;
import br.com.fiap.incidentmanagement.dto.category.IncidentTypeRequest;
import br.com.fiap.incidentmanagement.dto.category.IncidentTypeResponse;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.infra.exception.NotFoundException;
import br.com.fiap.incidentmanagement.repository.CategoryRepository;
import br.com.fiap.incidentmanagement.repository.IncidentRepository;
import br.com.fiap.incidentmanagement.repository.IncidentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Gerencia as categorias e os tipos de ocorrência (cada categoria agrupa vários tipos)
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final IncidentTypeRepository typeRepository;
    private final IncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll(Sort.by("name")).stream().map(CategoryResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return new CategoryResponse(findCategory(id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = Names.normalize(request.name());
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Já existe uma categoria com este nome.");
        }
        return new CategoryResponse(categoryRepository.save(new Category(name)));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategory(id);
        String name = Names.normalize(request.name());
        categoryRepository.findByNameIgnoreCase(name)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Já existe uma categoria com este nome.");
                });
        category.rename(name);
        return new CategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategory(id);
        if (typeRepository.existsByCategoryId(id) || incidentRepository.existsByCategoryId(id)) {
            throw new BusinessException("A categoria possui tipos ou ocorrências vinculados e não pode ser excluída.");
        }
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<IncidentTypeResponse> listTypes(String category) {
        List<IncidentType> types = category == null || category.isBlank()
                ? typeRepository.findAll(Sort.by("category.name", "name"))
                : typeRepository.findAllByCategoryNameIgnoreCaseOrderByName(Names.normalize(category));
        return types.stream().map(IncidentTypeResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public IncidentTypeResponse getType(Long id) {
        return new IncidentTypeResponse(findType(id));
    }

    @Transactional
    public IncidentTypeResponse createType(IncidentTypeRequest request) {
        Category category = findCategoryByName(request.category());
        String name = Names.normalize(request.name());
        if (typeRepository.existsByNameIgnoreCaseAndCategoryId(name, category.getId())) {
            throw new BusinessException("Já existe um tipo com este nome nesta categoria.");
        }
        return new IncidentTypeResponse(typeRepository.save(new IncidentType(name, category)));
    }

    @Transactional
    public IncidentTypeResponse updateType(Long id, IncidentTypeRequest request) {
        IncidentType type = findType(id);
        Category category = findCategoryByName(request.category());
        String name = Names.normalize(request.name());
        typeRepository.findByNameIgnoreCaseAndCategoryNameIgnoreCase(name, category.getName())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Já existe um tipo com este nome nesta categoria.");
                });
        if (!type.getCategory().equals(category) && incidentRepository.existsByTypeId(id)) {
            throw new BusinessException("O tipo possui ocorrências registradas e não pode mudar de categoria.");
        }
        type.update(name, category);
        return new IncidentTypeResponse(type);
    }

    @Transactional
    public void deleteType(Long id) {
        IncidentType type = findType(id);
        if (incidentRepository.existsByTypeId(id)) {
            throw new BusinessException("O tipo possui ocorrências registradas e não pode ser excluído.");
        }
        typeRepository.delete(type);
    }

    // Usados pelos campos de seleção do front-end
    @Transactional(readOnly = true)
    public List<String> categoryNames() {
        return listCategories().stream().map(CategoryResponse::name).toList();
    }

    @Transactional(readOnly = true)
    public List<String> typeNames(String category) {
        findCategoryByName(category);
        return listTypes(category).stream().map(IncidentTypeResponse::name).toList();
    }

    @Transactional(readOnly = true)
    public IncidentType findType(String typeName, String categoryName) {
        return typeRepository
                .findByNameIgnoreCaseAndCategoryNameIgnoreCase(Names.normalize(typeName), Names.normalize(categoryName))
                .orElseThrow(() -> new BusinessException(
                        "O tipo '" + typeName + "' não existe na categoria '" + categoryName + "'."));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Categoria não encontrada."));
    }

    private Category findCategoryByName(String name) {
        return categoryRepository
                .findByNameIgnoreCase(Names.normalize(name))
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada: " + name));
    }

    private IncidentType findType(Long id) {
        return typeRepository.findById(id).orElseThrow(() -> new NotFoundException("Tipo de ocorrência não encontrado."));
    }
}
