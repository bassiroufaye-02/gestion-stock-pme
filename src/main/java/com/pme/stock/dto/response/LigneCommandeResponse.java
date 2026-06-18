package com.pme.stock.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneCommandeResponse {
    private Long id;
    private Integer quantite;
    private BigDecimal prixUnitaireHT;
    private BigDecimal montantLigneHT;
    private Long produitId;
    private String produitReference;
    private String produitDesignation;
}
