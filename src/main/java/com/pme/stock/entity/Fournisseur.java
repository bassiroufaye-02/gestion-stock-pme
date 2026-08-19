package com.pme.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// Représente un fournisseur partenaire pour l'approvisionnement et les commandes d'achat.
@Entity
@Table(name = "fournisseurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fournisseur extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;  // Ex: "FOUR-001" — toujours en majuscules

    @Column(name = "raison_sociale", nullable = false, length = 200)
    private String raisonSociale;

    @Column(name = "email", length = 150, unique = true)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "adresse", length = 500)
    private String adresse;

    @Column(name = "ville", length = 100)
    private String ville;

    @Column(name = "pays", length = 100)
    @Builder.Default
    private String pays = "Sénégal";

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @OneToMany(mappedBy = "fournisseur", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<CommandeFournisseur> commandes = new ArrayList<>();

    @OneToMany(mappedBy = "fournisseur", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Produit> produits = new ArrayList<>();
}
