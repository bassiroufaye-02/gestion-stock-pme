package com.pme.stock.service;

import com.pme.stock.dto.request.CommandeFournisseurRequest;
import com.pme.stock.dto.request.LigneCommandeFournisseurRequest;
import com.pme.stock.dto.response.CommandeFournisseurResponse;
import com.pme.stock.entity.StatutCommandeFournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommandeFournisseurService {
    CommandeFournisseurResponse creer(CommandeFournisseurRequest request);
    CommandeFournisseurResponse envoyer(Long id);
    CommandeFournisseurResponse receptionner(Long id);
    CommandeFournisseurResponse annuler(Long id);
    CommandeFournisseurResponse ajouterLigne(Long commandeId, LigneCommandeFournisseurRequest request);
    CommandeFournisseurResponse supprimerLigne(Long commandeId, Long ligneId);
    CommandeFournisseurResponse trouverParId(Long id);
    CommandeFournisseurResponse trouverParNumero(String numero);
    Page<CommandeFournisseurResponse> listerToutes(Pageable pageable);
    Page<CommandeFournisseurResponse> listerParFournisseur(Long fournisseurId, Pageable pageable);
    Page<CommandeFournisseurResponse> listerParStatut(StatutCommandeFournisseur statut, Pageable pageable);
}
