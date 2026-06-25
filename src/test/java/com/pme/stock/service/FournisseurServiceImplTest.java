package com.pme.stock.service;

import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.response.FournisseurResponse;
import com.pme.stock.entity.Fournisseur;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.FournisseurMapper;
import com.pme.stock.repository.FournisseurRepository;
import com.pme.stock.service.impl.FournisseurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FournisseurService - Tests unitaires")
class FournisseurServiceImplTest {

    @Mock
    private FournisseurRepository fournisseurRepository;

    @Mock
    private FournisseurMapper fournisseurMapper;

    @InjectMocks
    private FournisseurServiceImpl fournisseurService;

    private Fournisseur fournisseur;
    private FournisseurRequest request;

    @BeforeEach
    void setUp() {
        fournisseur = Fournisseur.builder()
                .id(1L)
                .code("FOUR-001")
                .raisonSociale("Tech Supplies")
                .email("contact@techsupplies.sn")
                .actif(true)
                .build();

        request = new FournisseurRequest();
        request.setCode("four-001");
        request.setRaisonSociale("Tech Supplies");
        request.setEmail("contact@techsupplies.sn");
        request.setVille("Dakar");
        request.setPays("Sénégal");

        given(fournisseurMapper.toEntity(any(FournisseurRequest.class))).willAnswer(inv -> {
            FournisseurRequest req = inv.getArgument(0);
            return Fournisseur.builder()
                    .code(req.getCode())
                    .raisonSociale(req.getRaisonSociale())
                    .email(req.getEmail())
                    .ville(req.getVille())
                    .pays(req.getPays())
                    .build();
        });
        given(fournisseurMapper.toResponse(any(Fournisseur.class))).willAnswer(inv -> {
            Fournisseur f = inv.getArgument(0);
            return FournisseurResponse.builder()
                    .id(f.getId())
                    .code(f.getCode())
                    .raisonSociale(f.getRaisonSociale())
                    .email(f.getEmail())
                    .actif(f.getActif())
                    .build();
        });
    }

    @Nested
    @DisplayName("creer()")
    class CreerTests {

        @Test
        @DisplayName("✅ creer_casNominal — code MAJUSCULE, save appelé")
        void creer_casNominal() {
            given(fournisseurRepository.existsByCode("FOUR-001")).willReturn(false);
            given(fournisseurRepository.existsByEmail("contact@techsupplies.sn")).willReturn(false);
            given(fournisseurRepository.save(any(Fournisseur.class))).willReturn(fournisseur);

            FournisseurResponse response = fournisseurService.creer(request);

            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo("FOUR-001");
            then(fournisseurRepository).should(times(1)).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ creer_codeDoublon — BusinessException contient code")
        void creer_codeDoublon() {
            given(fournisseurRepository.existsByCode("FOUR-001")).willReturn(true);

            assertThatThrownBy(() -> fournisseurService.creer(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("code");
        }

        @Test
        @DisplayName("❌ creer_emailDoublon — BusinessException contient email")
        void creer_emailDoublon() {
            given(fournisseurRepository.existsByCode("FOUR-001")).willReturn(false);
            given(fournisseurRepository.existsByEmail("contact@techsupplies.sn")).willReturn(true);

            assertThatThrownBy(() -> fournisseurService.creer(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("✅ creer_sansEmail — existsByEmail jamais appelé")
        void creer_sansEmail() {
            request.setEmail(null);
            given(fournisseurRepository.existsByCode("FOUR-001")).willReturn(false);
            given(fournisseurRepository.save(any(Fournisseur.class))).willReturn(fournisseur);

            fournisseurService.creer(request);

            then(fournisseurRepository).should(never()).existsByEmail(any());
        }
    }

    @Nested
    @DisplayName("modifier()")
    class ModifierTests {

        @Test
        @DisplayName("✅ modifier_casNominal — save appelé, response ok")
        void modifier_casNominal() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(fournisseurRepository.existsByCodeAndIdNot("FOUR-001", 1L)).willReturn(false);
            given(fournisseurRepository.existsByEmailAndIdNot("contact@techsupplies.sn", 1L)).willReturn(false);
            given(fournisseurRepository.save(any(Fournisseur.class))).willReturn(fournisseur);

            FournisseurResponse response = fournisseurService.modifier(1L, request);

            assertThat(response).isNotNull();
            then(fournisseurRepository).should(times(1)).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ modifier_codeDoublonSurAutre — BusinessException")
        void modifier_codeDoublonSurAutre() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(fournisseurRepository.existsByCodeAndIdNot("FOUR-001", 1L)).willReturn(true);

            assertThatThrownBy(() -> fournisseurService.modifier(1L, request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("❌ modifier_emailDoublonSurAutre — BusinessException")
        void modifier_emailDoublonSurAutre() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(fournisseurRepository.existsByCodeAndIdNot("FOUR-001", 1L)).willReturn(false);
            given(fournisseurRepository.existsByEmailAndIdNot("contact@techsupplies.sn", 1L)).willReturn(true);

            assertThatThrownBy(() -> fournisseurService.modifier(1L, request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("✅ modifier_memeCodeMemeEmail — save sans exception")
        void modifier_memeCodeMemeEmail() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(fournisseurRepository.existsByCodeAndIdNot("FOUR-001", 1L)).willReturn(false);
            given(fournisseurRepository.existsByEmailAndIdNot("contact@techsupplies.sn", 1L)).willReturn(false);
            given(fournisseurRepository.save(any(Fournisseur.class))).willReturn(fournisseur);

            fournisseurService.modifier(1L, request);

            then(fournisseurRepository).should(times(1)).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ modifier_idInexistant — ResourceNotFoundException")
        void modifier_idInexistant() {
            given(fournisseurRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fournisseurService.modifier(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("trouverParId()")
    class TrouverTests {

        @Test
        @DisplayName("✅ trouverParId_existant — response avec bon id")
        void trouverParId_existant() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));

            FournisseurResponse response = fournisseurService.trouverParId(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("desactiver()")
    class DesactiverTests {

        @Test
        @DisplayName("❌ desactiver_avecCommandesEnCours — BusinessException")
        void desactiver_avecCommandesEnCours() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(fournisseurRepository.hasCommandesEnCours(1L)).willReturn(true);

            assertThatThrownBy(() -> fournisseurService.desactiver(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Impossible de désactiver : des commandes sont en cours");
        }

        @Test
        @DisplayName("✅ desactiver_sansCommandes — actif=false, save appelé")
        void desactiver_sansCommandes() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(fournisseurRepository.hasCommandesEnCours(1L)).willReturn(false);

            fournisseurService.desactiver(1L);

            assertThat(fournisseur.getActif()).isFalse();
            then(fournisseurRepository).should(times(1)).save(fournisseur);
        }
    }

    @Nested
    @DisplayName("rechercherTous()")
    class RechercherTousTests {

        @Test
        @DisplayName("✅ rechercherTous_casNominal — retourne tous les fournisseurs actifs et inactifs")
        void rechercherTous_casNominal() {
            Fournisseur fournisseurInactif = Fournisseur.builder()
                    .id(2L)
                    .code("FOUR-002")
                    .raisonSociale("Inactive Supplies")
                    .actif(false)
                    .build();

            Page<Fournisseur> page = new PageImpl<>(List.of(fournisseur, fournisseurInactif));
            given(fournisseurRepository.rechercherIncluantInactifs("Tech", PageRequest.of(0, 20)))
                    .willReturn(page);

            Page<FournisseurResponse> result = fournisseurService.rechercherTous("Tech", PageRequest.of(0, 20));

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            then(fournisseurRepository).should(times(1)).rechercherIncluantInactifs("Tech", PageRequest.of(0, 20));
        }
    }

    @Nested
    @DisplayName("reactiver()")
    class ReactiverTests {

        @Test
        @DisplayName("✅ reactiver_casNominal — actif=true, save appelé")
        void reactiver_casNominal() {
            fournisseur.setActif(false);
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(fournisseurRepository.save(any(Fournisseur.class))).willReturn(fournisseur);

            FournisseurResponse response = fournisseurService.reactiver(1L);

            assertThat(response).isNotNull();
            assertThat(response.getActif()).isTrue();
            then(fournisseurRepository).should(times(1)).save(fournisseur);
        }

        @Test
        @DisplayName("✅ reactiver_dejaActif — no-op silencieux, retourne response")
        void reactiver_dejaActif() {
            fournisseur.setActif(true);
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(fournisseurRepository.save(any(Fournisseur.class))).willReturn(fournisseur);

            FournisseurResponse response = fournisseurService.reactiver(1L);

            assertThat(response).isNotNull();
            assertThat(response.getActif()).isTrue();
            then(fournisseurRepository).should(times(1)).save(fournisseur);
        }

        @Test
        @DisplayName("❌ reactiver_idInexistant — ResourceNotFoundException")
        void reactiver_idInexistant() {
            given(fournisseurRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fournisseurService.reactiver(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
