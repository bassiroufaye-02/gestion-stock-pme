package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.config.SecurityProblemSupport;
import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.response.FournisseurResponse;
import com.pme.stock.security.service.JwtService;
import com.pme.stock.security.service.UserDetailsServiceImpl;
import com.pme.stock.service.FournisseurService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FournisseurController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("FournisseurController")
class FournisseurControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private FournisseurService fournisseurService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProblemSupport securityProblemSupport;

    private FournisseurResponse buildResponse() {
        return FournisseurResponse.builder()
                .id(1L).code("FOUR-001")
                .raisonSociale("Dakar Fournitures SARL")
                .email("contact@dakarfournitures.sn")
                .telephone("+221 77 000 0001")
                .ville("Dakar").pays("Sénégal")
                .actif(true).nombreProduits(2).nombreCommandes(1)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /fournisseurs retourne 201")
    @WithMockUser(roles = "ADMIN")
    void creer_retourne201() throws Exception {
        FournisseurRequest request = new FournisseurRequest();
        request.setCode("FOUR-001");
        request.setRaisonSociale("Dakar Fournitures SARL");
        request.setEmail("contact@dakarfournitures.sn");

        when(fournisseurService.creer(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/fournisseurs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("FOUR-001"));
    }

    @Test
    @DisplayName("GET /fournisseurs/{id} retourne 200")
    @WithMockUser
    void trouverParId_retourne200() throws Exception {
        when(fournisseurService.trouverParId(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/fournisseurs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.raisonSociale").value("Dakar Fournitures SARL"));
    }

    @Test
    @DisplayName("GET /fournisseurs/code/{code} retourne 200")
    @WithMockUser
    void trouverParCode_retourne200() throws Exception {
        when(fournisseurService.trouverParCode("FOUR-001")).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/fournisseurs/code/FOUR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("FOUR-001"));
    }

    @Test
    @DisplayName("GET /fournisseurs/actifs retourne 200")
    @WithMockUser
    void listerActifs_retourne200() throws Exception {
        when(fournisseurService.listerActifs()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/fournisseurs/actifs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("FOUR-001"));
    }

    @Test
    @DisplayName("GET /fournisseurs?q= retourne 200 paginé")
    @WithMockUser
    void rechercher_retourne200() throws Exception {
        when(fournisseurService.rechercher(eq("Dakar"), any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/fournisseurs").param("q", "Dakar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("FOUR-001"));
    }

    @Test
    @DisplayName("PUT /fournisseurs/{id} retourne 200")
    @WithMockUser(roles = "GESTIONNAIRE")
    void modifier_retourne200() throws Exception {
        FournisseurRequest request = new FournisseurRequest();
        request.setCode("FOUR-001");
        request.setRaisonSociale("Dakar Fournitures Modifiée");

        when(fournisseurService.modifier(eq(1L), any())).thenReturn(buildResponse());

        mockMvc.perform(put("/api/v1/fournisseurs/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /fournisseurs/{id} retourne 204")
    @WithMockUser(roles = "ADMIN")
    void desactiver_retourne204() throws Exception {
        doNothing().when(fournisseurService).desactiver(1L);

        mockMvc.perform(delete("/api/v1/fournisseurs/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
