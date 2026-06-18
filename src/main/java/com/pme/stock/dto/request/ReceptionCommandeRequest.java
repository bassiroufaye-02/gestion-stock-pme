package com.pme.stock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReceptionCommandeRequest {

    @NotNull
    @Schema(description = "ID de la commande fournisseur à réceptionner")
    private Long commandeId;

    @Schema(description = "Notes sur la réception (qualité, manques...)")
    private String notes;

    // Quantités réellement reçues par ligne (si null → 100% reçu pour toutes les lignes)
    private List<ReceptionLigneRequest> lignes;

    @Data
    public static class ReceptionLigneRequest {
        @NotNull
        private Long ligneId;

        @NotNull
        @Min(value = 0, message = "La quantité reçue doit être supérieure ou égale à 0")
        private Integer quantiteRecue;
    }
}
