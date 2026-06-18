package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.service.impl.CategorieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pme.stock.config.SecurityConfig;
import com.pme.stock.config.SecurityProblemSupport;
import org.springframework.context.annotation.Import;

@WebMvcTest(CategorieController.class)
@Import({SecurityConfig.class, SecurityProblemSupport.class})
@ActiveProfiles("test")
@DisplayName("CategorieController - Tests d'intégration MockMvc")
class CategorieControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private CategorieService categorieService;
    @MockBean  private com.pme.stock.security.service.JwtService jwtService;
    @MockBean  private com.pme.stock.security.service.UserDetailsServiceImpl userDetailsService;

    private CategorieResponse buildResponse() {
        return CategorieResponse.builder()
                .id(1L).code("INFO").libelle("Informatique")
                .description("Matériel info").actif(true)
                .nombreProduits(3).createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("✅ GET /categories - authentifié retourne 200 + liste")
    @WithMockUser(roles = "EMPLOYE")
    void listerActives_authentifie_retourne200() throws Exception {
        when(categorieService.listerActives()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("INFO"))
                .andExpect(jsonPath("$[0].libelle").value("Informatique"));
    }

    @Test
    @DisplayName("❌ GET /categories - non authentifié retourne 401")
    void listerActives_nonAuthentifie_retourne401() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("✅ POST /categories - admin peut créer une catégorie")
    @WithMockUser(roles = "ADMIN")
    void creer_admin_retourne201() throws Exception {
        CategorieRequest request = new CategorieRequest();
        request.setCode("NEW"); request.setLibelle("Nouvelle catégorie");

        when(categorieService.creer(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("❌ POST /categories - EMPLOYE ne peut pas créer (403)")
    @WithMockUser(roles = "EMPLOYE")
    void creer_employe_retourne403() throws Exception {
        CategorieRequest request = new CategorieRequest();
        request.setCode("NEW"); request.setLibelle("Test");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("❌ POST /categories - code manquant retourne 400")
    @WithMockUser(roles = "ADMIN")
    void creer_codeManquant_retourne400() throws Exception {
        CategorieRequest request = new CategorieRequest();
        request.setLibelle("Sans code");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ DELETE /categories/{id} - seul ADMIN peut désactiver")
    @WithMockUser(roles = "ADMIN")
    void desactiver_admin_retourne204() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("❌ DELETE /categories/{id} - GESTIONNAIRE refusé (403)")
    @WithMockUser(roles = "GESTIONNAIRE")
    void desactiver_gestionnaire_retourne403() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("✅ GET /categories/{id} - retourne la catégorie")
    @WithMockUser(roles = "EMPLOYE")
    void trouverParId_retourne200() throws Exception {
        when(categorieService.trouverParId(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("INFO"));
    }

    @Test
    @DisplayName("✅ GET /categories/toutes - ADMIN retourne la liste")
    @WithMockUser(roles = "ADMIN")
    void listerToutes_admin_retourne200() throws Exception {
        when(categorieService.listerToutes()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/categories/toutes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("INFO"));
    }

    @Test
    @DisplayName("✅ PUT /categories/{id} - ADMIN peut modifier")
    @WithMockUser(roles = "ADMIN")
    void modifier_admin_retourne200() throws Exception {
        CategorieRequest request = new CategorieRequest();
        request.setCode("INFO"); request.setLibelle("Informatique Updated");

        when(categorieService.modifier(eq(1L), any())).thenReturn(buildResponse());

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
