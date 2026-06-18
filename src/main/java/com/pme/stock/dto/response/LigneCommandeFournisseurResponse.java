package com.pme.stock.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeFournisseurResponse {
    private Long id;
    private Integer quantiteCommandee;
    private Integer quantiteRecue;
    private BigDecimal prixUnitaireAchat;
    private BigDecimal montantLigneHT;
    private Long produitId;
    private String produitReference;
    private String produitDesignation;
    private Boolean receptionComplete;  // quantiteRecue >= quantiteCommandee
}
