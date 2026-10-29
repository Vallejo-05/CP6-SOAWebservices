package br.com.fiap.incidentmanagement.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerConfigTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("A documentação OpenAPI é pública e lista os recursos da API")
    void apiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Incident Management"))
                .andExpect(jsonPath("$.paths['/incidents']").exists())
                .andExpect(jsonPath("$.paths['/incidents/summary']").exists())
                .andExpect(jsonPath("$.paths['/students/{id}/image']").exists())
                .andExpect(jsonPath("$.paths['/schoolclasses/{id}/students']").exists())
                .andExpect(jsonPath("$.paths['/categories']").exists())
                .andExpect(jsonPath("$.paths['/incident-types']").exists())
                .andExpect(jsonPath("$.paths['/users/password']").exists());
    }
}
