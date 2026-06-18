package com.pme.stock.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientResponse {
    private Long id;
    private String code;
    private String raisonSociale;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private Boolean actif;
    private LocalDateTime createdAt;
}
