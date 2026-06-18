package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.config.SecurityProblemSupport;
import com.pme.stock.dto.request.CommandeFournisseurRequest;
import com.pme.stock.dto.request.LigneCommandeFournisseurRequest;
import com.pme.stock.dto.response.CommandeFournisseurResponse;
import com.pme.stock.entity.StatutCommandeFournisseur;
import com.pme.stock.security.service.JwtService;
import com.pme.stock.security.service.UserDetailsServiceImpl;
import com.pme.stock.service.CommandeFournisseurService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommandeFournisseurController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("CommandeFournisseurController")
class CommandeFournisseurControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CommandeFournisseurService commandeFournisseurService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProblemSupport securityProblemSupport;

    private CommandeFournisseurResponse buildResponse() {
        return CommandeFournisseurResponse.builder()
                .id(1L).numeroCommande("CF-2026-00001")
                .dateCommande(LocalDate.now())
                .statut(StatutCommandeFournisseur.BROUILLON)
                .montantHT(BigDecimal.ZERO).montantTVA(BigDecimal.ZERO).montantTTC(BigDecimal.ZERO)
                .fournisseurId(1L).fournisseurRaisonSociale("Dakar Fournitures SARL")
                .lignes(List.of())
                .build();
    }

    @Test
    @DisplayName("POST /commandes-fournisseurs retourne 201")
    @WithMockUser(roles = "ADMIN")
    void creer_retourne201() throws Exception {
        CommandeFournisseurRequest request = new CommandeFournisseurRequest();
        request.setFournisseurId(1L);
        request.setTauxTVA(new BigDecimal("18.00"));
        
        LigneCommandeFournisseurRequest ligne = new LigneCommandeFournisseurRequest();
        ligne.setProduitId(10L);
        ligne.setQuantiteCommandee(50);
        ligne.setPrixUnitaireAchat(new BigDecimal("1500"));
        request.setLignes(List.of(ligne));

        when(commandeFournisseurService.creer(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/commandes-fournisseurs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCommande").value("CF-2026-00001"));
    }

    @Test
    @DisplayName("GET /commandes-fournisseurs/{id} retourne 200")
    @WithMockUser
    void trouverParId_retourne200() throws Exception {
        when(commandeFournisseurService.trouverParId(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/commandes-fournisseurs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("BROUILLON"));
    }

    @Test
    @DisplayName("GET /commandes-fournisseurs/numero/{numero} retourne 200")
    @WithMockUser
    void trouverParNumero_retourne200() throws Exception {
        when(commandeFournisseurService.trouverParNumero("CF-2026-00001")).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/commandes-fournisseurs/numero/CF-2026-00001"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /commandes-fournisseurs retourne 200 paginé")
    @WithMockUser
    void listerToutes_retourne200() throws Exception {
        when(commandeFournisseurService.listerToutes(any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/commandes-fournisseurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numeroCommande").value("CF-2026-00001"));
    }

    @Test
    @DisplayName("GET /commandes-fournisseurs/fournisseur/{id} retourne 200 paginé")
    @WithMockUser
    void listerParFournisseur_retourne200() throws Exception {
        when(commandeFournisseurService.listerParFournisseur(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/commandes-fournisseurs/fournisseur/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /commandes-fournisseurs/statut/{statut} retourne 200 paginé")
    @WithMockUser
    void listerParStatut_retourne200() throws Exception {
        when(commandeFournisseurService.listerParStatut(eq(StatutCommandeFournisseur.BROUILLON), any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/commandes-fournisseurs/statut/BROUILLON"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /commandes-fournisseurs/{id}/envoyer retourne 200")
    @WithMockUser(roles = "GESTIONNAIRE")
    void envoyer_retourne200() throws Exception {
        CommandeFournisseurResponse envoyee = buildResponse();
        envoyee.setStatut(StatutCommandeFournisseur.ENVOYEE);
        when(commandeFournisseurService.envoyer(1L)).thenReturn(envoyee);

        mockMvc.perform(post("/api/v1/commandes-fournisseurs/1/envoyer").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ENVOYEE"));
    }

    @Test
    @DisplayName("POST /commandes-fournisseurs/{id}/receptionner retourne 200")
    @WithMockUser(roles = "GESTIONNAIRE")
    void receptionner_retourne200() throws Exception {
        CommandeFournisseurResponse recue = buildResponse();
        recue.setStatut(StatutCommandeFournisseur.RECUE);
        when(commandeFournisseurService.receptionner(1L)).thenReturn(recue);

        mockMvc.perform(post("/api/v1/commandes-fournisseurs/1/receptionner").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RECUE"));
    }

    @Test
    @DisplayName("POST /commandes-fournisseurs/{id}/annuler retourne 200")
    @WithMockUser(roles = "ADMIN")
    void annuler_retourne200() throws Exception {
        CommandeFournisseurResponse annulee = buildResponse();
        annulee.setStatut(StatutCommandeFournisseur.ANNULEE);
        when(commandeFournisseurService.annuler(1L)).thenReturn(annulee);

        mockMvc.perform(post("/api/v1/commandes-fournisseurs/1/annuler").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));
    }

    @Test
    @DisplayName("POST /commandes-fournisseurs/{id}/lignes retourne 200")
    @WithMockUser(roles = "GESTIONNAIRE")
    void ajouterLigne_retourne200() throws Exception {
        LigneCommandeFournisseurRequest request = new LigneCommandeFournisseurRequest();
        request.setProduitId(11L);
        request.setQuantiteCommandee(20);
        request.setPrixUnitaireAchat(new BigDecimal("800"));

        when(commandeFournisseurService.ajouterLigne(eq(1L), any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/commandes-fournisseurs/1/lignes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /commandes-fournisseurs/{id}/lignes/{ligneId} retourne 200")
    @WithMockUser(roles = "GESTIONNAIRE")
    void supprimerLigne_retourne200() throws Exception {
        when(commandeFournisseurService.supprimerLigne(eq(1L), eq(5L))).thenReturn(buildResponse());

        mockMvc.perform(delete("/api/v1/commandes-fournisseurs/1/lignes/5").with(csrf()))
                .andExpect(status().isOk());
    }
}
