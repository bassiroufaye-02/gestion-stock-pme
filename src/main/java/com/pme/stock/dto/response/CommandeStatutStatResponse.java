package com.pme.stock.dto.response;

import com.pme.stock.entity.StatutCommande;
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
public class CommandeStatutStatResponse {

    @Schema(description = "Statut de la commande", example = "LIVREE")
    private StatutCommande statut;

    @Schema(description = "Nombre de commandes dans ce statut", example = "37")
    private long nombreCommandes;

    @Schema(description = "Montant TTC total cumulé de ces commandes", example = "2150000.00")
    private BigDecimal montantTotalTTC;
}
