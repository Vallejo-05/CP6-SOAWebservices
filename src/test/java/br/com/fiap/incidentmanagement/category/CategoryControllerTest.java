package br.com.fiap.incidentmanagement.category;

import br.com.fiap.incidentmanagement.dto.category.CategoryResponse;
import br.com.fiap.incidentmanagement.dto.category.IncidentTypeResponse;
import br.com.fiap.incidentmanagement.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CategoryService service;

    @Test
    @DisplayName("Administrativo cadastra categoria (201)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void createCategory() throws Exception {
        when(service.createCategory(any())).thenReturn(new CategoryResponse(4L, "SAUDE"));

        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "Saúde" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("SAUDE"));
    }

    @Test
    @DisplayName("Professor não cadastra categorias (403)")
    @WithMockUser(roles = "PROFESSOR")
    void createCategoryAsProfessorIsForbidden() throws Exception {
        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "Saúde" }
                        """))
                .andExpect(status().isForbidden());
        verify(service, never()).createCategory(any());
    }

    @Test
    @DisplayName("Cadastra tipo de ocorrência vinculado a uma categoria (201)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void createType() throws Exception {
        when(service.createType(any())).thenReturn(new IncidentTypeResponse(8L, "USO_DE_BONE", "ADMINISTRATIVA"));

        mockMvc.perform(post("/incident-types").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "Uso de boné", "category": "administrativa" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("ADMINISTRATIVA"));
    }

    @Test
    @DisplayName("O front-end lista os nomes das categorias e os tipos de cada categoria")
    @WithMockUser(roles = "PROFESSOR")
    void namesForSelects() throws Exception {
        when(service.categoryNames()).thenReturn(List.of("ADMINISTRATIVA", "DISCIPLINAR", "PEDAGOGICA"));
        when(service.typeNames("DISCIPLINAR")).thenReturn(List.of("BRIGA", "INDISCIPLINA"));

        mockMvc.perform(get("/incidents/categories")).andExpect(jsonPath("$[1]").value("DISCIPLINAR"));
        mockMvc.perform(get("/incidents/types/DISCIPLINAR")).andExpect(jsonPath("$[1]").value("INDISCIPLINA"));
    }
}
