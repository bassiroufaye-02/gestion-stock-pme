package com.pme.stock.mapper;

import com.pme.stock.dto.response.MouvementStockResponse;
import com.pme.stock.entity.MouvementStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MouvementStockMapper {

    @Mapping(target = "produitId",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getId() : null)")
    @Mapping(target = "produitReference",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getReference() : null)")
    @Mapping(target = "produitDesignation",
             expression = "java(entity.getProduit() != null ? entity.getProduit().getDesignation() : null)")
    @Mapping(target = "utilisateurEmail",
             expression = "java(entity.getUtilisateur() != null ? entity.getUtilisateur().getEmail() : null)")
    MouvementStockResponse toResponse(MouvementStock entity);

    List<MouvementStockResponse> toResponseList(List<MouvementStock> entities);
}
