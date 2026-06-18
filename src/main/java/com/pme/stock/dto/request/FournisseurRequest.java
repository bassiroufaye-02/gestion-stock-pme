package com.pme.stock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FournisseurRequest {

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20)
    @Schema(description = "Code unique du fournisseur", example = "FOUR-001")
    private String code;

    @NotBlank(message = "La raison sociale est obligatoire")
    @Size(max = 200)
    @Schema(description = "Raison sociale", example = "SENELEC Distribution")
    private String raisonSociale;

    @Email(message = "Format email invalide")
    @Size(max = 150)
    @Schema(description = "Email de contact", example = "contact@senelec.sn")
    private String email;

    @Size(max = 20)
    @Schema(description = "Téléphone", example = "+221 33 839 30 00")
    private String telephone;

    @Size(max = 500)
    private String adresse;

    @Size(max = 100)
    private String ville;

    @Size(max = 100)
    @Schema(description = "Pays", example = "Sénégal")
    private String pays;
}
