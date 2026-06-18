package com.pme.stock.service;

import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.response.FournisseurResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FournisseurService {
    FournisseurResponse creer(FournisseurRequest request);
    FournisseurResponse modifier(Long id, FournisseurRequest request);
    FournisseurResponse trouverParId(Long id);
    FournisseurResponse trouverParCode(String code);
    List<FournisseurResponse> listerActifs();
    Page<FournisseurResponse> rechercher(String search, Pageable pageable);
    void desactiver(Long id);
}
