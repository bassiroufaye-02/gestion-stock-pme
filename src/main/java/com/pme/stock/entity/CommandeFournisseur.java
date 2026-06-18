package com.pme.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "commandes_fournisseurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeFournisseur extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_commande", nullable = false, unique = true, length = 30)
    private String numeroCommande;

    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande;

    @Column(name = "date_commande_prevue")
    private LocalDate dateCommandePrevue;

    @Column(name = "date_reception")
    private LocalDate dateReception;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private StatutCommandeFournisseur statut = StatutCommandeFournisseur.BROUILLON;

    @Column(name = "montant_ht", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantHT = BigDecimal.ZERO;

    @Column(name = "montant_tva", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTVA = BigDecimal.ZERO;

    @Column(name = "montant_ttc", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTTC = BigDecimal.ZERO;

    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxTVA = new BigDecimal("18.00");

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    @JsonIgnore
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id")
    @JsonIgnore
    private Utilisateur creePar;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<LigneCommandeFournisseur> lignes = new ArrayList<>();

    public void calculerMontants() {
        if (lignes != null) {
            lignes.forEach(LigneCommandeFournisseur::calculerMontantLigneHT);
        }
        this.montantHT = lignes == null ? BigDecimal.ZERO : lignes.stream()
                .map(LigneCommandeFournisseur::getMontantLigneHT)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taux = this.tauxTVA != null ? this.tauxTVA : BigDecimal.ZERO;
        this.montantTVA = this.montantHT.multiply(taux).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.montantTTC = this.montantHT.add(this.montantTVA);
    }

    @PostLoad
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

    public boolean peutEtreModifiee() {
        return this.statut == StatutCommandeFournisseur.BROUILLON;
    }

    public boolean peutEtreEnvoyee() {
        return this.statut == StatutCommandeFournisseur.BROUILLON
            && this.lignes != null && !this.lignes.isEmpty();
    }

    public boolean peutEtreReceptionnee() {
        return this.statut == StatutCommandeFournisseur.ENVOYEE
            || this.statut == StatutCommandeFournisseur.RECUE_PARTIELLE;
    }

    public boolean peutEtreAnnulee() {
        return this.statut == StatutCommandeFournisseur.BROUILLON
            || this.statut == StatutCommandeFournisseur.ENVOYEE;
    }
}
