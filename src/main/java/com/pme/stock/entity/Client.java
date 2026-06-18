package com.pme.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "raison_sociale", nullable = false, length = 200)
    private String raisonSociale;

    @Column(unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(length = 500)
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Column(nullable = false)
    private Boolean actif = true;

    @JsonIgnore
    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<CommandeClient> commandes = new ArrayList<>();
}
