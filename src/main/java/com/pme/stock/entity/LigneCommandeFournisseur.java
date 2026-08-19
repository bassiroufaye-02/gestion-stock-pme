package com.pme.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// Représente une ligne d'une commande fournisseur avec le produit, la quantité et le prix d'achat.
@Entity
@Table(name = "lignes_commandes_fournisseurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommandeFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantite_commandee", nullable = false)
    private Integer quantiteCommandee;

    @Column(name = "quantite_recue", nullable = false)
    @Builder.Default
    private Integer quantiteRecue = 0;

    @Column(name = "prix_unitaire_achat", nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaireAchat; // Prix d'achat négocié avec ce fournisseur

    @Column(name = "montant_ligne_ht", precision = 15, scale = 2)
    private BigDecimal montantLigneHT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    @JsonIgnore
    private CommandeFournisseur commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @JsonIgnore
    private Produit produit;

    // Permet de calculer le montant HT d'une ligne selon le prix d'achat et la quantité demandée.
    public void calculerMontantLigneHT() {
        if (this.prixUnitaireAchat != null && this.quantiteCommandee != null) {
            this.montantLigneHT = this.prixUnitaireAchat
                .multiply(BigDecimal.valueOf(this.quantiteCommandee));
        }
    }

    // Permet de vérifier si toute la quantité commandée a bien été livrée.
    public boolean estTotalementRecue() {
        return this.quantiteRecue >= this.quantiteCommandee;
    }
}
