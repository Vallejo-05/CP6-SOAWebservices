package br.com.fiap.incidentmanagement.user;

import br.com.fiap.incidentmanagement.dto.user.UserResponse;
import br.com.fiap.incidentmanagement.service.UserService;
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
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService service;

    private static final String NEW_USER = """
            { "name": "Maria", "login": "maria@escola.com", "password": "senha123", "role": "PROFESSOR" }
            """;

    @Test
    @DisplayName("Perfil ADMINISTRATIVO cadastra usuário (201) e a senha não é devolvida")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void createAsAdmin() throws Exception {
        when(service.create(any())).thenReturn(new UserResponse(5L, "Maria", "maria@escola.com", "PROFESSOR", true));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(NEW_USER))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/users/5"))
                .andExpect(jsonPath("$.role").value("PROFESSOR"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Outros perfis não podem cadastrar usuários (403)")
    @WithMockUser(roles = "COORDENADOR")
    void createAsCoordinatorIsForbidden() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(NEW_USER))
                .andExpect(status().isForbidden());
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("Senha com menos de 6 caracteres é rejeitada (400)")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void createWithShortPassword() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("""
                        { "name": "Maria", "login": "maria@escola.com", "password": "123", "role": "PROFESSOR" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("password"));
    }

    @Test
    @DisplayName("Lista os perfis disponíveis")
    @WithMockUser(roles = "ADMINISTRATIVO")
    void roles() throws Exception {
        when(service.roles()).thenReturn(List.of("ADMINISTRATIVO", "ANALISTA", "COORDENADOR", "PROFESSOR"));

        mockMvc.perform(get("/users/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DisplayName("Qualquer usuário autenticado pode alterar a própria senha (204)")
    @WithMockUser(username = "professor@escola.com", roles = "PROFESSOR")
    void changeOwnPassword() throws Exception {
        mockMvc.perform(patch("/users/password").contentType(MediaType.APPLICATION_JSON).content("""
                        { "id": 4, "oldPassword": "123456", "newPassword": "novasenha" }
                        """))
                .andExpect(status().isNoContent());
        verify(service).changeOwnPassword(eq("professor@escola.com"), any());
    }

    @Test
    @DisplayName("Requisição sem token recebe 401")
    void withoutToken() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
    }
}
