package com.pme.stock.mapper;

import com.pme.stock.dto.response.CommandeClientResponse;
import com.pme.stock.dto.response.LigneCommandeResponse;
import com.pme.stock.entity.CommandeClient;
import com.pme.stock.entity.LigneCommandeClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommandeClientMapper {

    @Mapping(target = "clientId",
             expression = "java(entity.getClient() != null ? entity.getClient().getId() : null)")
    @Mapping(target = "clientRaisonSociale",
             expression = "java(entity.getClient() != null ? entity.getClient().getRaisonSociale() : null)")
    @Mapping(target = "traitePar",
             expression = "java(entity.getTraitePar() != null ? entity.getTraitePar().getEmail() : null)")
    @Mapping(target = "montantHT",
             expression = "java(entity.getMontantHT() != null ? entity.getMontantHT() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "montantTVA",
             expression = "java(entity.getMontantTVA() != null ? entity.getMontantTVA() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "montantTTC",
             expression = "java(entity.getMontantTTC() != null ? entity.getMontantTTC() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "tauxTVA",
             expression = "java(entity.getTauxTVA() != null ? entity.getTauxTVA() : new java.math.BigDecimal(\"18.00\"))")
    CommandeClientResponse toResponse(CommandeClient entity);

    @Mapping(target = "produitId",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getId() : null)")
    @Mapping(target = "produitReference",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getReference() : null)")
    @Mapping(target = "produitDesignation",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getDesignation() : null)")
    @Mapping(target = "prixUnitaireHT",
             expression = "java(entity.getPrixUnitaireHT() != null ? entity.getPrixUnitaireHT() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "montantLigneHT",
             expression = "java(entity.getMontantLigneHT() != null ? entity.getMontantLigneHT() : java.math.BigDecimal.ZERO)")
    LigneCommandeResponse toLigneResponse(LigneCommandeClient entity);

    List<CommandeClientResponse> toResponseList(List<CommandeClient> entities);
}
