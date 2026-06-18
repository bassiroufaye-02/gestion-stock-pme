package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.service.ProduitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pme.stock.config.SecurityConfig;
import com.pme.stock.config.SecurityProblemSupport;
import org.springframework.context.annotation.Import;

@WebMvcTest(ProduitController.class)
@Import({SecurityConfig.class, SecurityProblemSupport.class})
@DisplayName("ProduitController - Tests d'intégration Web")
class ProduitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProduitService produitService;

    @MockBean
    private com.pme.stock.security.service.JwtService jwtService;

    @MockBean
    private com.pme.stock.security.service.UserDetailsServiceImpl userDetailsService;

    private ProduitResponse produitResponse;
    private ProduitRequest produitRequest;

    @BeforeEach
    void setUp() {
        produitResponse = ProduitResponse.builder()
                .id(1L)
                .reference("REF-001")
                .designation("Ordinateur Portable")
                .prixAchat(new BigDecimal("400.00"))
                .prixVente(new BigDecimal("650.00"))
                .quantiteStock(10)
                .seuilAlerte(3)
                .actif(true)
                .enAlerte(false)
                .enRupture(false)
                .build();

        produitRequest = new ProduitRequest();
        produitRequest.setReference("REF-001");
        produitRequest.setDesignation("Ordinateur Portable");
        produitRequest.setPrixAchat(new BigDecimal("400.00"));
        produitRequest.setPrixVente(new BigDecimal("650.00"));
        produitRequest.setQuantiteStock(10);
        produitRequest.setSeuilAlerte(3);
    }

    // =====================================================================
    // Tests GET
    // =====================================================================

    @Nested
    @DisplayName("GET /api/v1/produits/{id}")
    class GetProduitTests {

        @Test
        @WithMockUser(roles = "EMPLOYE")
        @DisplayName("✅ 200 OK - Retourne le produit si authentifié")
        void getProduit_utilisateurAuthentifie_retourne200() throws Exception {
            given(produitService.trouverParId(1L)).willReturn(produitResponse);

            mockMvc.perform(get("/api/v1/produits/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reference").value("REF-001"))
                    .andExpect(jsonPath("$.designation").value("Ordinateur Portable"))
                    .andExpect(jsonPath("$.quantiteStock").value(10));
        }

        @Test
        @DisplayName("❌ 401 Unauthorized - Sans authentification")
        void getProduit_sansAuthentification_retourne401() throws Exception {
            mockMvc.perform(get("/api/v1/produits/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =====================================================================
    // Tests POST (création)
    // =====================================================================

    @Nested
    @DisplayName("POST /api/v1/produits")
    class PostProduitTests {

        @Test
        @WithMockUser(roles = "GESTIONNAIRE")
        @DisplayName("✅ 201 Created - Crée le produit si GESTIONNAIRE")
        void creerProduit_roleGestionnaire_retourne201() throws Exception {
            given(produitService.creer(any(ProduitRequest.class))).willReturn(produitResponse);

            mockMvc.perform(post("/api/v1/produits")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(produitRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.reference").value("REF-001"));
        }

        @Test
        @WithMockUser(roles = "EMPLOYE")
        @DisplayName("❌ 403 Forbidden - EMPLOYE ne peut pas créer")
        void creerProduit_roleEmploye_retourne403() throws Exception {
            mockMvc.perform(post("/api/v1/produits")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(produitRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ 400 Bad Request - Corps invalide (référence manquante)")
        void creerProduit_requeteInvalide_retourne400() throws Exception {
            produitRequest.setReference(null); // Invalide

            mockMvc.perform(post("/api/v1/produits")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(produitRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =====================================================================
    // Tests DELETE
    // =====================================================================

    @Nested
    @DisplayName("DELETE /api/v1/produits/{id}")
    class DeleteProduitTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ 204 No Content - ADMIN peut désactiver")
        void desactiverProduit_roleAdmin_retourne204() throws Exception {
            willDoNothing().given(produitService).desactiver(1L);

            mockMvc.perform(delete("/api/v1/produits/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "GESTIONNAIRE")
        @DisplayName("❌ 403 Forbidden - GESTIONNAIRE ne peut pas supprimer")
        void desactiverProduit_roleGestionnaire_retourne403() throws Exception {
            mockMvc.perform(delete("/api/v1/produits/1").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    // =====================================================================
    // Tests alertes
    // =====================================================================

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("✅ GET /alertes - Retourne la liste des produits en alerte")
    void listerAlertes_doitRetournerListeAlertes() throws Exception {
        ProduitResponse enAlerte = ProduitResponse.builder()
                .id(2L).reference("REF-002").designation("Souris")
                .prixAchat(BigDecimal.TEN).prixVente(BigDecimal.TEN)
                .quantiteStock(1).seuilAlerte(5).enAlerte(true).build();

        given(produitService.listerProduitsEnAlerte()).willReturn(List.of(enAlerte));

        mockMvc.perform(get("/api/v1/produits/alertes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].enAlerte").value(true))
                .andExpect(jsonPath("$[0].reference").value("REF-002"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYE")
    @DisplayName("✅ GET /reference/{reference} - Retourne le produit")
    void trouverParReference_retourne200() throws Exception {
        given(produitService.trouverParReference("REF-001")).willReturn(produitResponse);

        mockMvc.perform(get("/api/v1/produits/reference/REF-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("REF-001"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYE")
    @DisplayName("✅ GET / - Retourne tous les produits (paginés)")
    void listerTous_retourne200() throws Exception {
        given(produitService.listerTous(any())).willReturn(new PageImpl<>(List.of(produitResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/produits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reference").value("REF-001"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYE")
    @DisplayName("✅ GET /recherche - Recherche des produits")
    void rechercher_retourne200() throws Exception {
        given(produitService.rechercher(eq("Portable"), any())).willReturn(new PageImpl<>(List.of(produitResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/produits/recherche").param("q", "Portable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reference").value("REF-001"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYE")
    @DisplayName("✅ GET /categorie/{categorieId} - Liste par catégorie")
    void listerParCategorie_retourne200() throws Exception {
        given(produitService.listerParCategorie(eq(1L), any())).willReturn(new PageImpl<>(List.of(produitResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/produits/categorie/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYE")
    @DisplayName("✅ GET /alertes/count - Compte les alertes")
    void compterAlertes_retourne200() throws Exception {
        given(produitService.compterProduitsEnAlerte()).willReturn(5L);

        mockMvc.perform(get("/api/v1/produits/alertes/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("✅ PUT /{id} - Modifie le produit")
    void modifier_retourne200() throws Exception {
        given(produitService.modifier(eq(1L), any(ProduitRequest.class))).willReturn(produitResponse);

        mockMvc.perform(put("/api/v1/produits/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produitRequest)))
                .andExpect(status().isOk());
    }
}
