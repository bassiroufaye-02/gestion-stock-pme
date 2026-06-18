package com.pme.stock.service;

import com.pme.stock.dto.request.CommandeFournisseurRequest;
import com.pme.stock.dto.request.LigneCommandeFournisseurRequest;
import com.pme.stock.dto.response.CommandeFournisseurResponse;
import com.pme.stock.entity.*;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.CommandeFournisseurMapper;
import com.pme.stock.repository.*;
import com.pme.stock.service.impl.CommandeFournisseurServiceImpl;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CommandeFournisseurService - Tests unitaires")
class CommandeFournisseurServiceImplTest {

    @Mock
    private CommandeFournisseurRepository commandeFournisseurRepository;
    @Mock
    private FournisseurRepository fournisseurRepository;
    @Mock
    private ProduitRepository produitRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private MouvementStockRepository mouvementStockRepository;
    @Mock
    private CommandeFournisseurMapper commandeFournisseurMapper;

    @InjectMocks
    private CommandeFournisseurServiceImpl commandeFournisseurService;

    private Fournisseur fournisseur;
    private Produit produit;
    private CommandeFournisseur commande;
    private CommandeFournisseurRequest request;

    @BeforeEach
    void setUp() {
        fournisseur = Fournisseur.builder()
                .id(1L)
                .code("FOUR-001")
                .raisonSociale("Tech Supplies")
                .actif(true)
                .build();

        produit = Produit.builder()
                .id(1L)
                .reference("PROD-001")
                .designation("Ordinateur")
                .quantiteStock(10)
                .seuilAlerte(5)
                .actif(true)
                .build();

        LigneCommandeFournisseur ligne = LigneCommandeFournisseur.builder()
                .id(1L)
                .quantiteCommandee(5)
                .quantiteRecue(0)
                .prixUnitaireAchat(BigDecimal.valueOf(1000))
                .produit(produit)
                .build();

        commande = CommandeFournisseur.builder()
                .id(1L)
                .numeroCommande("CF-" + LocalDate.now().getYear() + "-00001")
                .dateCommande(LocalDate.now())
                .statut(StatutCommandeFournisseur.BROUILLON)
                .tauxTVA(new BigDecimal("18.00"))
                .fournisseur(fournisseur)
                .lignes(new ArrayList<>(List.of(ligne)))
                .build();
        ligne.setCommande(commande);
        commande.calculerMontants();

        LigneCommandeFournisseurRequest lineReq = new LigneCommandeFournisseurRequest();
        lineReq.setProduitId(1L);
        lineReq.setQuantiteCommandee(5);
        lineReq.setPrixUnitaireAchat(BigDecimal.valueOf(1000));

        request = new CommandeFournisseurRequest();
        request.setFournisseurId(1L);
        request.setTauxTVA(new BigDecimal("18.00"));
        request.setLignes(List.of(lineReq));

        given(commandeFournisseurMapper.toResponse(any(CommandeFournisseur.class))).willAnswer(inv -> {
            CommandeFournisseur c = inv.getArgument(0);
            return CommandeFournisseurResponse.builder()
                    .id(c.getId())
                    .numeroCommande(c.getNumeroCommande())
                    .statut(c.getStatut())
                    .montantHT(c.getMontantHT())
                    .montantTVA(c.getMontantTVA())
                    .montantTTC(c.getMontantTTC())
                    .dateReception(c.getDateReception())
                    .build();
        });

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        org.mockito.Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        org.mockito.Mockito.lenient().when(authentication.getName()).thenReturn("admin@pme.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("creer()")
    class CreerTests {

        @Test
        @DisplayName("✅ creer_casNominal — numéro CF-YYYY-00001, montants calculés")
        void creer_casNominal() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));
            given(utilisateurRepository.findByEmail("admin@pme.com")).willReturn(Optional.empty());
            given(commandeFournisseurRepository.findMaxSequenceNumber(any())).willReturn(null);
            given(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).willAnswer(inv -> {
                CommandeFournisseur saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            CommandeFournisseurResponse response = commandeFournisseurService.creer(request);

            assertThat(response).isNotNull();
            assertThat(response.getNumeroCommande()).isEqualTo("CF-" + LocalDate.now().getYear() + "-00001");
            assertThat(response.getMontantHT()).isEqualByComparingTo(new BigDecimal("5000.00"));
            then(commandeFournisseurRepository).should(times(1)).save(any(CommandeFournisseur.class));
        }

        @Test
        @DisplayName("❌ creer_fournisseurInexistant — ResourceNotFoundException")
        void creer_fournisseurInexistant() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commandeFournisseurService.creer(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("❌ creer_fournisseurInactif — BusinessException")
        void creer_fournisseurInactif() {
            fournisseur.setActif(false);
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));

            assertThatThrownBy(() -> commandeFournisseurService.creer(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("n'est pas actif");
        }

        @Test
        @DisplayName("❌ creer_produitInexistant — ResourceNotFoundException")
        void creer_produitInexistant() {
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(produitRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commandeFournisseurService.creer(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("❌ creer_produitInactif — BusinessException")
        void creer_produitInactif() {
            produit.setActif(false);
            given(fournisseurRepository.findById(1L)).willReturn(Optional.of(fournisseur));
            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));

            assertThatThrownBy(() -> commandeFournisseurService.creer(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("n'est pas actif");
        }
    }

    @Nested
    @DisplayName("envoyer()")
    class EnvoyerTests {

        @Test
        @DisplayName("✅ envoyer_casNominal — statut ENVOYEE")
        void envoyer_casNominal() {
            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commande));
            given(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).willReturn(commande);

            CommandeFournisseurResponse response = commandeFournisseurService.envoyer(1L);

            assertThat(response.getStatut()).isEqualTo(StatutCommandeFournisseur.ENVOYEE);
            then(commandeFournisseurRepository).should(times(1)).save(commande);
        }

        @Test
        @DisplayName("❌ envoyer_commandeSansBrouillon — BusinessException")
        void envoyer_commandeSansBrouillon() {
            commande.setStatut(StatutCommandeFournisseur.ENVOYEE);
            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commande));

            assertThatThrownBy(() -> commandeFournisseurService.envoyer(1L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("receptionner()")
    class ReceptionnerTests {

        @Test
        @DisplayName("✅ réception totale — statut RECUE, stock incrémenté, mouvement ENTREE créé")
        void receptionner_total() {
            Produit produitRec = Produit.builder()
                    .id(1L).reference("PROD-001").quantiteStock(10).seuilAlerte(5).actif(true).build();
            LigneCommandeFournisseur ligne = LigneCommandeFournisseur.builder()
                    .quantiteCommandee(20).quantiteRecue(0)
                    .prixUnitaireAchat(new BigDecimal("1000")).produit(produitRec).build();
            CommandeFournisseur cmd = CommandeFournisseur.builder()
                    .id(1L).numeroCommande("CF-" + LocalDate.now().getYear() + "-00001")
                    .statut(StatutCommandeFournisseur.ENVOYEE)
                    .lignes(new ArrayList<>(List.of(ligne))).build();
            ligne.setCommande(cmd);

            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(cmd));
            given(utilisateurRepository.findByEmail("admin@pme.com")).willReturn(Optional.empty());
            given(produitRepository.save(any())).willReturn(produitRec);
            given(commandeFournisseurRepository.save(any())).willReturn(cmd);

            commandeFournisseurService.receptionner(1L);

            assertThat(produitRec.getQuantiteStock()).isEqualTo(30);
            assertThat(cmd.getStatut()).isEqualTo(StatutCommandeFournisseur.RECUE);
            assertThat(cmd.getDateReception()).isEqualTo(LocalDate.now());
            verify(produitRepository, times(1)).save(produitRec);
            verify(mouvementStockRepository, times(1)).save(argThat(m ->
                    m.getTypeMouvement() == MouvementStock.TypeMouvement.ENTREE && m.getQuantite() == 20
            ));
        }

        @Test
        @DisplayName("✅ réception partielle — statut RECUE_PARTIELLE")
        void receptionner_partiel() {
            Produit produit1 = Produit.builder().id(1L).reference("P1").quantiteStock(0).actif(true).build();
            Produit produit2 = Produit.builder().id(2L).reference("P2").quantiteStock(0).actif(true).build();
            LigneCommandeFournisseur ligne1 = LigneCommandeFournisseur.builder()
                    .id(1L).quantiteCommandee(10).quantiteRecue(10).prixUnitaireAchat(BigDecimal.TEN).produit(produit1).build();
            LigneCommandeFournisseur ligne2 = LigneCommandeFournisseur.builder()
                    .id(2L).quantiteCommandee(10).quantiteRecue(5).prixUnitaireAchat(BigDecimal.TEN).produit(produit2).build();
            CommandeFournisseur cmd = CommandeFournisseur.builder()
                    .id(1L).numeroCommande("CF-" + LocalDate.now().getYear() + "-00002")
                    .statut(StatutCommandeFournisseur.RECUE_PARTIELLE)
                    .lignes(new ArrayList<>(List.of(ligne1, ligne2))).build();
            ligne1.setCommande(cmd);
            ligne2.setCommande(cmd);

            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(cmd));
            given(produitRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(commandeFournisseurRepository.save(any())).willReturn(cmd);

            commandeFournisseurService.receptionner(1L);

            assertThat(produit2.getQuantiteStock()).isEqualTo(5);
            assertThat(ligne2.getQuantiteRecue()).isEqualTo(10);
            assertThat(cmd.getStatut()).isEqualTo(StatutCommandeFournisseur.RECUE);
        }

        @Test
        @DisplayName("❌ receptionner_commandeNonEnvoyee — BusinessException")
        void receptionner_commandeNonEnvoyee() {
            commande.setStatut(StatutCommandeFournisseur.BROUILLON);
            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commande));

            assertThatThrownBy(() -> commandeFournisseurService.receptionner(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("n'est pas dans un état réceptionnable");
        }
    }

    @Nested
    @DisplayName("annuler()")
    class AnnulerTests {

        @Test
        @DisplayName("✅ annuler_brouillon — statut ANNULEE")
        void annuler_brouillon() {
            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commande));
            given(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).willReturn(commande);

            CommandeFournisseurResponse response = commandeFournisseurService.annuler(1L);

            assertThat(response.getStatut()).isEqualTo(StatutCommandeFournisseur.ANNULEE);
        }

        @Test
        @DisplayName("❌ annuler_commandeDejaRecue — BusinessException")
        void annuler_commandeDejaRecue() {
            commande.setStatut(StatutCommandeFournisseur.RECUE);
            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commande));

            assertThatThrownBy(() -> commandeFournisseurService.annuler(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ne peut plus être annulée");
        }
    }

    @Nested
    @DisplayName("ajouterLigne() / supprimerLigne()")
    class LignesTests {

        @Test
        @DisplayName("✅ ajouterLigne_casNominal — ligne ajoutée, montants recalculés")
        void ajouterLigne_casNominal() {
            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commande));
            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));
            given(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).willReturn(commande);

            LigneCommandeFournisseurRequest lineReq = new LigneCommandeFournisseurRequest();
            lineReq.setProduitId(1L);
            lineReq.setQuantiteCommandee(3);
            lineReq.setPrixUnitaireAchat(BigDecimal.valueOf(500));

            commandeFournisseurService.ajouterLigne(1L, lineReq);

            assertThat(commande.getLignes()).hasSize(2);
            then(commandeFournisseurRepository).should(times(1)).save(commande);
        }

        @Test
        @DisplayName("❌ supprimerLigne_commandeNonBrouillon — BusinessException BROUILLON")
        void supprimerLigne_commandeNonBrouillon() {
            commande.setStatut(StatutCommandeFournisseur.ENVOYEE);
            given(commandeFournisseurRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commande));

            assertThatThrownBy(() -> commandeFournisseurService.supprimerLigne(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("BROUILLON");
        }
    }
}
