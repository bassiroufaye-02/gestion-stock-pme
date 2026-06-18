package com.pme.stock.service;

import com.pme.stock.dto.request.MouvementStockRequest;
import com.pme.stock.entity.MouvementStock;
import com.pme.stock.entity.Produit;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.repository.MouvementStockRepository;
import com.pme.stock.repository.ProduitRepository;
import com.pme.stock.repository.UtilisateurRepository;
import com.pme.stock.service.impl.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService - Tests unitaires")
class StockServiceTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private MouvementStockRepository mouvementStockRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private StockService stockService;

    private Produit produit;

    @BeforeEach
    void setUp() {
        produit = Produit.builder()
                .id(1L).reference("REF-001").designation("PC")
                .prixAchat(BigDecimal.TEN).prixVente(BigDecimal.TEN)
                .quantiteStock(10).seuilAlerte(3).actif(true).build();

        // Simuler un utilisateur authentifié dans le SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@pme.com", null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =====================================================================
    // Tests Mouvement ENTREE
    // =====================================================================

    @Nested
    @DisplayName("Mouvement ENTREE")
    class EntreeTests {

        @Test
        @DisplayName("✅ ENTREE — quantiteStock augmentée + mouvement sauvegardé")
        void entree_doitAugmenterStock() {
            // GIVEN
            MouvementStockRequest request = new MouvementStockRequest();
            request.setTypeMouvement(MouvementStock.TypeMouvement.ENTREE);
            request.setQuantite(5);
            request.setMotif("Réapprovisionnement");
            request.setProduitId(1L);

            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));
            given(utilisateurRepository.findByEmail(any())).willReturn(Optional.empty());
            given(mouvementStockRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // WHEN
            MouvementStock result = stockService.effectuerMouvement(request);

            // THEN
            assertThat(produit.getQuantiteStock()).isEqualTo(15);
            assertThat(result.getTypeMouvement()).isEqualTo(MouvementStock.TypeMouvement.ENTREE);
            then(mouvementStockRepository).should(times(1)).save(any(MouvementStock.class));
        }
    }

    // =====================================================================
    // Tests Mouvement SORTIE
    // =====================================================================

    @Nested
    @DisplayName("Mouvement SORTIE")
    class SortieTests {

        @Test
        @DisplayName("✅ SORTIE — quantiteStock diminuée + mouvement sauvegardé")
        void sortie_stockSuffisant_doitReduireStock() {
            // GIVEN
            MouvementStockRequest request = new MouvementStockRequest();
            request.setTypeMouvement(MouvementStock.TypeMouvement.SORTIE);
            request.setQuantite(4);
            request.setProduitId(1L);

            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));
            given(utilisateurRepository.findByEmail(any())).willReturn(Optional.empty());
            given(mouvementStockRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // WHEN
            stockService.effectuerMouvement(request);

            // THEN
            assertThat(produit.getQuantiteStock()).isEqualTo(6);
            then(mouvementStockRepository).should(times(1)).save(any(MouvementStock.class));
        }

        @Test
        @DisplayName("❌ SORTIE stock insuffisant — doit lever BusinessException via IllegalStateException")
        void sortie_stockInsuffisant_doitLeverBusinessException() {
            // GIVEN
            MouvementStockRequest request = new MouvementStockRequest();
            request.setTypeMouvement(MouvementStock.TypeMouvement.SORTIE);
            request.setQuantite(50); // dépasse le stock de 10
            request.setProduitId(1L);

            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));

            // WHEN / THEN
            assertThatThrownBy(() -> stockService.effectuerMouvement(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("insuffisant");
        }
    }

    // =====================================================================
    // Tests Mouvement AJUSTEMENT
    // =====================================================================

    @Nested
    @DisplayName("Mouvement AJUSTEMENT")
    class AjustementTests {

        @Test
        @DisplayName("✅ AJUSTEMENT — quantiteStock = valeur exacte définie")
        void ajustement_doitDefinirQuantiteExacte() {
            // GIVEN
            MouvementStockRequest request = new MouvementStockRequest();
            request.setTypeMouvement(MouvementStock.TypeMouvement.AJUSTEMENT);
            request.setQuantite(20);
            request.setMotif("Inventaire");
            request.setProduitId(1L);

            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));
            given(utilisateurRepository.findByEmail(any())).willReturn(Optional.empty());
            given(mouvementStockRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // WHEN
            MouvementStock result = stockService.effectuerMouvement(request);

            // THEN
            assertThat(produit.getQuantiteStock()).isEqualTo(20);
            assertThat(result.getTypeMouvement()).isEqualTo(MouvementStock.TypeMouvement.AJUSTEMENT);
            then(mouvementStockRepository).should(times(1)).save(any(MouvementStock.class));
        }

        @Test
        @DisplayName("❌ AJUSTEMENT quantité négative — doit lever BusinessException")
        void ajustement_quantiteNegative_doitLeverBusinessException() {
            // GIVEN
            MouvementStockRequest request = new MouvementStockRequest();
            request.setTypeMouvement(MouvementStock.TypeMouvement.AJUSTEMENT);
            request.setQuantite(-5);
            request.setProduitId(1L);

            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));

            // WHEN / THEN
            assertThatThrownBy(() -> stockService.effectuerMouvement(request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // =====================================================================
    // Tests Produit inexistant
    // =====================================================================

    @Nested
    @DisplayName("Produit inexistant")
    class ProduitInexistantTests {

        @Test
        @DisplayName("❌ produit inexistant — doit lever ResourceNotFoundException")
        void effectuerMouvement_produitInexistant_doitLeverNotFoundException() {
            // GIVEN
            MouvementStockRequest request = new MouvementStockRequest();
            request.setTypeMouvement(MouvementStock.TypeMouvement.ENTREE);
            request.setQuantite(5);
            request.setProduitId(999L);

            given(produitRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> stockService.effectuerMouvement(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =====================================================================
    // Tests alerte de stock
    // =====================================================================

    @Nested
    @DisplayName("Alerte de stock")
    class AlerteStockTests {

        @Test
        @DisplayName("✅ ENTREE — alerte de stock déclenchée quand quantité <= seuil après décrement")
        void sortie_enRuptureAlerte_doitLoguerAlerte() {
            // GIVEN — stock = 4, seuil = 3, sortie de 1 => stock = 3 <= seuil (alerte)
            produit.setQuantiteStock(4);
            MouvementStockRequest request = new MouvementStockRequest();
            request.setTypeMouvement(MouvementStock.TypeMouvement.SORTIE);
            request.setQuantite(1);
            request.setProduitId(1L);

            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));
            given(utilisateurRepository.findByEmail(any())).willReturn(Optional.empty());
            given(mouvementStockRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // WHEN — ne doit pas lever d'exception même si en alerte
            MouvementStock result = stockService.effectuerMouvement(request);

            // THEN
            assertThat(produit.getQuantiteStock()).isEqualTo(3);
            assertThat(produit.isEnRuptureAlerte()).isTrue();
            assertThat(result).isNotNull();
        }
    }

    // =====================================================================
    // Tests listerMouvementsProduit
    // =====================================================================

    @Nested
    @DisplayName("listerMouvementsProduit()")
    class ListerMouvementsTests {

        @Test
        @DisplayName("✅ produit existant — délègue au repository et retourne la page")
        void listerMouvements_produitExistant_doitDeleguerAuRepository() {
            // GIVEN
            PageRequest pageable = PageRequest.of(0, 10);
            MouvementStock mouvement = MouvementStock.builder()
                    .id(1L).typeMouvement(MouvementStock.TypeMouvement.ENTREE)
                    .quantite(5).produit(produit).build();
            Page<MouvementStock> page = new PageImpl<>(List.of(mouvement), pageable, 1);

            given(produitRepository.existsById(1L)).willReturn(true);
            given(mouvementStockRepository.findByProduitId(1L, pageable)).willReturn(page);

            // WHEN
            Page<MouvementStock> result = stockService.listerMouvementsProduit(1L, pageable);

            // THEN
            assertThat(result.getTotalElements()).isEqualTo(1);
            then(mouvementStockRepository).should(times(1)).findByProduitId(1L, pageable);
        }

        @Test
        @DisplayName("❌ produit inexistant — doit lever ResourceNotFoundException")
        void listerMouvements_produitInexistant_doitLeverResourceNotFoundException() {
            // GIVEN
            PageRequest pageable = PageRequest.of(0, 10);
            given(produitRepository.existsById(999L)).willReturn(false);

            // WHEN / THEN
            assertThatThrownBy(() -> stockService.listerMouvementsProduit(999L, pageable))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
