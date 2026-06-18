package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.config.SecurityProblemSupport;
import com.pme.stock.dto.response.*;
import com.pme.stock.entity.StatutCommande;
import com.pme.stock.security.service.JwtService;
import com.pme.stock.security.service.UserDetailsServiceImpl;
import com.pme.stock.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("DashboardController")
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DashboardService dashboardService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProblemSupport securityProblemSupport;

    @Test
    @DisplayName("GET /dashboard retourne 200 avec le snapshot complet")
    @WithMockUser
    void getDashboard_retourne200() throws Exception {
        DashboardResponse response = DashboardResponse.builder()
                .genereLe(LocalDateTime.now())
                .stock(StockValeurResponse.builder()
                        .valeurTotaleAchat(new BigDecimal("4500000"))
                        .valeurTotaleVente(new BigDecimal("6200000"))
                        .margePotentielle(new BigDecimal("1700000"))
                        .nombreProduitsActifs(142)
                        .quantiteTotaleStock(8450)
                        .build())
                .produitsEnAlerte(Collections.emptyList())
                .nombreProduitsEnRupture(0)
                .commandesParStatut(Collections.emptyList())
                .topProduits(Collections.emptyList())
                .chiffreAffairesMoisCourant(ChiffreAffairesMoisResponse.builder()
                        .annee(2026).mois(6)
                        .chiffreAffairesTTC(BigDecimal.ZERO)
                        .nombreCommandesLivrees(0)
                        .build())
                .nombreFournisseursActifs(3)
                .nombreCommandesFournisseursEnAttente(1)
                .build();

        when(dashboardService.genererDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.valeurTotaleAchat").value(4500000))
                .andExpect(jsonPath("$.nombreFournisseursActifs").value(3));
    }

    @Test
    @DisplayName("GET /dashboard/stock/valeur retourne 200")
    @WithMockUser
    void getValeurStock_retourne200() throws Exception {
        when(dashboardService.calculerValeurStock()).thenReturn(
                StockValeurResponse.builder()
                        .valeurTotaleAchat(new BigDecimal("4500000"))
                        .valeurTotaleVente(new BigDecimal("6200000"))
                        .margePotentielle(new BigDecimal("1700000"))
                        .nombreProduitsActifs(142)
                        .quantiteTotaleStock(8450)
                        .build()
        );

        mockMvc.perform(get("/api/v1/dashboard/stock/valeur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.margePotentielle").value(1700000));
    }

    @Test
    @DisplayName("GET /dashboard/commandes/stats retourne 200")
    @WithMockUser
    void getStatsCommandes_retourne200() throws Exception {
        when(dashboardService.statistiquesCommandesParStatut()).thenReturn(List.of(
                CommandeStatutStatResponse.builder()
                        .statut(StatutCommande.LIVREE)
                        .nombreCommandes(37)
                        .montantTotalTTC(new BigDecimal("2150000"))
                        .build()
        ));

        mockMvc.perform(get("/api/v1/dashboard/commandes/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statut").value("LIVREE"))
                .andExpect(jsonPath("$[0].nombreCommandes").value(37));
    }

    @Test
    @DisplayName("GET /dashboard/produits/top retourne 200 avec limite par défaut")
    @WithMockUser
    void getTopProduits_retourne200() throws Exception {
        when(dashboardService.topProduitsVendus(eq(5))).thenReturn(List.of(
                ProduitTopVenteResponse.builder()
                        .produitId(12L).reference("PROD-0012")
                        .designation("Ramette papier A4 80g")
                        .quantiteTotaleCommandee(320)
                        .chiffreAffairesHT(new BigDecimal("960000"))
                        .build()
        ));

        mockMvc.perform(get("/api/v1/dashboard/produits/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference").value("PROD-0012"));
    }

    @Test
    @DisplayName("GET /dashboard/produits/top avec limite explicite retourne 200")
    @WithMockUser
    void getTopProduits_avecLimite_retourne200() throws Exception {
        when(dashboardService.topProduitsVendus(eq(10))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/dashboard/produits/top").param("limite", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /dashboard/ca/mensuel sans paramètres utilise le mois courant")
    @WithMockUser
    void getChiffreAffairesMois_sansParametres_retourne200() throws Exception {
        when(dashboardService.chiffreAffairesMois(anyInt(), anyInt())).thenReturn(
                ChiffreAffairesMoisResponse.builder()
                        .annee(2026).mois(6)
                        .chiffreAffairesTTC(new BigDecimal("3450000"))
                        .nombreCommandesLivrees(28)
                        .build()
        );

        mockMvc.perform(get("/api/v1/dashboard/ca/mensuel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiffreAffairesTTC").value(3450000));
    }

    @Test
    @DisplayName("GET /dashboard/ca/mensuel avec annee et mois explicites retourne 200")
    @WithMockUser
    void getChiffreAffairesMois_avecParametres_retourne200() throws Exception {
        when(dashboardService.chiffreAffairesMois(eq(2025), eq(12))).thenReturn(
                ChiffreAffairesMoisResponse.builder()
                        .annee(2025).mois(12)
                        .chiffreAffairesTTC(new BigDecimal("5000000"))
                        .nombreCommandesLivrees(40)
                        .build()
        );

        mockMvc.perform(get("/api/v1/dashboard/ca/mensuel")
                        .param("annee", "2025")
                        .param("mois", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annee").value(2025))
                .andExpect(jsonPath("$.mois").value(12));
    }
}
