package br.com.fiap.incidentmanagement.student;

import br.com.fiap.incidentmanagement.dto.student.StudentResponse;
import br.com.fiap.incidentmanagement.infra.exception.NotFoundException;
import br.com.fiap.incidentmanagement.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

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
class StudentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StudentService service;

    @Test
    @DisplayName("Cadastra aluno com data de nascimento no formato dd/MM/yyyy (201)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void create() throws Exception {
        when(service.create(any()))
                .thenReturn(new StudentResponse(1L, "Ana Souza", LocalDate.of(2012, 5, 10), null, true));

        mockMvc.perform(post("/students").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "Ana Souza", "birthDate": "10/05/2012" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.birthDate").value("10/05/2012"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Rejeita data em formato inválido (400)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void createWithInvalidDate() throws Exception {
        mockMvc.perform(post("/students").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "Ana Souza", "birthDate": "2012-05-10" }
                        """))
                .andExpect(status().isBadRequest());
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("Professor não pode cadastrar alunos (403)")
    @WithMockUser(roles = "PROFESSOR")
    void createAsProfessorIsForbidden() throws Exception {
        mockMvc.perform(post("/students").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "Ana Souza", "birthDate": "10/05/2012" }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Atualização aceita o JSON completo devolvido pela consulta (campos extras são ignorados)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void updateIgnoresExtraFields() throws Exception {
        when(service.update(any()))
                .thenReturn(new StudentResponse(1L, "Ana Silva", LocalDate.of(2012, 5, 10), null, true));

        mockMvc.perform(put("/students").contentType(MediaType.APPLICATION_JSON).content("""
                        { "id": 1, "name": "Ana Silva", "birthDate": "10/05/2012", "imageUrl": null, "active": true }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Silva"));
    }

    @Test
    @DisplayName("Envia a foto do aluno via multipart (campo 'image')")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void uploadPhoto() throws Exception {
        when(service.uploadPhoto(eq(1L), any())).thenReturn(new StudentResponse(
                1L, "Ana", LocalDate.of(2012, 5, 10), "http://localhost/students/1/image", true));
        MockMultipartFile image = new MockMultipartFile("image", "foto.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart(HttpMethod.PATCH, "/students/1/image").file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("http://localhost/students/1/image"));
    }

    @Test
    @DisplayName("A foto é pública (usada em <img src>) e devolvida com o content type correto")
    void photoIsPublic() throws Exception {
        when(service.getPhoto(1L)).thenReturn(new StudentService.Photo(new byte[]{1, 2, 3}, "image/png"));

        mockMvc.perform(get("/students/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("Aluno inexistente retorna 404")
    @WithMockUser
    void notFound() throws Exception {
        when(service.get(99L)).thenThrow(new NotFoundException("Aluno não encontrado."));

        mockMvc.perform(get("/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado."));
    }
}
