package com.pme.stock.service.impl;

import com.pme.stock.dto.request.CommandeFournisseurRequest;
import com.pme.stock.dto.request.LigneCommandeFournisseurRequest;
import com.pme.stock.dto.response.CommandeFournisseurResponse;
import com.pme.stock.entity.*;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.CommandeFournisseurMapper;
import com.pme.stock.repository.*;
import com.pme.stock.service.CommandeFournisseurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandeFournisseurServiceImpl implements CommandeFournisseurService {

    private final CommandeFournisseurRepository commandeFournisseurRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final CommandeFournisseurMapper commandeFournisseurMapper;

    @Override
    @Transactional
    // Permet de cr?er creer.
    public CommandeFournisseurResponse creer(CommandeFournisseurRequest request) {
        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", request.getFournisseurId()));

        if (!Boolean.TRUE.equals(fournisseur.getActif())) {
            throw new BusinessException("Le fournisseur '" + fournisseur.getRaisonSociale() + "' n'est pas actif");
        }

        BigDecimal tauxTVA = request.getTauxTVA() != null ? request.getTauxTVA() : new BigDecimal("18.00");

        CommandeFournisseur commande = CommandeFournisseur.builder()
                .numeroCommande(genererNumeroCommande())
                .dateCommande(LocalDate.now())
                .dateCommandePrevue(request.getDateCommandePrevue())
                .statut(StatutCommandeFournisseur.BROUILLON)
                .tauxTVA(tauxTVA)
                .fournisseur(fournisseur)
                .notes(request.getNotes())
                .build();

        List<LigneCommandeFournisseur> lignes = new ArrayList<>();
        for (LigneCommandeFournisseurRequest lineReq : request.getLignes()) {
            Produit produit = produitRepository.findById(lineReq.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit", lineReq.getProduitId()));

            if (!Boolean.TRUE.equals(produit.getActif())) {
                throw new BusinessException("Le produit '" + produit.getReference() + "' n'est pas actif");
            }

            LigneCommandeFournisseur ligne = LigneCommandeFournisseur.builder()
                    .quantiteCommandee(lineReq.getQuantiteCommandee())
                    .quantiteRecue(0)
                    .prixUnitaireAchat(lineReq.getPrixUnitaireAchat())
                    .produit(produit)
                    .commande(commande)
                    .build();
            ligne.calculerMontantLigneHT();
            lignes.add(ligne);
        }
        commande.setLignes(lignes);
        commande.calculerMontants();
        commande.setCreePar(getUtilisateurConnecte());

        commande = commandeFournisseurRepository.save(commande);
        log.info("Commande fournisseur créée : {} pour le fournisseur {}", commande.getNumeroCommande(), fournisseur.getRaisonSociale());
        return commandeFournisseurMapper.toResponse(commande);
    }

    @Override
    @Transactional
    // Permet de traiter envoyer.
    public CommandeFournisseurResponse envoyer(Long id) {
        CommandeFournisseur commande = commandeFournisseurRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", id));

        if (!commande.peutEtreEnvoyee()) {
            throw new BusinessException("La commande doit être en BROUILLON et contenir des lignes");
        }

        commande.setStatut(StatutCommandeFournisseur.ENVOYEE);
        commande = commandeFournisseurRepository.save(commande);
        log.info("Commande fournisseur {} envoyée", commande.getNumeroCommande());
        return commandeFournisseurMapper.toResponse(commande);
    }

    @Override
    @Transactional
    // Permet de traiter receptionner.
    public CommandeFournisseurResponse receptionner(Long id) {
        CommandeFournisseur commande = commandeFournisseurRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", id));

        if (!commande.peutEtreReceptionnee()) {
            throw new BusinessException("La commande n'est pas dans un état réceptionnable. Statut actuel : " + commande.getStatut());
        }

        Utilisateur utilisateurConnecte = getUtilisateurConnecte();

        for (LigneCommandeFournisseur ligne : commande.getLignes()) {
            int quantiteARecevoir = ligne.getQuantiteCommandee() - ligne.getQuantiteRecue();
            if (quantiteARecevoir > 0) {
                Produit produit = ligne.getProduit();
                produit.incrementerStock(quantiteARecevoir);
                ligne.setQuantiteRecue(ligne.getQuantiteCommandee());
                produitRepository.save(produit);

                MouvementStock mouvement = MouvementStock.builder()
                        .typeMouvement(MouvementStock.TypeMouvement.ENTREE)
                        .quantite(quantiteARecevoir)
                        .motif("Réception commande fournisseur " + commande.getNumeroCommande())
                        .produit(produit)
                        .utilisateur(utilisateurConnecte)
                        .build();
                mouvementStockRepository.save(mouvement);

                log.info("Réception : +{} unités de {}", quantiteARecevoir, produit.getReference());
                if (produit.isEnRuptureAlerte()) {
                    log.warn("Alerte stock : le produit {} est en dessous du seuil d'alerte (stock={}, seuil={})",
                            produit.getReference(), produit.getQuantiteStock(), produit.getSeuilAlerte());
                }
            }
        }

        boolean toutesLignesRecues = commande.getLignes().stream()
                .allMatch(LigneCommandeFournisseur::estTotalementRecue);

        if (toutesLignesRecues) {
            commande.setStatut(StatutCommandeFournisseur.RECUE);
            commande.setDateReception(LocalDate.now());
        } else {
            commande.setStatut(StatutCommandeFournisseur.RECUE_PARTIELLE);
        }

        commande = commandeFournisseurRepository.save(commande);
        log.info("Réception commande {} — statut: {}", commande.getNumeroCommande(), commande.getStatut());
        return commandeFournisseurMapper.toResponse(commande);
    }

    @Override
    @Transactional
    // Permet de supprimer annuler.
    public CommandeFournisseurResponse annuler(Long id) {
        CommandeFournisseur commande = commandeFournisseurRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", id));

        if (!commande.peutEtreAnnulee()) {
            throw new BusinessException("La commande ne peut plus être annulée");
        }

        commande.setStatut(StatutCommandeFournisseur.ANNULEE);
        commande = commandeFournisseurRepository.save(commande);
        log.info("Commande annulée : {}", commande.getNumeroCommande());
        return commandeFournisseurMapper.toResponse(commande);
    }

    @Override
    @Transactional
    // Permet de cr?er ajouterLigne.
    public CommandeFournisseurResponse ajouterLigne(Long commandeId, LigneCommandeFournisseurRequest request) {
        CommandeFournisseur commande = commandeFournisseurRepository.findByIdWithRelations(commandeId)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", commandeId));

        if (!commande.peutEtreModifiee()) {
            throw new BusinessException("Impossible de modifier : la commande doit être en statut BROUILLON");
        }

        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit", request.getProduitId()));

        if (!Boolean.TRUE.equals(produit.getActif())) {
            throw new BusinessException("Le produit '" + produit.getReference() + "' n'est pas actif");
        }

        LigneCommandeFournisseur ligne = LigneCommandeFournisseur.builder()
                .quantiteCommandee(request.getQuantiteCommandee())
                .quantiteRecue(0)
                .prixUnitaireAchat(request.getPrixUnitaireAchat())
                .produit(produit)
                .commande(commande)
                .build();
        ligne.calculerMontantLigneHT();
        commande.getLignes().add(ligne);
        commande.calculerMontants();

        commande = commandeFournisseurRepository.save(commande);
        log.info("Ligne ajoutée à la commande fournisseur {}", commandeId);
        return commandeFournisseurMapper.toResponse(commande);
    }

    @Override
    @Transactional
    // Permet de supprimer supprimerLigne.
    public CommandeFournisseurResponse supprimerLigne(Long commandeId, Long ligneId) {
        CommandeFournisseur commande = commandeFournisseurRepository.findByIdWithRelations(commandeId)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", commandeId));

        if (!commande.peutEtreModifiee()) {
            throw new BusinessException("Impossible de modifier : la commande doit être en statut BROUILLON");
        }

        LigneCommandeFournisseur ligneToRemove = commande.getLignes().stream()
                .filter(l -> l.getId() != null && l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("LigneCommandeFournisseur", ligneId));

        commande.getLignes().remove(ligneToRemove);
        commande.calculerMontants();
        commande = commandeFournisseurRepository.save(commande);
        log.info("Ligne {} supprimée de la commande fournisseur {}", ligneId, commandeId);
        return commandeFournisseurMapper.toResponse(commande);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter trouverParId.
    public CommandeFournisseurResponse trouverParId(Long id) {
        CommandeFournisseur commande = commandeFournisseurRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", id));
        return commandeFournisseurMapper.toResponse(commande);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter trouverParNumero.
    public CommandeFournisseurResponse trouverParNumero(String numero) {
        CommandeFournisseur commande = commandeFournisseurRepository.findByNumeroCommande(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Commande fournisseur avec numéro " + numero));
        return commandeFournisseurMapper.toResponse(commande);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter listerToutes.
    public Page<CommandeFournisseurResponse> listerToutes(Pageable pageable) {
        return commandeFournisseurRepository.findAllWithFournisseur(pageable)
                .map(commandeFournisseurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter listerParFournisseur.
    public Page<CommandeFournisseurResponse> listerParFournisseur(Long fournisseurId, Pageable pageable) {
        return commandeFournisseurRepository.findByFournisseurId(fournisseurId, pageable)
                .map(commandeFournisseurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    // Permet de traiter listerParStatut.
    public Page<CommandeFournisseurResponse> listerParStatut(StatutCommandeFournisseur statut, Pageable pageable) {
        return commandeFournisseurRepository.findByStatut(statut, pageable)
                .map(commandeFournisseurMapper::toResponse);
    }

    // Permet de g?n?rer genererNumeroCommande.
    private String genererNumeroCommande() {
        int year = LocalDate.now().getYear();
        String prefixe = "CF-" + year + "-";
        Integer max = commandeFournisseurRepository.findMaxSequenceNumber(prefixe);
        int seq = (max == null ? 0 : max) + 1;
        return prefixe + String.format("%05d", seq);
    }

    // Permet de r?cup?rer UtilisateurConnecte.
    private Utilisateur getUtilisateurConnecte() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return utilisateurRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            log.debug("Impossible de récupérer l'utilisateur courant");
            return null;
        }
    }
}
