package com.pme.stock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CommandeFournisseurRequest {

    @NotNull(message = "Le fournisseur est obligatoire")
    @Schema(description = "ID du fournisseur", example = "1")
    private Long fournisseurId;

    @Schema(description = "Date de commande prévue", example = "2026-07-15")
    private LocalDate dateCommandePrevue;

    @NotNull(message = "Le taux de TVA est obligatoire")
    @DecimalMin(value = "0.0", message = "Le taux de TVA doit être supérieur ou égal à 0")
    @DecimalMax(value = "100.0", message = "Le taux de TVA doit être inférieur ou égal à 100")
    @Schema(description = "Taux de TVA appliqué", example = "18.00")
    private BigDecimal tauxTVA;

    @Schema(description = "Notes ou instructions")
    private String notes;

    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    @Valid
    private List<LigneCommandeFournisseurRequest> lignes;
}
