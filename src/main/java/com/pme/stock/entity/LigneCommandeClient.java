package com.pme.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// Représente une ligne d'une commande client avec le produit, la quantité et le montant associé.
@Entity
@Table(name = "lignes_commandes_clients")
@Getter
@Setter
public class LigneCommandeClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire_ht", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaireHT;

    @Column(name = "montant_ligne_ht", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantLigneHT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    @JsonIgnore
    private CommandeClient commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @JsonIgnore
    private Produit produit;

    // Permet de calculer le montant HT d'une ligne selon la quantité et le prix unitaire.
    public void calculerMontantLigneHT() {
        if (this.quantite != null && this.prixUnitaireHT != null) {
            this.montantLigneHT = this.prixUnitaireHT.multiply(BigDecimal.valueOf(this.quantite));
        } else if (this.montantLigneHT == null) {
            this.montantLigneHT = BigDecimal.ZERO;
        }
    }

    @PrePersist
    @PreUpdate
    @PostLoad
    // Permet de synchroniser automatiquement le montant d'une ligne avant sauvegarde ou chargement.
    private void synchroniserMontantLigneHT() {
        calculerMontantLigneHT();
    }
}
