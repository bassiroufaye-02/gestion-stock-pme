package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.dto.request.ClientRequest;
import com.pme.stock.dto.response.ClientResponse;
import com.pme.stock.config.SecurityProblemSupport;
import com.pme.stock.security.service.JwtService;
import com.pme.stock.security.service.UserDetailsServiceImpl;
import com.pme.stock.service.ClientService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("ClientController")
class ClientControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ClientService clientService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProblemSupport securityProblemSupport;

    private ClientResponse buildResponse() {
        ClientResponse response = new ClientResponse();
        response.setId(2L);
        response.setCode("CLI-002");
        response.setRaisonSociale("Commerce Beta");
        response.setEmail("beta@commerce.sn");
        response.setTelephone("77 500 00 00");
        response.setVille("Thiès");
        response.setActif(true);
        response.setCreatedAt(LocalDateTime.of(2026, 6, 6, 12, 46, 26));
        return response;
    }

    @Test
    @DisplayName("GET /clients/recherche retourne 200 avec application/json")
    @WithMockUser
    void recherche_retourne200() throws Exception {
        when(clientService.rechercher(eq("CLI-002"), any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/clients/recherche")
                        .param("search", "CLI-002")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "string"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].code").value("CLI-002"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /clients retourne 200 avec application/json")
    @WithMockUser
    void lister_retourne200() throws Exception {
        when(clientService.listerActifs(any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].raisonSociale").value("Commerce Beta"));
    }

    @Test
    @DisplayName("GET /clients/{id} retourne 200")
    @WithMockUser
    void trouverParId_retourne200() throws Exception {
        when(clientService.trouverParId(2L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/clients/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("POST /clients admin retourne 201")
    @WithMockUser(roles = "ADMIN")
    void creer_admin_retourne201() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setCode("CLI-005");
        request.setRaisonSociale("Nouveau client");

        when(clientService.creer(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("PUT /clients/{id} admin retourne 200")
    @WithMockUser(roles = "ADMIN")
    void modifier_admin_retourne200() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setCode("CLI-002");
        request.setRaisonSociale("Commerce Beta Updated");

        when(clientService.modifier(eq(2L), any())).thenReturn(buildResponse());

        mockMvc.perform(put("/api/v1/clients/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /clients/{id} admin retourne 204")
    @WithMockUser(roles = "ADMIN")
    void desactiver_admin_retourne204() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/2").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
