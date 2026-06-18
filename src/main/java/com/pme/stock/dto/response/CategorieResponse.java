package com.pme.stock.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategorieResponse {
    private Long id;
    private String code;
    private String libelle;
    private String description;
    private Boolean actif;
    private int nombreProduits;
    private LocalDateTime createdAt;
}
