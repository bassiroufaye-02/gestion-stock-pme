package com.pme.stock.service;

import com.pme.stock.dto.response.ChiffreAffairesMoisResponse;
import com.pme.stock.dto.response.CommandeStatutStatResponse;
import com.pme.stock.dto.response.DashboardResponse;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.dto.response.ProduitTopVenteResponse;
import com.pme.stock.dto.response.StockValeurResponse;
import com.pme.stock.entity.Produit;
import com.pme.stock.entity.StatutCommande;
import com.pme.stock.entity.StatutCommandeFournisseur;
import com.pme.stock.mapper.ProduitMapper;
import com.pme.stock.repository.CommandeClientRepository;
import com.pme.stock.repository.CommandeFournisseurRepository;
import com.pme.stock.repository.FournisseurRepository;
import com.pme.stock.repository.LigneCommandeClientRepository;
import com.pme.stock.repository.ProduitRepository;
import com.pme.stock.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardServiceImpl - Tests unitaires")
class DashboardServiceImplTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private CommandeClientRepository commandeClientRepository;

    @Mock
    private LigneCommandeClientRepository ligneCommandeClientRepository;

    @Mock
    private ProduitMapper produitMapper;

    @Mock
    private FournisseurRepository fournisseurRepository;

    @Mock
    private CommandeFournisseurRepository commandeFournisseurRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Nested
    @DisplayName("calculerValeurStock()")
    class CalculerValeurStockTests {

        @Test
        @DisplayName("✅ calcule correctement la marge potentielle")
        void calculerValeurStock_casNominal_doitCalculerMargeCorrectement() {
            given(produitRepository.sumValeurStockAchat()).willReturn(new BigDecimal("4500000"));
            given(produitRepository.sumValeurStockVente()).willReturn(new BigDecimal("6200000"));
            given(produitRepository.countByActifTrue()).willReturn(142L);
            given(produitRepository.sumQuantiteTotaleStock()).willReturn(8450L);

            StockValeurResponse result = dashboardService.calculerValeurStock();

            assertThat(result.getMargePotentielle()).isEqualTo(new BigDecimal("1700000"));
            assertThat(result.getValeurTotaleAchat()).isEqualTo(new BigDecimal("4500000"));
            assertThat(result.getValeurTotaleVente()).isEqualTo(new BigDecimal("6200000"));
            assertThat(result.getNombreProduitsActifs()).isEqualTo(142L);
            assertThat(result.getQuantiteTotaleStock()).isEqualTo(8450L);
        }

        @Test
        @DisplayName("✅ retourne des zéros quand le stock est vide")
        void calculerValeurStock_stockVide_doitRetournerZeros() {
            given(produitRepository.sumValeurStockAchat()).willReturn(BigDecimal.ZERO);
            given(produitRepository.sumValeurStockVente()).willReturn(BigDecimal.ZERO);
            given(produitRepository.countByActifTrue()).willReturn(0L);
            given(produitRepository.sumQuantiteTotaleStock()).willReturn(0L);

            StockValeurResponse result = dashboardService.calculerValeurStock();

            assertThat(result.getMargePotentielle()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getValeurTotaleAchat()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getValeurTotaleVente()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getNombreProduitsActifs()).isZero();
            assertThat(result.getQuantiteTotaleStock()).isZero();
        }
    }

    @Nested
    @DisplayName("statistiquesCommandesParStatut()")
    class StatistiquesCommandesParStatutTests {

        @Test
        @DisplayName("✅ mappe correctement les statistiques par statut")
        void statistiquesCommandesParStatut_casNominal_doitMapperCorrectement() {
            Object[] ligne1 = {StatutCommande.LIVREE, 37L, new BigDecimal("2150000")};
            Object[] ligne2 = {StatutCommande.CONFIRMEE, 12L, new BigDecimal("480000")};
            Object[] ligne3 = {StatutCommande.ANNULEE, 3L, new BigDecimal("75000")};
            given(commandeClientRepository.statistiquesParStatut())
                    .willReturn(List.of(ligne1, ligne2, ligne3));

            List<CommandeStatutStatResponse> result = dashboardService.statistiquesCommandesParStatut();

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStatut()).isEqualTo(StatutCommande.LIVREE);
            assertThat(result.get(0).getNombreCommandes()).isEqualTo(37L);
            assertThat(result.get(0).getMontantTotalTTC()).isEqualTo(new BigDecimal("2150000"));
            assertThat(result.get(1).getStatut()).isEqualTo(StatutCommande.CONFIRMEE);
            assertThat(result.get(2).getStatut()).isEqualTo(StatutCommande.ANNULEE);
        }

        @Test
        @DisplayName("✅ retourne une liste vide sans exception")
        void statistiquesCommandesParStatut_listeVide_doitRetournerListeVide() {
            given(commandeClientRepository.statistiquesParStatut()).willReturn(Collections.emptyList());

            List<CommandeStatutStatResponse> result = dashboardService.statistiquesCommandesParStatut();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("topProduitsVendus()")
    class TopProduitsVendusTests {

        @Test
        @DisplayName("✅ mappe correctement les Object[] en ProduitTopVenteResponse")
        void topProduitsVendus_casNominal_doitMapperCorrectement() {
            Object[] ligne1 = {1L, "PROD-001", "Ramette papier A4", 320L, new BigDecimal("960000")};
            Object[] ligne2 = {2L, "PROD-002", "Stylo bleu", 210L, new BigDecimal("42000")};
            Object[] ligne3 = {3L, "PROD-003", "Classeur A4", 150L, new BigDecimal("75000")};
            Object[] ligne4 = {4L, "PROD-004", "Agenda 2026", 95L, new BigDecimal("28500")};
            Object[] ligne5 = {5L, "PROD-005", "Toner HP", 42L, new BigDecimal("126000")};
            given(ligneCommandeClientRepository.topProduitsCommandes(any(Pageable.class)))
                    .willReturn(List.of(ligne1, ligne2, ligne3, ligne4, ligne5));

            List<ProduitTopVenteResponse> result = dashboardService.topProduitsVendus(5);

            assertThat(result).hasSize(5);
            assertThat(result.get(0).getProduitId()).isEqualTo(1L);
            assertThat(result.get(0).getReference()).isEqualTo("PROD-001");
            assertThat(result.get(0).getDesignation()).isEqualTo("Ramette papier A4");
            assertThat(result.get(0).getQuantiteTotaleCommandee()).isEqualTo(320L);
            assertThat(result.get(0).getChiffreAffairesHT()).isEqualTo(new BigDecimal("960000"));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            then(ligneCommandeClientRepository).should().topProduitsCommandes(pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("✅ retourne une liste vide avec limite zéro")
        void topProduitsVendus_limiteZero_doitRetournerListeVide() {
            List<ProduitTopVenteResponse> result = dashboardService.topProduitsVendus(0);

            assertThat(result).isEmpty();
            then(ligneCommandeClientRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("chiffreAffairesMois()")
    class ChiffreAffairesMoisTests {

        @Test
        @DisplayName("✅ retourne le CA et le nombre de commandes livrées")
        void chiffreAffairesMois_casNominal_doitRetournerDonneesCorrectes() {
            given(commandeClientRepository.sumChiffreAffairesMois(2026, 6))
                    .willReturn(new BigDecimal("3450000"));
            given(commandeClientRepository.countCommandesLivreesMois(2026, 6))
                    .willReturn(28L);

            ChiffreAffairesMoisResponse result = dashboardService.chiffreAffairesMois(2026, 6);

            assertThat(result.getAnnee()).isEqualTo(2026);
            assertThat(result.getMois()).isEqualTo(6);
            assertThat(result.getChiffreAffairesTTC()).isEqualTo(new BigDecimal("3450000"));
            assertThat(result.getNombreCommandesLivrees()).isEqualTo(28L);
        }

        @Test
        @DisplayName("✅ retourne zéro quand aucune commande livrée")
        void chiffreAffairesMois_aucuneCommande_doitRetournerZeros() {
            given(commandeClientRepository.sumChiffreAffairesMois(2025, 1))
                    .willReturn(BigDecimal.ZERO);
            given(commandeClientRepository.countCommandesLivreesMois(2025, 1))
                    .willReturn(0L);

            ChiffreAffairesMoisResponse result = dashboardService.chiffreAffairesMois(2025, 1);

            assertThat(result.getAnnee()).isEqualTo(2025);
            assertThat(result.getMois()).isEqualTo(1);
            assertThat(result.getChiffreAffairesTTC()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getNombreCommandesLivrees()).isZero();
        }
    }

    @Nested
    @DisplayName("genererDashboard()")
    class GenererDashboardTests {

        private void mockDependancesDashboard(List<Produit> produitsEnAlerte) {
            given(produitRepository.findProduitsEnAlerte()).willReturn(produitsEnAlerte);
            given(produitRepository.countProduitsEnRupture()).willReturn(5L);
            given(produitRepository.sumValeurStockAchat()).willReturn(new BigDecimal("1000000"));
            given(produitRepository.sumValeurStockVente()).willReturn(new BigDecimal("1500000"));
            given(produitRepository.countByActifTrue()).willReturn(50L);
            given(produitRepository.sumQuantiteTotaleStock()).willReturn(2000L);

            Object[] statut = {StatutCommande.LIVREE, 10L, new BigDecimal("500000")};
            given(commandeClientRepository.statistiquesParStatut())
                    .willReturn(List.<Object[]>of(statut));

            Object[] topProduit = {1L, "PROD-001", "Produit test", 100L, new BigDecimal("30000")};
            given(ligneCommandeClientRepository.topProduitsCommandes(any(Pageable.class)))
                    .willReturn(List.<Object[]>of(topProduit));

            given(commandeClientRepository.sumChiffreAffairesMois(any(Integer.class), any(Integer.class)))
                    .willReturn(new BigDecimal("250000"));
            given(commandeClientRepository.countCommandesLivreesMois(any(Integer.class), any(Integer.class)))
                    .willReturn(8L);

            given(fournisseurRepository.findAllByActifTrue()).willReturn(Collections.emptyList());

            Page<?> pageVide = new PageImpl<>(Collections.emptyList());
            given(commandeFournisseurRepository.findByStatut(eq(StatutCommandeFournisseur.ENVOYEE), any(Pageable.class)))
                    .willReturn((Page) pageVide);
            given(commandeFournisseurRepository.findByStatut(eq(StatutCommandeFournisseur.RECUE_PARTIELLE), any(Pageable.class)))
                    .willReturn((Page) pageVide);
        }

        @Test
        @DisplayName("✅ génère un snapshot complet du dashboard")
        void genererDashboard_casNominal_doitGenererSnapshotComplet() {
            Produit produit1 = Produit.builder().id(1L).reference("REF-001").designation("Produit 1").build();
            Produit produit2 = Produit.builder().id(2L).reference("REF-002").designation("Produit 2").build();
            ProduitResponse response1 = ProduitResponse.builder().id(1L).reference("REF-001").build();
            ProduitResponse response2 = ProduitResponse.builder().id(2L).reference("REF-002").build();

            mockDependancesDashboard(List.of(produit1, produit2));
            given(produitMapper.toResponse(produit1)).willReturn(response1);
            given(produitMapper.toResponse(produit2)).willReturn(response2);

            DashboardResponse dashboard = dashboardService.genererDashboard();

            assertThat(dashboard.getGenereLe()).isNotNull();
            assertThat(dashboard.getStock()).isNotNull();
            assertThat(dashboard.getCommandesParStatut()).isNotNull();
            assertThat(dashboard.getTopProduits()).isNotNull();
            assertThat(dashboard.getChiffreAffairesMoisCourant()).isNotNull();
            assertThat(dashboard.getNombreProduitsEnRupture()).isEqualTo(5L);
            assertThat(dashboard.getProduitsEnAlerte()).hasSize(2);

            then(produitMapper).should(times(2)).toResponse(any(Produit.class));
        }

        @Test
        @DisplayName("✅ retourne une liste vide de produits en alerte sans exception")
        void genererDashboard_aucunProduitEnAlerte_doitRetournerListeVide() {
            mockDependancesDashboard(Collections.emptyList());

            DashboardResponse dashboard = dashboardService.genererDashboard();

            assertThat(dashboard.getProduitsEnAlerte()).isEmpty();
            assertThat(dashboard.getGenereLe()).isNotNull();
            assertThat(dashboard.getStock()).isNotNull();
        }
    }
}
