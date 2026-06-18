package com.pme.stock.service;

import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.entity.Categorie;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.CategorieMapper;
import com.pme.stock.repository.CategorieRepository;
import com.pme.stock.service.impl.CategorieService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategorieService - Tests unitaires")
class CategorieServiceTest {

    @Mock
    private CategorieRepository categorieRepository;

    @Mock
    private CategorieMapper categorieMapper;

    @InjectMocks
    private CategorieService categorieService;

    private Categorie categorieInfo;
    private CategorieRequest request;
    private CategorieResponse categorieResponse;

    @BeforeEach
    void setUp() {
        categorieInfo = Categorie.builder()
                .id(1L).code("INFO").libelle("Informatique")
                .description("Matériel informatique").actif(true).build();

        request = new CategorieRequest();
        request.setCode("INFO");
        request.setLibelle("Informatique");
        request.setDescription("Matériel informatique");

        categorieResponse = CategorieResponse.builder()
                .id(1L).code("INFO").libelle("Informatique")
                .description("Matériel informatique").actif(true).nombreProduits(0)
                .build();
    }

    // =====================================================================
    // Tests de création
    // =====================================================================

    @Nested
    @DisplayName("creer()")
    class CreerTests {

        @Test
        @DisplayName("✅ succès — cas nominal : code sauvegardé en MAJUSCULES, response non null")
        void creer_casNominal_doitRetournerResponse() {
            // GIVEN
            given(categorieRepository.existsByCode("INFO")).willReturn(false);
            given(categorieRepository.save(any(Categorie.class))).willReturn(categorieInfo);
            given(categorieMapper.toResponse(categorieInfo)).willReturn(categorieResponse);

            // WHEN
            CategorieResponse response = categorieService.creer(request);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo("INFO");
            assertThat(response.getLibelle()).isEqualTo("Informatique");
            then(categorieRepository).should(times(1)).save(argThat(c -> "INFO".equals(c.getCode())));
            then(categorieMapper).should(times(1)).toResponse(categorieInfo);
        }

        @Test
        @DisplayName("✅ succès — code en minuscules est converti en MAJUSCULES avant sauvegarde")
        void creer_codeMinuscules_doitConvertirEnMajuscules() {
            // GIVEN
            request.setCode("info");
            given(categorieRepository.existsByCode("info")).willReturn(false);
            given(categorieRepository.save(argThat(c -> "INFO".equals(c.getCode())))).willReturn(categorieInfo);
            given(categorieMapper.toResponse(any(Categorie.class))).willReturn(categorieResponse);

            // WHEN
            categorieService.creer(request);

            // THEN
            then(categorieRepository).should(times(1)).save(argThat(c -> "INFO".equals(c.getCode())));
        }

        @Test
        @DisplayName("❌ doublon — code déjà existant doit lever BusinessException")
        void creer_codeDoublon_doitLeverBusinessException() {
            // GIVEN
            given(categorieRepository.existsByCode("INFO")).willReturn(true);

            // WHEN / THEN
            assertThatThrownBy(() -> categorieService.creer(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("INFO");
            then(categorieRepository).should(never()).save(any());
        }
    }

    // =====================================================================
    // Tests de modification
    // =====================================================================

    @Nested
    @DisplayName("modifier()")
    class ModifierTests {

        @Test
        @DisplayName("✅ succès — cas nominal : libelle et description mis à jour")
        void modifier_casNominal_doitMettreAJour() {
            // GIVEN
            CategorieRequest updateRequest = new CategorieRequest();
            updateRequest.setCode("INFO");
            updateRequest.setLibelle("Informatique & Réseaux");
            updateRequest.setDescription("Mise à jour");

            CategorieResponse updatedResponse = CategorieResponse.builder()
                    .id(1L).code("INFO").libelle("Informatique & Réseaux")
                    .description("Mise à jour").actif(true).nombreProduits(0).build();

            given(categorieRepository.findById(1L)).willReturn(Optional.of(categorieInfo));
            given(categorieRepository.save(any(Categorie.class))).willAnswer(inv -> inv.getArgument(0));
            given(categorieMapper.toResponse(any(Categorie.class))).willReturn(updatedResponse);

            // WHEN
            CategorieResponse response = categorieService.modifier(1L, updateRequest);

            // THEN
            assertThat(response.getLibelle()).isEqualTo("Informatique & Réseaux");
            assertThat(response.getDescription()).isEqualTo("Mise à jour");
            then(categorieRepository).should(times(1)).save(any(Categorie.class));
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void modifier_idInexistant_doitLeverResourceNotFoundException() {
            // GIVEN
            given(categorieRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> categorieService.modifier(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            then(categorieRepository).should(never()).save(any());
        }
    }

    // =====================================================================
    // Tests de recherche
    // =====================================================================

    @Nested
    @DisplayName("trouverParId()")
    class TrouverParIdTests {

        @Test
        @DisplayName("✅ id existant — retourne la response avec le bon id")
        void trouverParId_existant_doitRetournerResponse() {
            // GIVEN
            given(categorieRepository.findById(1L)).willReturn(Optional.of(categorieInfo));
            given(categorieMapper.toResponse(categorieInfo)).willReturn(categorieResponse);

            // WHEN
            CategorieResponse response = categorieService.trouverParId(1L);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getCode()).isEqualTo("INFO");
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void trouverParId_inexistant_doitLeverResourceNotFoundException() {
            // GIVEN
            given(categorieRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> categorieService.trouverParId(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =====================================================================
    // Tests de listage
    // =====================================================================

    @Nested
    @DisplayName("listerActives()")
    class ListerActivesTests {

        @Test
        @DisplayName("✅ listerActives — appelle findAllByActifTrue() et retourne la liste mappée")
        void listerActives_doitAppelerFindAllByActifTrue() {
            // GIVEN
            given(categorieRepository.findAllByActifTrue()).willReturn(List.of(categorieInfo));
            given(categorieMapper.toResponseList(List.of(categorieInfo))).willReturn(List.of(categorieResponse));

            // WHEN
            List<CategorieResponse> result = categorieService.listerActives();

            // THEN
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCode()).isEqualTo("INFO");
            then(categorieRepository).should(times(1)).findAllByActifTrue();
        }

        @Test
        @DisplayName("✅ listerToutes — appelle findAll() et retourne toutes les catégories")
        void listerToutes_doitAppelerFindAll() {
            // GIVEN
            Categorie inactive = Categorie.builder().id(2L).code("OLD")
                    .libelle("Ancienne").actif(false).build();
            CategorieResponse inactiveResponse = CategorieResponse.builder()
                    .id(2L).code("OLD").libelle("Ancienne").actif(false).nombreProduits(0).build();

            given(categorieRepository.findAll()).willReturn(List.of(categorieInfo, inactive));
            given(categorieMapper.toResponseList(anyList())).willReturn(List.of(categorieResponse, inactiveResponse));

            // WHEN
            List<CategorieResponse> result = categorieService.listerToutes();

            // THEN
            assertThat(result).hasSize(2);
            then(categorieRepository).should(times(1)).findAll();
        }
    }

    // =====================================================================
    // Tests de désactivation
    // =====================================================================

    @Nested
    @DisplayName("desactiver()")
    class DesactiverTests {

        @Test
        @DisplayName("✅ existant — setActif(false) appelé sur l'entité puis sauvegardé")
        void desactiver_existant_doitSetActifFalse() {
            // GIVEN
            given(categorieRepository.findById(1L)).willReturn(Optional.of(categorieInfo));

            // WHEN
            categorieService.desactiver(1L);

            // THEN
            assertThat(categorieInfo.getActif()).isFalse();
            then(categorieRepository).should(times(1)).save(argThat(c -> !c.getActif()));
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void desactiver_idInexistant_doitLeverException() {
            // GIVEN
            given(categorieRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> categorieService.desactiver(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
            then(categorieRepository).should(never()).save(any());
        }
    }
}
