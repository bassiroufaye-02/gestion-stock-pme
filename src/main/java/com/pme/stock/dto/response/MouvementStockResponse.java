package com.pme.stock.dto.response;

import com.pme.stock.entity.MouvementStock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockResponse {

    private Long id;
    private MouvementStock.TypeMouvement typeMouvement;
    private Integer quantite;
    private String motif;
    private Long produitId;
    private String produitReference;
    private String produitDesignation;
    private String utilisateurEmail;
    private LocalDateTime createdAt;
    private String createdBy;

    // Permet de traiter fromEntity.
    public static MouvementStockResponse fromEntity(MouvementStock mouvement) {
        return MouvementStockResponse.builder()
                .id(mouvement.getId())
                .typeMouvement(mouvement.getTypeMouvement())
                .quantite(mouvement.getQuantite())
                .motif(mouvement.getMotif())
                .produitId(mouvement.getProduit() != null ? mouvement.getProduit().getId() : null)
                .produitReference(mouvement.getProduit() != null ? mouvement.getProduit().getReference() : null)
                .produitDesignation(mouvement.getProduit() != null ? mouvement.getProduit().getDesignation() : null)
                .utilisateurEmail(mouvement.getUtilisateur() != null ? mouvement.getUtilisateur().getEmail() : null)
                .createdAt(mouvement.getCreatedAt())
                .createdBy(mouvement.getCreatedBy())
                .build();
    }
}
