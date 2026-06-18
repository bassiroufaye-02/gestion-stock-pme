package com.pme.stock.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProduitResponse {
    private Long id;
    private String reference;
    private String designation;
    private String description;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private Integer quantiteStock;
    private Integer seuilAlerte;
    private String uniteMesure;
    private Boolean actif;
    private Boolean enAlerte;
    private Boolean enRupture;
    private Long categorieId;
    private String categorieLibelle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
