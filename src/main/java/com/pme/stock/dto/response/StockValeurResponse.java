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
public class StockValeurResponse {

    @Schema(description = "Valeur totale du stock au prix d'achat", example = "4500000.00")
    private BigDecimal valeurTotaleAchat;

    @Schema(description = "Valeur totale du stock au prix de vente", example = "6200000.00")
    private BigDecimal valeurTotaleVente;

    @Schema(description = "Marge potentielle totale (vente - achat)", example = "1700000.00")
    private BigDecimal margePotentielle;

    @Schema(description = "Nombre total de produits actifs en stock", example = "142")
    private long nombreProduitsActifs;

    @Schema(description = "Quantité totale d'unités en stock toutes références confondues", example = "8450")
    private long quantiteTotaleStock;
}
