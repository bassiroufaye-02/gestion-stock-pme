package com.pme.stock.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneCommandeRequest {
    @NotNull
    private Long produitId;
    @Min(1)
    private Integer quantite;
    @DecimalMin("0.01")
    private BigDecimal prixUnitaireHT;
}
