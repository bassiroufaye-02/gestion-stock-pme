package com.pme.stock.service.impl;

import com.pme.stock.dto.request.MouvementStockRequest;
import com.pme.stock.entity.MouvementStock;
import com.pme.stock.entity.Produit;
import com.pme.stock.entity.Utilisateur;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.repository.MouvementStockRepository;
import com.pme.stock.repository.ProduitRepository;
import com.pme.stock.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public MouvementStock effectuerMouvement(MouvementStockRequest request) {
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit", request.getProduitId()));

        switch (request.getTypeMouvement()) {
            case ENTREE -> produit.incrementerStock(request.getQuantite());
            case SORTIE -> {
                try {
                    produit.decrementerStock(request.getQuantite());
                } catch (IllegalStateException e) {
                    throw new BusinessException(e.getMessage());
                }
            }
            case AJUSTEMENT -> {
                if (request.getQuantite() < 0) throw new BusinessException("La quantité d'ajustement doit être positive. Pour réduire le stock, utilisez SORTIE.");
                produit.setQuantiteStock(request.getQuantite());
            }
        }

        produitRepository.save(produit);

        // Alerte de stock
        if (produit.isEnRuptureAlerte()) {
            log.warn("⚠️ ALERTE STOCK - Produit {} : {} unités restantes (seuil : {})",
                    produit.getReference(), produit.getQuantiteStock(), produit.getSeuilAlerte());
        }

        // Récupération de l'utilisateur connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

        MouvementStock mouvement = MouvementStock.builder()
                .typeMouvement(request.getTypeMouvement())
                .quantite(request.getQuantite())
                .motif(request.getMotif())
                .produit(produit)
                .utilisateur(utilisateur)
                .build();

        return mouvementStockRepository.save(mouvement);
    }

    @Transactional(readOnly = true)
    public Page<MouvementStock> listerMouvementsProduit(Long produitId, Pageable pageable) {
        if (!produitRepository.existsById(produitId)) {
            throw new ResourceNotFoundException("Produit", produitId);
        }
        return mouvementStockRepository.findByProduitId(produitId, pageable);
    }
}
