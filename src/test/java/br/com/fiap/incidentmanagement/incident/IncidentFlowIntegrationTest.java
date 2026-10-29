package br.com.fiap.incidentmanagement.incident;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Teste de integração do fluxo completo, com login real (JWT) de cada perfil e banco H2
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IncidentFlowIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    private String login(String login) throws Exception {
        String body = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.tokenJWT");
    }

    private int idOf(String json) {
        return JsonPath.read(json, "$.id");
    }

    @Test
    @DisplayName("Administrativo cadastra turma e aluno, professor registra, coordenador acompanha e analista consulta")
    void fullFlow() throws Exception {
        String admin = login("admin@escola.com");
        String professor = login("professor@escola.com");
        String coordinator = login("coordenador@escola.com");
        String analyst = login("analista@escola.com");

        int classId = idOf(mockMvc.perform(post("/schoolclasses").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"1A\",\"shift\":\"MANHA\",\"year\":\"2026\",\"semester\":\"SEGUNDO\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        int studentId = idOf(mockMvc.perform(post("/students").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana Souza\",\"birthDate\":\"10/05/2012\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());

        // Antes do vínculo, o aluno não pode receber ocorrência nesta turma
        String incident = """
                {"idSchoolClass":"%d","idStudent":"%d","registerDate":"20/10/2026 - 09:30",
                 "category":"DISCIPLINAR","type":"INDISCIPLINA","description":"Conversa excessiva"}
                """.formatted(classId, studentId);
        mockMvc.perform(post("/incidents").header("Authorization", professor)
                        .contentType(MediaType.APPLICATION_JSON).content(incident))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/schoolclasses/" + classId + "/students").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"studentIds\":[" + studentId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ana Souza"));

        int incidentId = idOf(mockMvc.perform(post("/incidents").header("Authorization", professor)
                        .contentType(MediaType.APPLICATION_JSON).content(incident))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentName").value("Ana Souza"))
                .andExpect(jsonPath("$.registerDate").value("20/10/2026 - 09:30"))
                .andReturn().getResponse().getContentAsString());

        // O coordenador coloca a ocorrência em atendimento; o professor não pode
        String status = "{\"id\":" + incidentId + ",\"status\":\"PROGRESSING\",\"updateDate\":\"21/10/2026 - 10:00\"}";
        mockMvc.perform(patch("/incidents/status").header("Authorization", professor)
                        .contentType(MediaType.APPLICATION_JSON).content(status))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/incidents/status").header("Authorization", coordinator)
                        .contentType(MediaType.APPLICATION_JSON).content(status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROGRESSING"));

        // O analista consulta com filtros e o histórico do aluno
        mockMvc.perform(get("/incidents/status/active?sort=student.name").header("Authorization", analyst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(incidentId));
        mockMvc.perform(get("/incidents?category=disciplinar&startDate=2026-10-01&endDate=2026-10-31&sort=category")
                        .header("Authorization", analyst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/students/" + studentId + "/incidents").header("Authorization", analyst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("INDISCIPLINA"));

        // Dashboard
        mockMvc.perform(get("/incidents/summary").header("Authorization", coordinator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressing").value(1))
                .andExpect(jsonPath("$.bySchooclass[0].key").value("1A"))
                .andExpect(jsonPath("$.byStudent[0].key").value("Ana Souza"));

        // O professor exclui a própria ocorrência (exclusão lógica)
        mockMvc.perform(delete("/incidents/" + incidentId).header("Authorization", professor))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/incidents/" + incidentId).header("Authorization", analyst))
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    @DisplayName("Login inválido retorna 401 e token inválido também")
    void invalidCredentials() throws Exception {
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin@escola.com\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/students").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuário inativado não consegue mais fazer login")
    void inactiveUserCannotLogin() throws Exception {
        String admin = login("admin@escola.com");
        int userId = idOf(mockMvc.perform(post("/users").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Maria\",\"login\":\"maria@escola.com\",\"password\":\"senha123\",\"role\":\"PROFESSOR\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());

        mockMvc.perform(delete("/users/" + userId).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"maria@escola.com\",\"password\":\"senha123\"}"))
                .andExpect(status().isUnauthorized());
    }
}
