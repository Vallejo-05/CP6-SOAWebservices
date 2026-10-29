package br.com.fiap.incidentmanagement.incident;

import br.com.fiap.incidentmanagement.dto.incident.IncidentResponse;
import br.com.fiap.incidentmanagement.dto.incident.IncidentSummaryResponse;
import br.com.fiap.incidentmanagement.dto.incident.KeyValue;
import br.com.fiap.incidentmanagement.service.IncidentService;
import br.com.fiap.incidentmanagement.support.TestData;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    IncidentService service;

    // Mesmo formato enviado por cadastro-ocorrencia.js (IDs como texto e data dd/MM/yyyy - HH:mm)
    private static final String NEW_INCIDENT = """
            {
              "idSchoolClass": "1",
              "idStudent": "1",
              "registerDate": "28/10/2026 - 09:30",
              "category": "DISCIPLINAR",
              "type": "INDISCIPLINA",
              "description": "Conversa excessiva durante a aula"
            }
            """;

    private IncidentResponse response() {
        return new IncidentResponse(TestData.incident(1L, TestData.user(4L, "professor@escola.com", "PROFESSOR")));
    }

    @Test
    @DisplayName("Professor registra ocorrência (201)")
    @WithMockUser(username = "professor@escola.com", roles = "PROFESSOR")
    void createAsProfessor() throws Exception {
        when(service.create(eq("professor@escola.com"), any())).thenReturn(response());

        mockMvc.perform(post("/incidents").contentType(MediaType.APPLICATION_JSON).content(NEW_INCIDENT))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/incidents/1"))
                .andExpect(jsonPath("$.schoolClassName").value("Turma 1"))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.deleted").value(false));
    }

    @Test
    @DisplayName("Analista não registra ocorrências (403)")
    @WithMockUser(roles = "ANALISTA")
    void createAsAnalystIsForbidden() throws Exception {
        mockMvc.perform(post("/incidents").contentType(MediaType.APPLICATION_JSON).content(NEW_INCIDENT))
                .andExpect(status().isForbidden());
        verify(service, never()).create(any(), any());
    }

    @Test
    @DisplayName("Descrição é obrigatória (400)")
    @WithMockUser(roles = "PROFESSOR")
    void createWithoutDescription() throws Exception {
        mockMvc.perform(post("/incidents").contentType(MediaType.APPLICATION_JSON).content("""
                        { "idSchoolClass": 1, "idStudent": 1, "category": "DISCIPLINAR", "type": "BRIGA" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("description"));
    }

    @Test
    @DisplayName("Administrativo não consulta ocorrências (403)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void listAsAdminIsForbidden() throws Exception {
        mockMvc.perform(get("/incidents")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Somente o coordenador altera o status (403 para professor)")
    @WithMockUser(roles = "PROFESSOR")
    void changeStatusAsProfessorIsForbidden() throws Exception {
        mockMvc.perform(patch("/incidents/status").contentType(MediaType.APPLICATION_JSON).content("""
                        { "id": 1, "status": "SOLVED", "updateDate": "28/10/2026 - 10:00" }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("O dashboard usa os nomes de campo esperados pelo front-end (inclusive 'bySchooclass')")
    @WithMockUser(roles = "COORDENADOR")
    void summary() throws Exception {
        List<KeyValue> one = List.of(new KeyValue("X", 1));
        when(service.summary()).thenReturn(new IncidentSummaryResponse(
                5, 3, 2, 1, 1, 0, 1, one, one, one, one, one));

        mockMvc.perform(get("/incidents/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.waiting").value(2))
                .andExpect(jsonPath("$.bySchooclass[0].key").value("X"))
                .andExpect(jsonPath("$.byStudent[0].value").value(1));
    }
}
