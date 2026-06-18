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
public class ChiffreAffairesMoisResponse {

    @Schema(example = "2026")
    private int annee;

    @Schema(example = "6")
    private int mois;

    @Schema(description = "Chiffre d'affaires TTC du mois (commandes LIVREE uniquement)", example = "3450000.00")
    private BigDecimal chiffreAffairesTTC;

    @Schema(description = "Nombre de commandes livrées sur le mois", example = "28")
    private long nombreCommandesLivrees;
}
