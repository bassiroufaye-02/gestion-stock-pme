package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.config.SecurityConfig;
import com.pme.stock.config.SecurityProblemSupport;
import com.pme.stock.dto.request.MouvementStockRequest;
import com.pme.stock.dto.response.MouvementStockResponse;
import com.pme.stock.entity.MouvementStock;
import com.pme.stock.mapper.MouvementStockMapper;
import com.pme.stock.service.impl.StockService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@Import({SecurityConfig.class, SecurityProblemSupport.class})
@ActiveProfiles("test")
@DisplayName("StockController - Tests d'intégration MockMvc")
class StockControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private StockService stockService;
    @MockBean  private MouvementStockMapper mouvementStockMapper;
    @MockBean  private com.pme.stock.security.service.JwtService jwtService;
    @MockBean  private com.pme.stock.security.service.UserDetailsServiceImpl userDetailsService;

    private MouvementStockResponse buildResponse() {
        MouvementStockResponse response = new MouvementStockResponse();
        response.setId(1L);
        response.setTypeMouvement(MouvementStock.TypeMouvement.ENTREE);
        response.setQuantite(5);
        response.setMotif("Réapprovisionnement");
        response.setProduitId(1L);
        response.setProduitReference("REF-001");
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    @Test
    @DisplayName("❌ POST /stock/mouvements - non authentifié retourne 401")
    void effectuerMouvement_nonAuthentifie_retourne401() throws Exception {
        MouvementStockRequest request = new MouvementStockRequest();
        request.setTypeMouvement(MouvementStock.TypeMouvement.ENTREE);
        request.setQuantite(5);
        request.setProduitId(1L);

        mockMvc.perform(post("/api/v1/stock/mouvements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("✅ POST /stock/mouvements - GESTIONNAIRE peut effectuer un mouvement (201)")
    @WithMockUser(roles = "GESTIONNAIRE")
    void effectuerMouvement_gestionnaire_retourne201() throws Exception {
        MouvementStockRequest request = new MouvementStockRequest();
        request.setTypeMouvement(MouvementStock.TypeMouvement.ENTREE);
        request.setQuantite(5);
        request.setMotif("Réapprovisionnement");
        request.setProduitId(1L);

        MouvementStock mouvement = MouvementStock.builder()
                .id(1L).typeMouvement(MouvementStock.TypeMouvement.ENTREE).quantite(5).build();

        when(stockService.effectuerMouvement(any())).thenReturn(mouvement);
        when(mouvementStockMapper.toResponse(mouvement)).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/stock/mouvements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeMouvement").value("ENTREE"));
    }

    @Test
    @DisplayName("✅ GET /stock/mouvements/produit/{id} - retourne l'historique paginé")
    @WithMockUser(roles = "ADMIN")
    void listerMouvements_produitExistant_retourne200() throws Exception {
        MouvementStock mouvement = MouvementStock.builder()
                .id(1L).typeMouvement(MouvementStock.TypeMouvement.ENTREE).quantite(5).build();
        PageRequest pageable = PageRequest.of(0, 20);
        when(stockService.listerMouvementsProduit(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(mouvement), pageable, 1));
        when(mouvementStockMapper.toResponse(mouvement)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/stock/mouvements/produit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].typeMouvement").value("ENTREE"));
    }

    @Test
    @DisplayName("❌ GET /stock/mouvements/produit/{id} - EMPLOYE refusé (403)")
    @WithMockUser(roles = "EMPLOYE")
    void listerMouvements_employe_retourne403() throws Exception {
        mockMvc.perform(get("/api/v1/stock/mouvements/produit/1"))
                .andExpect(status().isForbidden());
    }
}
