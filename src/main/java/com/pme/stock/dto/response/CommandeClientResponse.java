package com.pme.stock.dto.response;

import com.pme.stock.entity.StatutCommande;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommandeClientResponse {
    private Long id;
    private String numeroCommande;
    private LocalDate dateCommande;
    private LocalDate dateLivraisonPrevue;
    private LocalDate dateLivraisonReelle;
    private StatutCommande statut;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal tauxTVA;
    private String notes;
    private Long clientId;
    private String clientRaisonSociale;
    private String traitePar;
    private List<LigneCommandeResponse> lignes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
