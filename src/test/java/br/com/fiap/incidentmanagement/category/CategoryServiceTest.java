package br.com.fiap.incidentmanagement.category;

import br.com.fiap.incidentmanagement.domain.category.Category;
import br.com.fiap.incidentmanagement.dto.category.CategoryRequest;
import br.com.fiap.incidentmanagement.dto.category.IncidentTypeRequest;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.repository.CategoryRepository;
import br.com.fiap.incidentmanagement.repository.IncidentRepository;
import br.com.fiap.incidentmanagement.repository.IncidentTypeRepository;
import br.com.fiap.incidentmanagement.service.CategoryService;
import br.com.fiap.incidentmanagement.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    CategoryRepository categoryRepository;

    @Mock
    IncidentTypeRepository typeRepository;

    @Mock
    IncidentRepository incidentRepository;

    @InjectMocks
    CategoryService service;

    @Test
    @DisplayName("Padroniza o nome da categoria (sem acentos, maiúsculo, com _)")
    void createNormalizesName() {
        when(categoryRepository.existsByNameIgnoreCase("ORIENTACAO_EDUCACIONAL")).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createCategory(new CategoryRequest("  Orientação educacional "));

        assertThat(response.name()).isEqualTo("ORIENTACAO_EDUCACIONAL");
    }

    @Test
    @DisplayName("Não permite categoria duplicada")
    void createDuplicatedCategory() {
        when(categoryRepository.existsByNameIgnoreCase("DISCIPLINAR")).thenReturn(true);

        assertThatThrownBy(() -> service.createCategory(new CategoryRequest("disciplinar")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Não exclui categoria que possui tipos vinculados")
    void deleteCategoryWithTypes() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(TestData.withId(new Category("DISCIPLINAR"), 1L)));
        when(typeRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteCategory(1L)).isInstanceOf(BusinessException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Não permite tipo duplicado na mesma categoria")
    void createDuplicatedType() {
        Category category = TestData.withId(new Category("DISCIPLINAR"), 1L);
        when(categoryRepository.findByNameIgnoreCase("DISCIPLINAR")).thenReturn(Optional.of(category));
        when(typeRepository.existsByNameIgnoreCaseAndCategoryId("BRIGA", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.createType(new IncidentTypeRequest("briga", "Disciplinar")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Não exclui tipo que já possui ocorrências")
    void deleteTypeWithIncidents() {
        when(typeRepository.findById(1L)).thenReturn(Optional.of(TestData.type(1L, "BRIGA", "DISCIPLINAR")));
        when(incidentRepository.existsByTypeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteType(1L)).isInstanceOf(BusinessException.class);
        verify(typeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Rejeita tipo que não pertence à categoria informada")
    void findTypeOfAnotherCategory() {
        when(typeRepository.findByNameIgnoreCaseAndCategoryNameIgnoreCase("ATRASO", "PEDAGOGICA"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findType("ATRASO", "PEDAGOGICA"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não existe na categoria");
    }
}
