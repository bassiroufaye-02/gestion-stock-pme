package com.pme.stock.service.impl;

import com.pme.stock.dto.request.CommandeClientRequest;
import com.pme.stock.dto.request.LigneCommandeRequest;
import com.pme.stock.dto.response.CommandeClientResponse;
import com.pme.stock.entity.*;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.CommandeClientMapper;
import com.pme.stock.repository.*;
import com.pme.stock.service.CommandeClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandeClientServiceImpl implements CommandeClientService {

    private final CommandeClientRepository commandeClientRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CommandeClientMapper commandeClientMapper;

    @Override
    @Transactional
    public CommandeClientResponse creer(CommandeClientRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id: " + request.getClientId()));

        CommandeClient commande = new CommandeClient();
        commande.setClient(client);
        commande.setNumeroCommande(genererNumeroCommande());
        commande.setDateCommande(LocalDate.now());
        commande.setDateLivraisonPrevue(request.getDateLivraisonPrevue());
        commande.setTauxTVA(request.getTauxTVA());
        commande.setNotes(request.getNotes());
        commande.setStatut(StatutCommande.BROUILLON);

        List<LigneCommandeClient> lignes = new ArrayList<>();
        if (request.getLignes() != null) {
            for (LigneCommandeRequest lReq : request.getLignes()) {
                Produit produit = produitRepository.findById(lReq.getProduitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + lReq.getProduitId()));

                if (!produit.getActif()) {
                    throw new BusinessException("Le produit " + produit.getReference() + " n'est pas actif");
                }

                LigneCommandeClient ligne = new LigneCommandeClient();
                ligne.setProduit(produit);
                ligne.setQuantite(lReq.getQuantite());
                ligne.setPrixUnitaireHT(lReq.getPrixUnitaireHT() != null ? lReq.getPrixUnitaireHT() : produit.getPrixVente());
                ligne.setCommande(commande);
                ligne.calculerMontantLigneHT();
                lignes.add(ligne);
            }
        }
        commande.setLignes(lignes);
        commande.calculerMontants();

        commande = commandeClientRepository.save(commande);
        log.info("Commande créée : {} pour le client {}", commande.getNumeroCommande(), client.getRaisonSociale());
        return commandeClientMapper.toResponse(commande);
    }

    @Override
    @Transactional
    public CommandeClientResponse confirmer(Long id) {
        CommandeClient commande = commandeClientRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id: " + id));

        if (!commande.peutEtreConfirmee()) {
            throw new BusinessException("La commande ne peut pas être confirmée (statut doit être BROUILLON et doit contenir des lignes)");
        }

        // Vérifier le stock disponible
        for (LigneCommandeClient ligne : commande.getLignes()) {
            Produit produit = ligne.getProduit();
            if (produit.getQuantiteStock() < ligne.getQuantite()) {
                throw new BusinessException("Stock insuffisant pour le produit " + produit.getReference()
                    + ": disponible=" + produit.getQuantiteStock() + ", demandé=" + ligne.getQuantite());
            }
        }

        // Décrémenter le stock
        for (LigneCommandeClient ligne : commande.getLignes()) {
            Produit produit = ligne.getProduit();
            produit.decrementerStock(ligne.getQuantite());
            produitRepository.save(produit);
        }

        commande.setStatut(StatutCommande.CONFIRMEE);

        // Associer à l'utilisateur qui confirme
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            utilisateurRepository.findByEmail(email).ifPresent(commande::setTraitePar);
        } catch (Exception e) {
            log.debug("Impossible de récupérer l'utilisateur courant");
        }

        commande = commandeClientRepository.save(commande);
        log.info("Commande confirmée : {}", commande.getNumeroCommande());
        return commandeClientMapper.toResponse(commande);
    }

    @Override
    @Transactional
    public CommandeClientResponse changerStatut(Long id, StatutCommande nouveauStatut) {
        CommandeClient commande = commandeClientRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id: " + id));

        StatutCommande ancienStatut = commande.getStatut();

        // Validation des transitions
        if (!estTransitionValide(ancienStatut, nouveauStatut)) {
            throw new BusinessException("Transition invalide de " + ancienStatut + " vers " + nouveauStatut);
        }

        // Traitement spécifique de CONFIRMEE
        if (nouveauStatut == StatutCommande.CONFIRMEE) {
            return confirmer(id);
        }

        // Traitement de LIVREE
        if (nouveauStatut == StatutCommande.LIVREE) {
            commande.setDateLivraisonReelle(LocalDate.now());
        }

        // Remettre le stock si annulation de commande confirmée
        if (nouveauStatut == StatutCommande.ANNULEE && ancienStatut == StatutCommande.CONFIRMEE) {
            for (LigneCommandeClient ligne : commande.getLignes()) {
                Produit produit = ligne.getProduit();
                produit.incrementerStock(ligne.getQuantite());
                produitRepository.save(produit);
            }
        }

        commande.setStatut(nouveauStatut);
        commande = commandeClientRepository.save(commande);
        log.info("Statut de la commande {} changé : {} -> {}", id, ancienStatut, nouveauStatut);
        return commandeClientMapper.toResponse(commande);
    }

    @Override
    @Transactional
    public CommandeClientResponse ajouterLigne(Long commandeId, LigneCommandeRequest request) {
        CommandeClient commande = commandeClientRepository.findByIdWithRelations(commandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id: " + commandeId));

        if (!commande.peutEtreModifiee()) {
            throw new BusinessException("Impossible de modifier : la commande doit être en statut BROUILLON");
        }

        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + request.getProduitId()));

        if (!produit.getActif()) {
            throw new BusinessException("Le produit " + produit.getReference() + " n'est pas actif");
        }

        LigneCommandeClient ligne = new LigneCommandeClient();
        ligne.setProduit(produit);
        ligne.setQuantite(request.getQuantite());
        ligne.setPrixUnitaireHT(request.getPrixUnitaireHT() != null ? request.getPrixUnitaireHT() : produit.getPrixVente());
        ligne.setCommande(commande);
        ligne.calculerMontantLigneHT();
        commande.getLignes().add(ligne);
        commande.calculerMontants();

        commande = commandeClientRepository.save(commande);
        log.info("Ligne ajoutée à la commande {}", commandeId);
        return commandeClientMapper.toResponse(commande);
    }

    @Override
    @Transactional
    public CommandeClientResponse supprimerLigne(Long commandeId, Long ligneId) {
        CommandeClient commande = commandeClientRepository.findByIdWithRelations(commandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id: " + commandeId));

        if (!commande.peutEtreModifiee()) {
            throw new BusinessException("Impossible de modifier : la commande doit être en statut BROUILLON");
        }

        LigneCommandeClient ligneToRemove = commande.getLignes().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée avec l'id: " + ligneId));

        commande.getLignes().remove(ligneToRemove);
        commande.calculerMontants();
        commande = commandeClientRepository.save(commande);
        log.info("Ligne {} supprimée de la commande {}", ligneId, commandeId);
        return commandeClientMapper.toResponse(commande);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeClientResponse trouverParId(Long id) {
        CommandeClient commande = commandeClientRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id: " + id));
        return commandeClientMapper.toResponse(commande);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandeClientResponse> listerParClient(Long clientId, Pageable pageable) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client non trouvé avec l'id: " + clientId);
        }
        return commandeClientRepository.findByClientIdWithClient(clientId, pageable).map(commandeClientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandeClientResponse> listerParStatut(StatutCommande statut, Pageable pageable) {
        return commandeClientRepository.findByStatutWithClient(statut, pageable).map(commandeClientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandeClientResponse> listerToutes(Pageable pageable) {
        return commandeClientRepository.findAllWithClient(pageable).map(commandeClientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeClientResponse rechercherParNumero(String numero) {
        CommandeClient commande = commandeClientRepository.findByNumeroCommandeWithRelations(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec le numéro: " + numero));
        return commandeClientMapper.toResponse(commande);
    }

    private String genererNumeroCommande() {
        YearMonth yearMonth = YearMonth.now();
        String prefixe = "CMD-" + yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")) + "-";

        Integer maxSeq = commandeClientRepository.findMaxSequenceNumber(prefixe);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;

        return prefixe + String.format("%04d", nextSeq);
    }

    private boolean estTransitionValide(StatutCommande ancien, StatutCommande nouveau) {
        return switch (ancien) {
            case BROUILLON -> nouveau == StatutCommande.CONFIRMEE || nouveau == StatutCommande.ANNULEE;
            case CONFIRMEE -> nouveau == StatutCommande.EN_PREPARATION || nouveau == StatutCommande.ANNULEE;
            case EN_PREPARATION -> nouveau == StatutCommande.EXPEDIEE;
            case EXPEDIEE -> nouveau == StatutCommande.LIVREE;
            case LIVREE -> false;
            case ANNULEE -> false;
        };
    }
}
