package com.pme.stock.service;

import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.response.ProduitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProduitService {
    ProduitResponse creer(ProduitRequest request);
    ProduitResponse modifier(Long id, ProduitRequest request);
    ProduitResponse trouverParId(Long id);
    ProduitResponse trouverParReference(String reference);
    Page<ProduitResponse> listerTous(Pageable pageable);
    Page<ProduitResponse> rechercher(String search, Pageable pageable);
    Page<ProduitResponse> listerParCategorie(Long categorieId, Pageable pageable);
    List<ProduitResponse> listerProduitsEnAlerte();
    void desactiver(Long id);
    long compterProduitsEnAlerte();
}
