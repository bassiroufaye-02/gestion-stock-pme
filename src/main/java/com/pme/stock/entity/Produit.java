package com.pme.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Column(name = "designation", nullable = false, length = 255)
    private String designation;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "prix_achat", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal prixAchat = BigDecimal.ZERO;

    @Column(name = "prix_vente", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal prixVente = BigDecimal.ZERO;

    @Column(name = "quantite_stock", nullable = false)
    @Builder.Default
    private Integer quantiteStock = 0;

    @Column(name = "seuil_alerte", nullable = false)
    @Builder.Default
    private Integer seuilAlerte = 5;

    @Column(name = "unite_mesure", length = 50)
    @Builder.Default
    private String uniteMesure = "unité";

    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @OneToMany(mappedBy = "produit", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<MouvementStock> mouvements = new ArrayList<>();

    // === Méthodes métier ===

    public boolean isEnRuptureAlerte() {
        return this.quantiteStock <= this.seuilAlerte;
    }

    public boolean isEnRupture() {
        return this.quantiteStock == 0;
    }

    public void incrementerStock(int quantite) {
        if (quantite <= 0) throw new IllegalArgumentException("La quantité doit être positive");
        this.quantiteStock += quantite;
    }

    public void decrementerStock(int quantite) {
        if (quantite <= 0) throw new IllegalArgumentException("La quantité doit être positive");
        if (this.quantiteStock < quantite) throw new IllegalStateException("Stock insuffisant : disponible=" + this.quantiteStock + ", demandé=" + quantite);
        this.quantiteStock -= quantite;
    }
}
