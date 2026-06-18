package com.pme.stock.dto.response;

import com.pme.stock.entity.StatutCommandeFournisseur;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeFournisseurResponse {
    private Long id;
    private String numeroCommande;
    private LocalDate dateCommande;
    private LocalDate dateCommandePrevue;
    private LocalDate dateReception;
    private StatutCommandeFournisseur statut;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal tauxTVA;
    private String notes;
    private Long fournisseurId;
    private String fournisseurRaisonSociale;
    private String creePar;  // email de l'utilisateur créateur
    private List<LigneCommandeFournisseurResponse> lignes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
