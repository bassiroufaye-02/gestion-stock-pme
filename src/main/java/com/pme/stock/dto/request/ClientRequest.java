package com.pme.stock.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String raisonSociale;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
}
