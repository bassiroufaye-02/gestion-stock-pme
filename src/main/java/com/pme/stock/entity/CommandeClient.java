package com.pme.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Représente une commande client globale, avec ses lignes, montants et statut de traitement.
@Entity
@Table(name = "commandes_clients")
@Getter
@Setter
public class CommandeClient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_commande", nullable = false, unique = true, length = 20)
    private String numeroCommande;

    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande;

    @Column(name = "date_livraison_prevue")
    private LocalDate dateLivraisonPrevue;

    @Column(name = "date_livraison_reelle")
    private LocalDate dateLivraisonReelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statut = StatutCommande.BROUILLON;

    @Column(name = "montant_ht", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantHT = BigDecimal.ZERO;

    @Column(name = "montant_tva", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTVA = BigDecimal.ZERO;

    @Column(name = "montant_ttc", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTTC = BigDecimal.ZERO;

    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTVA = new BigDecimal("18.00");

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LigneCommandeClient> lignes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traite_par_id")
    @JsonIgnore
    private Utilisateur traitePar;

    // Permet de recalculer les montants HT, TVA et TTC à partir des lignes de commande.
    public void calculerMontants() {
        if (lignes != null) {
            lignes.forEach(LigneCommandeClient::calculerMontantLigneHT);
        }
        this.montantHT = lignes == null ? BigDecimal.ZERO : lignes.stream()
                .map(LigneCommandeClient::getMontantLigneHT)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taux = this.tauxTVA != null ? this.tauxTVA : BigDecimal.ZERO;
        this.montantTVA = this.montantHT.multiply(taux).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.montantTTC = this.montantHT.add(this.montantTVA);
    }

    @PostLoad
    // Permet d’assurer que les montants ne sont jamais nuls lorsqu’ils sont chargés depuis la base.
    private void normaliserMontantsApresChargement() {
        if (montantHT == null) {
            montantHT = BigDecimal.ZERO;
        }
        if (montantTVA == null) {
            montantTVA = BigDecimal.ZERO;
        }
        if (montantTTC == null) {
            montantTTC = BigDecimal.ZERO;
        }
        if (tauxTVA == null) {
            tauxTVA = new BigDecimal("18.00");
        }
    }

    // Permet de vérifier si la commande peut encore être modifiée avant confirmation.
    public boolean peutEtreModifiee() {
        return this.statut == StatutCommande.BROUILLON;
    }

    // Permet de vérifier si la commande peut être annulée avant ou pendant son traitement.
    public boolean peutEtreAnnulee() {
        return this.statut == StatutCommande.BROUILLON || this.statut == StatutCommande.CONFIRMEE;
    }

    // Permet de vérifier si la commande peut passer au statut confirmé avec des lignes valides.
    public boolean peutEtreConfirmee() {
        return this.statut == StatutCommande.BROUILLON && lignes != null && !lignes.isEmpty();
    }
}
