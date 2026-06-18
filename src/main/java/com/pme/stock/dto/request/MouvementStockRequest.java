package com.pme.stock.dto.request;

import com.pme.stock.entity.MouvementStock;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MouvementStockRequest {

    @NotNull(message = "Le type de mouvement est obligatoire")
    private MouvementStock.TypeMouvement typeMouvement;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;

    private String motif;

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private Long produitId;
}
