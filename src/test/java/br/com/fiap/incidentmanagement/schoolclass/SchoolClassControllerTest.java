package br.com.fiap.incidentmanagement.schoolclass;

import br.com.fiap.incidentmanagement.domain.schoolclass.Semester;
import br.com.fiap.incidentmanagement.domain.schoolclass.Shift;
import br.com.fiap.incidentmanagement.dto.schoolclass.SchoolClassResponse;
import br.com.fiap.incidentmanagement.service.SchoolClassService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SchoolClassControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SchoolClassService service;

    private final SchoolClassResponse response =
            new SchoolClassResponse(1L, "1A", Shift.MANHA, 2026, Semester.SEGUNDO, false);

    @Test
    @DisplayName("Cadastra turma enviada pelo front-end (ano como texto) (201)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void create() throws Exception {
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/schoolclasses").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "1A", "shift": "MANHA", "year": "2026", "semester": "SEGUNDO" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shift").value("MANHA"))
                .andExpect(jsonPath("$.canceled").value(false));
    }

    @Test
    @DisplayName("Turno inexistente é rejeitado (400)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void createWithInvalidShift() throws Exception {
        mockMvc.perform(post("/schoolclasses").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "1A", "shift": "MADRUGADA", "year": 2026, "semester": "SEGUNDO" }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Lista turmas canceladas separadamente")
    @WithMockUser
    void listCanceled() throws Exception {
        when(service.list(eq(true), any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/schoolclasses/canceled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("1A"));
        verify(service).list(eq(true), any());
    }

    @Test
    @DisplayName("Vincula alunos à turma")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void linkStudents() throws Exception {
        mockMvc.perform(post("/schoolclasses/1/students").contentType(MediaType.APPLICATION_JSON).content("""
                        { "studentIds": [1, 2] }
                        """))
                .andExpect(status().isOk());
        verify(service).linkStudents(1L, List.of(1L, 2L));
    }

    @Test
    @DisplayName("Analista não pode cancelar turmas (403)")
    @WithMockUser(roles = "ANALISTA")
    void cancelAsAnalystIsForbidden() throws Exception {
        mockMvc.perform(delete("/schoolclasses/1")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Lista turnos e semestres para os campos de seleção")
    @WithMockUser
    void shiftsAndSemesters() throws Exception {
        when(service.shifts()).thenReturn(List.of("MANHA", "TARDE", "NOITE"));
        when(service.semesters()).thenReturn(List.of("PRIMEIRO", "SEGUNDO"));

        mockMvc.perform(get("/schoolclasses/shifts")).andExpect(jsonPath("$[2]").value("NOITE"));
        mockMvc.perform(get("/schoolclasses/semesters")).andExpect(jsonPath("$[0]").value("PRIMEIRO"));
    }
}
