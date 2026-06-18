package com.pme.stock.mapper;

import com.pme.stock.dto.response.CommandeFournisseurResponse;
import com.pme.stock.dto.response.LigneCommandeFournisseurResponse;
import com.pme.stock.entity.CommandeFournisseur;
import com.pme.stock.entity.LigneCommandeFournisseur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommandeFournisseurMapper {

    @Mapping(target = "fournisseurId",
             expression = "java(entity.getFournisseur() != null ? entity.getFournisseur().getId() : null)")
    @Mapping(target = "fournisseurRaisonSociale",
             expression = "java(entity.getFournisseur() != null ? entity.getFournisseur().getRaisonSociale() : null)")
    @Mapping(target = "creePar",
             expression = "java(entity.getCreePar() != null ? entity.getCreePar().getEmail() : null)")
    @Mapping(target = "montantHT",
             expression = "java(entity.getMontantHT() != null ? entity.getMontantHT() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "montantTVA",
             expression = "java(entity.getMontantTVA() != null ? entity.getMontantTVA() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "montantTTC",
             expression = "java(entity.getMontantTTC() != null ? entity.getMontantTTC() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "tauxTVA",
             expression = "java(entity.getTauxTVA() != null ? entity.getTauxTVA() : new java.math.BigDecimal(\"18.00\"))")
    CommandeFournisseurResponse toResponse(CommandeFournisseur entity);

    @Mapping(target = "produitId",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getId() : null)")
    @Mapping(target = "produitReference",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getReference() : null)")
    @Mapping(target = "produitDesignation",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getDesignation() : null)")
    @Mapping(target = "receptionComplete",
             expression = "java(entity.estTotalementRecue())")
    LigneCommandeFournisseurResponse toLigneResponse(LigneCommandeFournisseur entity);

    List<CommandeFournisseurResponse> toResponseList(List<CommandeFournisseur> entities);
}
