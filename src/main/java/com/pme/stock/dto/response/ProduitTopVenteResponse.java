package com.pme.stock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitTopVenteResponse {

    @Schema(example = "12")
    private Long produitId;

    @Schema(example = "PROD-0012")
    private String reference;

    @Schema(example = "Ramette papier A4 80g")
    private String designation;

    @Schema(description = "Quantité totale commandée toutes commandes confondues (hors ANNULEE)", example = "320")
    private long quantiteTotaleCommandee;

    @Schema(description = "Chiffre d'affaires HT généré par ce produit", example = "960000.00")
    private BigDecimal chiffreAffairesHT;
}
