package com.pme.stock.service;

import com.pme.stock.dto.request.CommandeClientRequest;
import com.pme.stock.dto.request.LigneCommandeRequest;
import com.pme.stock.dto.response.CommandeClientResponse;
import com.pme.stock.entity.*;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.CommandeClientMapper;
import com.pme.stock.repository.ClientRepository;
import com.pme.stock.repository.CommandeClientRepository;
import com.pme.stock.repository.ProduitRepository;
import com.pme.stock.repository.UtilisateurRepository;
import com.pme.stock.service.impl.CommandeClientServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommandeClientService - Tests unitaires")
class CommandeClientServiceImplTest {

    @Mock private CommandeClientRepository commandeClientRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private CommandeClientMapper commandeClientMapper;

    @InjectMocks private CommandeClientServiceImpl commandeClientService;

    private Client client;
    private Produit produit;
    private CommandeClient commandeBrouillon;
    private CommandeClient commandeConfirmee;
    private CommandeClientRequest createRequest;
    private LigneCommandeRequest ligneRequest;
    private CommandeClientResponse responseMock;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setRaisonSociale("Test Client");

        produit = Produit.builder()
                .id(1L)
                .reference("PROD-1")
                .prixVente(new BigDecimal("100.00"))
                .quantiteStock(50)
                .actif(true)
                .build();

        LigneCommandeClient ligne = new LigneCommandeClient();
        ligne.setId(1L);
        ligne.setProduit(produit);
        ligne.setQuantite(2);
        ligne.setPrixUnitaireHT(new BigDecimal("100.00"));
        ligne.calculerMontantLigneHT();

        commandeBrouillon = new CommandeClient();
        commandeBrouillon.setId(1L);
        commandeBrouillon.setClient(client);
        commandeBrouillon.setStatut(StatutCommande.BROUILLON);
        commandeBrouillon.setLignes(new ArrayList<>(List.of(ligne)));
        ligne.setCommande(commandeBrouillon);

        commandeConfirmee = new CommandeClient();
        commandeConfirmee.setId(2L);
        commandeConfirmee.setClient(client);
        commandeConfirmee.setStatut(StatutCommande.CONFIRMEE);
        LigneCommandeClient ligneConfirmee = new LigneCommandeClient();
        ligneConfirmee.setId(2L);
        ligneConfirmee.setProduit(produit);
        ligneConfirmee.setQuantite(2);
        ligneConfirmee.setPrixUnitaireHT(new BigDecimal("100.00"));
        ligneConfirmee.calculerMontantLigneHT();
        commandeConfirmee.setLignes(new ArrayList<>(List.of(ligneConfirmee)));

        ligneRequest = new LigneCommandeRequest();
        ligneRequest.setProduitId(1L);
        ligneRequest.setQuantite(2);

        createRequest = new CommandeClientRequest();
        createRequest.setClientId(1L);
        createRequest.setLignes(List.of(ligneRequest));
        
        responseMock = new CommandeClientResponse();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@pme.com", null, Collections.emptyList())
        );
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("creer()")
    class CreerTests {
        @Test
        @DisplayName("✅ succès — cas nominal")
        void creer_casNominal_doitRetournerResponse() {
            given(clientRepository.findById(1L)).willReturn(Optional.of(client));
            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));
            given(commandeClientRepository.findMaxSequenceNumber(anyString())).willReturn(0);
            given(commandeClientRepository.save(any(CommandeClient.class))).willAnswer(inv -> inv.getArgument(0));
            given(commandeClientMapper.toResponse(any(CommandeClient.class))).willReturn(responseMock);

            CommandeClientResponse result = commandeClientService.creer(createRequest);

            assertThat(result).isNotNull();
            verify(commandeClientRepository).save(any(CommandeClient.class));
        }

        @Test
        @DisplayName("❌ client inexistant — doit lever ResourceNotFoundException")
        void creer_clientInexistant_doitLeverException() {
            given(clientRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commandeClientService.creer(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client non trouvé");
        }

        @Test
        @DisplayName("❌ produit inexistant dans ligne — doit lever ResourceNotFoundException")
        void creer_produitInexistantDansLigne_doitLeverException() {
            given(clientRepository.findById(1L)).willReturn(Optional.of(client));
            given(produitRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commandeClientService.creer(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Produit non trouvé");
        }

        @Test
        @DisplayName("❌ produit inactif — doit lever BusinessException")
        void creer_produitInactif_doitLeverBusinessException() {
            produit.setActif(false);
            given(clientRepository.findById(1L)).willReturn(Optional.of(client));
            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));

            assertThatThrownBy(() -> commandeClientService.creer(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("n'est pas actif");
        }
    }

    @Nested
    @DisplayName("confirmer()")
    class ConfirmerTests {
        @Test
        @DisplayName("✅ succès — cas nominal (statut CONFIRMEE, stock décrémenté)")
        void confirmer_casNominal_doitRetournerResponse() {
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));
            given(utilisateurRepository.findByEmail(anyString())).willReturn(Optional.of(new Utilisateur()));
            given(commandeClientRepository.save(any(CommandeClient.class))).willAnswer(inv -> inv.getArgument(0));
            given(commandeClientMapper.toResponse(any(CommandeClient.class))).willReturn(responseMock);

            CommandeClientResponse result = commandeClientService.confirmer(1L);

            assertThat(result).isNotNull();
            assertThat(commandeBrouillon.getStatut()).isEqualTo(StatutCommande.CONFIRMEE);
            assertThat(produit.getQuantiteStock()).isEqualTo(48); // 50 - 2
            verify(produitRepository).save(produit);
            verify(commandeClientRepository).save(commandeBrouillon);
        }

        @Test
        @DisplayName("❌ stock insuffisant — doit lever BusinessException")
        void confirmer_stockInsuffisant_doitLeverBusinessException() {
            produit.setQuantiteStock(1);
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));

            assertThatThrownBy(() -> commandeClientService.confirmer(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Stock insuffisant");
        }

        @Test
        @DisplayName("❌ commande non brouillon — doit lever BusinessException")
        void confirmer_commandeNonBrouillon_doitLeverBusinessException() {
            commandeBrouillon.setStatut(StatutCommande.EN_PREPARATION);
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));

            assertThatThrownBy(() -> commandeClientService.confirmer(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ne peut pas être confirmée");
        }

        @Test
        @DisplayName("❌ commande sans lignes — doit lever BusinessException")
        void confirmer_commandeSansLignes_doitLeverBusinessException() {
            commandeBrouillon.setLignes(new ArrayList<>());
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));

            assertThatThrownBy(() -> commandeClientService.confirmer(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ne peut pas être confirmée");
        }
    }

    @Nested
    @DisplayName("changerStatut()")
    class ChangerStatutTests {
        @Test
        @DisplayName("✅ brouillon vers annulee — statut ANNULEE")
        void changerStatut_brouillonVersAnnulee_doitReussir() {
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));
            given(commandeClientRepository.save(any(CommandeClient.class))).willAnswer(inv -> inv.getArgument(0));
            given(commandeClientMapper.toResponse(any(CommandeClient.class))).willReturn(responseMock);

            CommandeClientResponse result = commandeClientService.changerStatut(1L, StatutCommande.ANNULEE);

            assertThat(result).isNotNull();
            assertThat(commandeBrouillon.getStatut()).isEqualTo(StatutCommande.ANNULEE);
            verify(commandeClientRepository).save(commandeBrouillon);
        }

        @Test
        @DisplayName("✅ confirmee vers annulee — stock REMIS")
        void changerStatut_confirmeeVersAnnulee_doitRemettreStock() {
            given(commandeClientRepository.findByIdWithRelations(2L)).willReturn(Optional.of(commandeConfirmee));
            given(commandeClientRepository.save(any(CommandeClient.class))).willAnswer(inv -> inv.getArgument(0));
            given(commandeClientMapper.toResponse(any(CommandeClient.class))).willReturn(responseMock);

            CommandeClientResponse result = commandeClientService.changerStatut(2L, StatutCommande.ANNULEE);

            assertThat(result).isNotNull();
            assertThat(commandeConfirmee.getStatut()).isEqualTo(StatutCommande.ANNULEE);
            assertThat(produit.getQuantiteStock()).isEqualTo(52); // 50 + 2 (stock remis)
            verify(produitRepository).save(produit);
            verify(commandeClientRepository).save(commandeConfirmee);
        }

        @Test
        @DisplayName("✅ expediee vers livree — dateLivraisonReelle = today")
        void changerStatut_expedieeVersLivree_doitMettreAJourDate() {
            CommandeClient commandeExpediee = new CommandeClient();
            commandeExpediee.setId(3L);
            commandeExpediee.setStatut(StatutCommande.EXPEDIEE);
            
            given(commandeClientRepository.findByIdWithRelations(3L)).willReturn(Optional.of(commandeExpediee));
            given(commandeClientRepository.save(any(CommandeClient.class))).willAnswer(inv -> inv.getArgument(0));
            given(commandeClientMapper.toResponse(any(CommandeClient.class))).willReturn(responseMock);

            CommandeClientResponse result = commandeClientService.changerStatut(3L, StatutCommande.LIVREE);

            assertThat(result).isNotNull();
            assertThat(commandeExpediee.getStatut()).isEqualTo(StatutCommande.LIVREE);
            assertThat(commandeExpediee.getDateLivraisonReelle()).isEqualTo(LocalDate.now());
            verify(commandeClientRepository).save(commandeExpediee);
        }

        @Test
        @DisplayName("❌ transition invalide — doit lever BusinessException")
        void changerStatut_transitionInvalide_doitLeverException() {
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));

            assertThatThrownBy(() -> commandeClientService.changerStatut(1L, StatutCommande.LIVREE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Transition invalide");
        }
    }

    @Nested
    @DisplayName("ajouterLigne()")
    class AjouterLigneTests {
        @Test
        @DisplayName("✅ succès — ligne ajoutée, montants recalculés")
        void ajouterLigne_casNominal_doitReussir() {
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));
            given(produitRepository.findById(1L)).willReturn(Optional.of(produit));
            given(commandeClientRepository.save(any(CommandeClient.class))).willAnswer(inv -> inv.getArgument(0));
            given(commandeClientMapper.toResponse(any(CommandeClient.class))).willReturn(responseMock);

            CommandeClientResponse result = commandeClientService.ajouterLigne(1L, ligneRequest);

            assertThat(result).isNotNull();
            assertThat(commandeBrouillon.getLignes()).hasSize(2);
            verify(commandeClientRepository).save(commandeBrouillon);
        }

        @Test
        @DisplayName("❌ commande non brouillon — doit lever BusinessException")
        void ajouterLigne_commandeNonBrouillon_doitLeverException() {
            commandeBrouillon.setStatut(StatutCommande.CONFIRMEE);
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));

            assertThatThrownBy(() -> commandeClientService.ajouterLigne(1L, ligneRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("doit être en statut BROUILLON");
        }
    }

    @Nested
    @DisplayName("supprimerLigne()")
    class SupprimerLigneTests {
        @Test
        @DisplayName("✅ succès — ligne retirée par id")
        void supprimerLigne_casNominal_doitReussir() {
            given(commandeClientRepository.findByIdWithRelations(1L)).willReturn(Optional.of(commandeBrouillon));
            given(commandeClientRepository.save(any(CommandeClient.class))).willAnswer(inv -> inv.getArgument(0));
            given(commandeClientMapper.toResponse(any(CommandeClient.class))).willReturn(responseMock);

            CommandeClientResponse result = commandeClientService.supprimerLigne(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(commandeBrouillon.getLignes()).isEmpty();
            verify(commandeClientRepository).save(commandeBrouillon);
        }
    }
}
