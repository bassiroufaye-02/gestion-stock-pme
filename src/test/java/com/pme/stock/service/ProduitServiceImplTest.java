package com.pme.stock.service;

import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.entity.Categorie;
import com.pme.stock.entity.Produit;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.ProduitMapper;
import com.pme.stock.repository.CategorieRepository;
import com.pme.stock.repository.ProduitRepository;
import com.pme.stock.service.impl.ProduitServiceImpl;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProduitService - Tests unitaires")
class ProduitServiceImplTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private CategorieRepository categorieRepository;

    @Mock
    private ProduitMapper produitMapper;

    @InjectMocks
    private ProduitServiceImpl produitService;

    private Produit produitExistant;
    private ProduitRequest produitRequest;
    private ProduitResponse produitResponse;
    private Categorie categorie;

    @BeforeEach
    void setUp() {
        categorie = Categorie.builder()
                .id(1L).code("INFO").libelle("Informatique").actif(true).build();

        produitExistant = Produit.builder()
                .id(1L)
                .reference("REF-001")
                .designation("Ordinateur Portable")
                .prixAchat(new BigDecimal("400.00"))
                .prixVente(new BigDecimal("650.00"))
                .quantiteStock(10)
                .seuilAlerte(3)
                .actif(true)
                .categorie(categorie)
                .build();

        produitRequest = new ProduitRequest();
        produitRequest.setReference("REF-001");
        produitRequest.setDesignation("Ordinateur Portable");
        produitRequest.setPrixAchat(new BigDecimal("400.00"));
        produitRequest.setPrixVente(new BigDecimal("650.00"));
        produitRequest.setQuantiteStock(10);
        produitRequest.setSeuilAlerte(3);
        produitRequest.setCategorieId(1L);

        produitResponse = ProduitResponse.builder()
                .id(1L).reference("REF-001").designation("Ordinateur Portable")
                .prixAchat(new BigDecimal("400.00")).prixVente(new BigDecimal("650.00"))
                .quantiteStock(10).seuilAlerte(3).actif(true)
                .enAlerte(false).enRupture(false)
                .categorieId(1L).categorieLibelle("Informatique")
                .build();
    }

    // =====================================================================
    // Tests de création
    // =====================================================================

    @Nested
    @DisplayName("creer()")
    class CreerTests {

        @Test
        @DisplayName("✅ succès — produit sauvegardé avec référence correcte, response non null")
        void creer_casNominal_doitRetournerResponse() {
            // GIVEN
            given(produitRepository.existsByReference("REF-001")).willReturn(false);
            given(categorieRepository.findById(1L)).willReturn(Optional.of(categorie));
            given(produitRepository.save(any(Produit.class))).willReturn(produitExistant);
            given(produitMapper.toResponse(produitExistant)).willReturn(produitResponse);

            // WHEN
            ProduitResponse result = produitService.creer(produitRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getReference()).isEqualTo("REF-001");
            assertThat(result.getDesignation()).isEqualTo("Ordinateur Portable");
            assertThat(result.getCategorieId()).isEqualTo(1L);
            then(produitRepository).should(times(1)).save(any(Produit.class));
        }

        @Test
        @DisplayName("❌ doublon — référence déjà existante doit lever BusinessException")
        void creer_referenceDoublonne_doitLeverBusinessException() {
            // GIVEN
            given(produitRepository.existsByReference("REF-001")).willReturn(true);

            // WHEN / THEN
            assertThatThrownBy(() -> produitService.creer(produitRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("REF-001");
            then(produitRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("✅ succès avec catégorie — categorieRepository.findById appelé")
        void creer_avecCategorie_doitAppelerCategorieRepository() {
            // GIVEN
            given(produitRepository.existsByReference(anyString())).willReturn(false);
            given(categorieRepository.findById(1L)).willReturn(Optional.of(categorie));
            given(produitRepository.save(any(Produit.class))).willReturn(produitExistant);
            given(produitMapper.toResponse(any(Produit.class))).willReturn(produitResponse);

            // WHEN
            produitService.creer(produitRequest);

            // THEN
            then(categorieRepository).should(times(1)).findById(1L);
        }

        @Test
        @DisplayName("❌ catégorie inexistante — doit lever ResourceNotFoundException")
        void creer_categorieInexistante_doitLeverResourceNotFoundException() {
            // GIVEN
            given(produitRepository.existsByReference(anyString())).willReturn(false);
            produitRequest.setCategorieId(99L);
            given(categorieRepository.findById(99L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> produitService.creer(produitRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
            then(produitRepository).should(never()).save(any());
        }
    }

    // =====================================================================
    // Tests de modification
    // =====================================================================

    @Nested
    @DisplayName("modifier()")
    class ModifierTests {

        @Test
        @DisplayName("✅ succès — modification avec même référence réussit")
        void modifier_casNominal_doitReussir() {
            // GIVEN
            given(produitRepository.findById(1L)).willReturn(Optional.of(produitExistant));
            given(categorieRepository.findById(1L)).willReturn(Optional.of(categorie));
            given(produitRepository.save(any(Produit.class))).willReturn(produitExistant);
            given(produitMapper.toResponse(any(Produit.class))).willReturn(produitResponse);

            // WHEN
            ProduitResponse result = produitService.modifier(1L, produitRequest);

            // THEN
            assertThat(result).isNotNull();
            then(produitRepository).should(times(1)).save(any(Produit.class));
        }

        @Test
        @DisplayName("❌ nouvelle référence déjà utilisée — doit lever BusinessException")
        void modifier_referenceChangeeDupliquee_doitLeverBusinessException() {
            // GIVEN
            produitRequest.setReference("REF-002"); // Référence différente de produitExistant
            given(produitRepository.findById(1L)).willReturn(Optional.of(produitExistant));
            given(produitRepository.existsByReference("REF-002")).willReturn(true);

            // WHEN / THEN
            assertThatThrownBy(() -> produitService.modifier(1L, produitRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("REF-002");
            then(produitRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void modifier_idInexistant_doitLeverResourceNotFoundException() {
            // GIVEN
            given(produitRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> produitService.modifier(999L, produitRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =====================================================================
    // Tests de récupération
    // =====================================================================

    @Nested
    @DisplayName("trouverParId()")
    class TrouverParIdTests {

        @Test
        @DisplayName("✅ id existant — retourne la response correcte")
        void trouverParId_existant_doitRetournerResponse() {
            // GIVEN
            given(produitRepository.findById(1L)).willReturn(Optional.of(produitExistant));
            given(produitMapper.toResponse(produitExistant)).willReturn(produitResponse);

            // WHEN
            ProduitResponse result = produitService.trouverParId(1L);

            // THEN
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getReference()).isEqualTo("REF-001");
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void trouverParId_idInconnu_doitLeverNotFoundException() {
            // GIVEN
            given(produitRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> produitService.trouverParId(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("trouverParReference()")
    class TrouverParReferenceTests {

        @Test
        @DisplayName("✅ référence existante — retourne la response correcte")
        void trouverParReference_existant_doitRetournerResponse() {
            // GIVEN
            given(produitRepository.findByReference("REF-001")).willReturn(Optional.of(produitExistant));
            given(produitMapper.toResponse(produitExistant)).willReturn(produitResponse);

            // WHEN
            ProduitResponse result = produitService.trouverParReference("REF-001");

            // THEN
            assertThat(result.getReference()).isEqualTo("REF-001");
        }

        @Test
        @DisplayName("❌ référence inexistante — doit lever ResourceNotFoundException")
        void trouverParReference_inexistant_doitLeverNotFoundException() {
            // GIVEN
            given(produitRepository.findByReference("INCONNU")).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> produitService.trouverParReference("INCONNU"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =====================================================================
    // Tests des alertes de stock
    // =====================================================================

    @Nested
    @DisplayName("listerProduitsEnAlerte()")
    class AlertesStockTests {

        @Test
        @DisplayName("✅ appelle findProduitsEnAlerte() et retourne les produits en alerte")
        void listerProduitsEnAlerte_doitAppelerFindProduitsEnAlerte() {
            // GIVEN
            Produit produitEnAlerte = Produit.builder()
                    .id(2L).reference("REF-002").designation("Souris")
                    .prixAchat(BigDecimal.TEN).prixVente(BigDecimal.TEN)
                    .quantiteStock(2).seuilAlerte(3).actif(true).build();

            ProduitResponse alerteResponse = ProduitResponse.builder()
                    .id(2L).reference("REF-002").enAlerte(true).enRupture(false).build();

            given(produitRepository.findProduitsEnAlerte()).willReturn(List.of(produitEnAlerte));
            given(produitMapper.toResponse(produitEnAlerte)).willReturn(alerteResponse);

            // WHEN
            List<ProduitResponse> alertes = produitService.listerProduitsEnAlerte();

            // THEN
            assertThat(alertes).hasSize(1);
            assertThat(alertes.get(0).getEnAlerte()).isTrue();
            then(produitRepository).should(times(1)).findProduitsEnAlerte();
        }
    }

    // =====================================================================
    // Tests de désactivation
    // =====================================================================

    @Nested
    @DisplayName("desactiver()")
    class DesactiverTests {

        @Test
        @DisplayName("✅ existant — actif = false puis sauvegardé")
        void desactiver_existant_doitSetActifFalse() {
            // GIVEN
            given(produitRepository.findById(1L)).willReturn(Optional.of(produitExistant));
            given(produitRepository.save(any(Produit.class))).willReturn(produitExistant);

            // WHEN
            produitService.desactiver(1L);

            // THEN
            assertThat(produitExistant.getActif()).isFalse();
            then(produitRepository).should(times(1)).save(argThat(p -> !p.getActif()));
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void desactiver_produitInexistant_doitLeverNotFoundException() {
            // GIVEN
            given(produitRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> produitService.desactiver(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =====================================================================
    // Tests de pagination
    // =====================================================================

    @Nested
    @DisplayName("listerTous()")
    class PaginationTests {

        @Test
        @DisplayName("✅ retourne une page de produits correctement mappée")
        void listerTous_doitRetournerPage() {
            // GIVEN
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Produit> page = new PageImpl<>(List.of(produitExistant), pageable, 1);
            given(produitRepository.findAll(pageable)).willReturn(page);
            given(produitMapper.toResponse(produitExistant)).willReturn(produitResponse);

            // WHEN
            Page<ProduitResponse> result = produitService.listerTous(pageable);

            // THEN
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getReference()).isEqualTo("REF-001");
        }
    }
}
