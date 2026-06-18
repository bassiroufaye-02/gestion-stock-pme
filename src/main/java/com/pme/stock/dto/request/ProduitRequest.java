package com.pme.stock.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProduitRequest {

    @NotBlank(message = "La référence est obligatoire")
    @Size(max = 100)
    private String reference;

    @NotBlank(message = "La désignation est obligatoire")
    @Size(max = 255)
    private String designation;

    private String description;

    @NotNull(message = "Le prix d'achat est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix d'achat ne peut pas être négatif")
    private BigDecimal prixAchat;

    @NotNull(message = "Le prix de vente est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix de vente ne peut pas être négatif")
    private BigDecimal prixVente;

    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer quantiteStock = 0;

    @Min(value = 0, message = "Le seuil d'alerte ne peut pas être négatif")
    private Integer seuilAlerte = 5;

    @Size(max = 50)
    private String uniteMesure = "unité";

    private Long categorieId;
}
