package com.pme.stock.service.impl;

import com.pme.stock.dto.response.ChiffreAffairesMoisResponse;
import com.pme.stock.dto.response.CommandeStatutStatResponse;
import com.pme.stock.dto.response.DashboardResponse;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.dto.response.ProduitTopVenteResponse;
import com.pme.stock.dto.response.StockValeurResponse;
import com.pme.stock.entity.StatutCommande;
import com.pme.stock.entity.StatutCommandeFournisseur;
import com.pme.stock.mapper.ProduitMapper;
import com.pme.stock.repository.CommandeClientRepository;
import com.pme.stock.repository.CommandeFournisseurRepository;
import com.pme.stock.repository.FournisseurRepository;
import com.pme.stock.repository.LigneCommandeClientRepository;
import com.pme.stock.repository.ProduitRepository;
import com.pme.stock.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProduitRepository produitRepository;
    private final CommandeClientRepository commandeClientRepository;
    private final LigneCommandeClientRepository ligneCommandeClientRepository;
    private final ProduitMapper produitMapper;
    private final FournisseurRepository fournisseurRepository;
    private final CommandeFournisseurRepository commandeFournisseurRepository;

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter calculerValeurStock.
    public StockValeurResponse calculerValeurStock() {
        BigDecimal valeurAchat = produitRepository.sumValeurStockAchat();
        BigDecimal valeurVente = produitRepository.sumValeurStockVente();
        return StockValeurResponse.builder()
                .valeurTotaleAchat(valeurAchat)
                .valeurTotaleVente(valeurVente)
                .margePotentielle(valeurVente.subtract(valeurAchat))
                .nombreProduitsActifs(produitRepository.countByActifTrue())
                .quantiteTotaleStock(produitRepository.sumQuantiteTotaleStock())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter statistiquesCommandesParStatut.
    public List<CommandeStatutStatResponse> statistiquesCommandesParStatut() {
        List<Object[]> resultats = commandeClientRepository.statistiquesParStatut();
        return resultats.stream()
                .map(row -> CommandeStatutStatResponse.builder()
                        .statut((StatutCommande) row[0])
                        .nombreCommandes((Long) row[1])
                        .montantTotalTTC((BigDecimal) row[2])
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de convertir topProduitsVendus.
    public List<ProduitTopVenteResponse> topProduitsVendus(int limite) {
        if (limite <= 0) {
            return List.of();
        }
        List<Object[]> resultats = ligneCommandeClientRepository
                .topProduitsCommandes(PageRequest.of(0, limite));
        return resultats.stream()
                .map(row -> ProduitTopVenteResponse.builder()
                        .produitId((Long) row[0])
                        .reference((String) row[1])
                        .designation((String) row[2])
                        .quantiteTotaleCommandee((Long) row[3])
                        .chiffreAffairesHT((BigDecimal) row[4])
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter chiffreAffairesMois.
    public ChiffreAffairesMoisResponse chiffreAffairesMois(int annee, int mois) {
        BigDecimal ca = commandeClientRepository.sumChiffreAffairesMois(annee, mois);
        long nb = commandeClientRepository.countCommandesLivreesMois(annee, mois);
        return ChiffreAffairesMoisResponse.builder()
                .annee(annee)
                .mois(mois)
                .chiffreAffairesTTC(ca)
                .nombreCommandesLivrees(nb)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de g?n?rer genererDashboard.
    public DashboardResponse genererDashboard() {
        LocalDate maintenant = LocalDate.now();
        List<ProduitResponse> produitsEnAlerte = produitRepository.findProduitsEnAlerte()
                .stream()
                .map(produitMapper::toResponse)
                .toList();

        Pageable unpaged = Pageable.unpaged();

        return DashboardResponse.builder()
                .genereLe(LocalDateTime.now())
                .stock(calculerValeurStock())
                .produitsEnAlerte(produitsEnAlerte)
                .nombreProduitsEnRupture(produitRepository.countProduitsEnRupture())
                .commandesParStatut(statistiquesCommandesParStatut())
                .topProduits(topProduitsVendus(5))
                .chiffreAffairesMoisCourant(chiffreAffairesMois(maintenant.getYear(), maintenant.getMonthValue()))
                .nombreFournisseursActifs(fournisseurRepository.findAllByActifTrue().size())
                .nombreCommandesFournisseursEnAttente(
                        commandeFournisseurRepository.findByStatut(StatutCommandeFournisseur.ENVOYEE, unpaged).getTotalElements()
                                + commandeFournisseurRepository.findByStatut(StatutCommandeFournisseur.RECUE_PARTIELLE, unpaged).getTotalElements())
                .build();
    }
}
