package com.pme.stock.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurResponse {
    private Long id;
    private String code;
    private String raisonSociale;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private String pays;
    private Boolean actif;
    private int nombreCommandes;  // commandes.size()
    private int nombreProduits;   // produits.size()
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
