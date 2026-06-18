package com.pme.stock.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CommandeClientRequest {
    @NotNull
    private Long clientId;
    private LocalDate dateLivraisonPrevue;
    private BigDecimal tauxTVA = new BigDecimal("18.00");
    private String notes;
    private List<LigneCommandeRequest> lignes;
}
