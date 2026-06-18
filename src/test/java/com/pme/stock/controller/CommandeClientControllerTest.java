package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.config.SecurityConfig;
import com.pme.stock.config.SecurityProblemSupport;
import com.pme.stock.dto.request.CommandeClientRequest;
import com.pme.stock.dto.request.LigneCommandeRequest;
import com.pme.stock.dto.response.CommandeClientResponse;
import com.pme.stock.entity.StatutCommande;
import com.pme.stock.service.CommandeClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(CommandeClientController.class)
@Import({SecurityConfig.class, SecurityProblemSupport.class})
@ActiveProfiles("test")
@DisplayName("CommandeClientController - Tests d'intégration MockMvc")
class CommandeClientControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private CommandeClientService commandeClientService;
    @MockBean  private com.pme.stock.security.service.JwtService jwtService;
    @MockBean  private com.pme.stock.security.service.UserDetailsServiceImpl userDetailsService;

    private CommandeClientResponse buildResponse() {
        CommandeClientResponse response = new CommandeClientResponse();
        response.setId(1L);
        response.setNumeroCommande("CMD-202506-0001");
        response.setDateCommande(LocalDate.now());
        response.setStatut(StatutCommande.BROUILLON);
        response.setMontantHT(BigDecimal.valueOf(200));
        response.setMontantTTC(BigDecimal.valueOf(236));
        response.setClientId(1L);
        response.setClientRaisonSociale("Client Test");
        return response;
    }

    @Test
    @DisplayName("❌ POST /commandes - non authentifié retourne 401")
    void creer_nonAuthentifie_retourne401() throws Exception {
        CommandeClientRequest request = new CommandeClientRequest();
        request.setClientId(1L);
        request.setLignes(List.of());

        mockMvc.perform(post("/api/v1/commandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("✅ POST /commandes - GESTIONNAIRE peut créer une commande (201)")
    @WithMockUser(roles = "GESTIONNAIRE")
    void creer_gestionnaire_retourne201() throws Exception {
        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(1L);
        ligne.setQuantite(2);
        ligne.setPrixUnitaireHT(BigDecimal.valueOf(100));

        CommandeClientRequest request = new CommandeClientRequest();
        request.setClientId(1L);
        request.setLignes(List.of(ligne));

        CommandeClientResponse response = buildResponse();
        when(commandeClientService.creer(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/commandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCommande").value("CMD-202506-0001"));
    }

    @Test
    @DisplayName("✅ GET /commandes/{id} - authentifié retourne la commande (200)")
    @WithMockUser(roles = "EMPLOYE")
    void trouverParId_authentifie_retourne200() throws Exception {
        when(commandeClientService.trouverParId(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/commandes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("BROUILLON"));
    }

    @Test
    @DisplayName("❌ GET /commandes/{id} - non authentifié retourne 401")
    void trouverParId_nonAuthentifie_retourne401() throws Exception {
        mockMvc.perform(get("/api/v1/commandes/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("✅ GET /commandes - liste paginée retourne 200")
    @WithMockUser(roles = "EMPLOYE")
    void listerToutes_authentifie_retourne200() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20);
        when(commandeClientService.listerToutes(any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), pageable, 1));

        mockMvc.perform(get("/api/v1/commandes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numeroCommande").value("CMD-202506-0001"));
    }

    @Test
    @DisplayName("✅ GET /commandes/client/{clientId} - liste par client retourne 200")
    @WithMockUser(roles = "EMPLOYE")
    void listerParClient_retourne200() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20);
        when(commandeClientService.listerParClient(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), pageable, 1));

        mockMvc.perform(get("/api/v1/commandes/client/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ GET /commandes/statut/BROUILLON - liste par statut retourne 200")
    @WithMockUser(roles = "EMPLOYE")
    void listerParStatut_retourne200() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20);
        when(commandeClientService.listerParStatut(eq(StatutCommande.BROUILLON), any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), pageable, 1));

        mockMvc.perform(get("/api/v1/commandes/statut/BROUILLON"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ GET /commandes/numero/{numero} - recherche par numéro retourne 200")
    @WithMockUser(roles = "EMPLOYE")
    void rechercherParNumero_retourne200() throws Exception {
        when(commandeClientService.rechercherParNumero("CMD-202506-0001")).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/commandes/numero/CMD-202506-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCommande").value("CMD-202506-0001"));
    }

    @Test
    @DisplayName("✅ POST /commandes/{id}/confirmer - GESTIONNAIRE confirme la commande")
    @WithMockUser(roles = "GESTIONNAIRE")
    void confirmer_gestionnaire_retourne200() throws Exception {
        CommandeClientResponse confirmed = buildResponse();
        confirmed.setStatut(StatutCommande.CONFIRMEE);
        when(commandeClientService.confirmer(1L)).thenReturn(confirmed);

        mockMvc.perform(post("/api/v1/commandes/1/confirmer")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CONFIRMEE"));
    }

    @Test
    @DisplayName("✅ PUT /commandes/{id}/statut - changement de statut réussit")
    @WithMockUser(roles = "GESTIONNAIRE")
    void changerStatut_retourne200() throws Exception {
        CommandeClientResponse annulee = buildResponse();
        annulee.setStatut(StatutCommande.ANNULEE);
        when(commandeClientService.changerStatut(eq(1L), eq(StatutCommande.ANNULEE))).thenReturn(annulee);

        mockMvc.perform(put("/api/v1/commandes/1/statut")
                        .param("nouveauStatut", "ANNULEE")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));
    }

    @Test
    @DisplayName("✅ POST /commandes/{id}/lignes - ajouter une ligne réussit")
    @WithMockUser(roles = "GESTIONNAIRE")
    void ajouterLigne_retourne200() throws Exception {
        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(1L);
        ligne.setQuantite(3);
        ligne.setPrixUnitaireHT(BigDecimal.valueOf(50));

        when(commandeClientService.ajouterLigne(eq(1L), any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/commandes/1/lignes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ligne))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ DELETE /commandes/{id}/lignes/{ligneId} - supprimer une ligne réussit")
    @WithMockUser(roles = "ADMIN")
    void supprimerLigne_retourne200() throws Exception {
        when(commandeClientService.supprimerLigne(1L, 1L)).thenReturn(buildResponse());

        mockMvc.perform(delete("/api/v1/commandes/1/lignes/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
