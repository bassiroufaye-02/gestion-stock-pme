package com.pme.stock.service;

import com.pme.stock.dto.request.CommandeClientRequest;
import com.pme.stock.dto.request.LigneCommandeRequest;
import com.pme.stock.dto.response.CommandeClientResponse;
import com.pme.stock.entity.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommandeClientService {
    CommandeClientResponse creer(CommandeClientRequest request);
    CommandeClientResponse confirmer(Long id);
    CommandeClientResponse changerStatut(Long id, StatutCommande nouveauStatut);
    CommandeClientResponse ajouterLigne(Long commandeId, LigneCommandeRequest request);
    CommandeClientResponse supprimerLigne(Long commandeId, Long ligneId);
    CommandeClientResponse trouverParId(Long id);
    Page<CommandeClientResponse> listerParClient(Long clientId, Pageable pageable);
    Page<CommandeClientResponse> listerParStatut(StatutCommande statut, Pageable pageable);
    Page<CommandeClientResponse> listerToutes(Pageable pageable);
    CommandeClientResponse rechercherParNumero(String numero);
}
