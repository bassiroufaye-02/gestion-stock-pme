package com.pme.stock.service.impl;

import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.entity.Categorie;
import com.pme.stock.entity.Produit;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.ProduitMapper;
import com.pme.stock.repository.CategorieRepository;
import com.pme.stock.repository.ProduitRepository;
import com.pme.stock.service.ProduitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final ProduitMapper produitMapper;

    @Override
    @Transactional
    // Permet de cr?er creer.
    public ProduitResponse creer(ProduitRequest request) {
        if (produitRepository.existsByReference(request.getReference())) {
            throw new BusinessException("Un produit avec la référence '" + request.getReference() + "' existe déjà");
        }
        Produit produit = mapperVersProduit(request, new Produit());
        produit = produitRepository.save(produit);
        log.info("Produit créé : {} - {}", produit.getReference(), produit.getDesignation());
        return produitMapper.toResponse(produit);
    }

    @Override
    @Transactional
    // Permet de modifier modifier.
    public ProduitResponse modifier(Long id, ProduitRequest request) {
        Produit produit = trouverProduitOuException(id);

        if (!produit.getReference().equals(request.getReference())
                && produitRepository.existsByReference(request.getReference())) {
            throw new BusinessException("La référence '" + request.getReference() + "' est déjà utilisée");
        }

        produit = mapperVersProduit(request, produit);
        produit = produitRepository.save(produit);
        return produitMapper.toResponse(produit);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter trouverParId.
    public ProduitResponse trouverParId(Long id) {
        return produitMapper.toResponse(trouverProduitOuException(id));
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter trouverParReference.
    public ProduitResponse trouverParReference(String reference) {
        Produit produit = produitRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec la référence : " + reference));
        return produitMapper.toResponse(produit);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter listerTous.
    public Page<ProduitResponse> listerTous(Pageable pageable) {
        return produitRepository.findAll(pageable).map(produitMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter rechercher.
    public Page<ProduitResponse> rechercher(String search, Pageable pageable) {
        return produitRepository.rechercherProduits(search, pageable).map(produitMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter listerParCategorie.
    public Page<ProduitResponse> listerParCategorie(Long categorieId, Pageable pageable) {
        return produitRepository.findByCategorieId(categorieId, pageable).map(produitMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter listerProduitsEnAlerte.
    public List<ProduitResponse> listerProduitsEnAlerte() {
        return produitRepository.findProduitsEnAlerte().stream()
                .map(produitMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    // Permet de supprimer desactiver.
    public void desactiver(Long id) {
        Produit produit = trouverProduitOuException(id);
        produit.setActif(false);
        produitRepository.save(produit);
        log.info("Produit désactivé : {}", produit.getReference());
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter compterProduitsEnAlerte.
    public long compterProduitsEnAlerte() {
        return produitRepository.countProduitsEnAlerte();
    }

    // === Méthodes privées ===

    // Permet de traiter trouverProduitOuException.
    private Produit trouverProduitOuException(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));
    }

    // Permet de traiter mapperVersProduit.
    private Produit mapperVersProduit(ProduitRequest request, Produit produit) {
        produit.setReference(request.getReference());
        produit.setDesignation(request.getDesignation());
        produit.setDescription(request.getDescription());
        produit.setPrixAchat(request.getPrixAchat());
        produit.setPrixVente(request.getPrixVente());
        produit.setQuantiteStock(request.getQuantiteStock() != null ? request.getQuantiteStock() : 0);
        produit.setSeuilAlerte(request.getSeuilAlerte() != null ? request.getSeuilAlerte() : 5);
        produit.setUniteMesure(request.getUniteMesure());

        if (request.getCategorieId() != null) {
            Categorie categorie = categorieRepository.findById(request.getCategorieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categorie", request.getCategorieId()));
            produit.setCategorie(categorie);
        }
        return produit;
    }
}
