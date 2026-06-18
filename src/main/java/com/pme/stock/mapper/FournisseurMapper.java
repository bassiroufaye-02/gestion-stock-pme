package com.pme.stock.mapper;

import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.response.FournisseurResponse;
import com.pme.stock.entity.Fournisseur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FournisseurMapper {

    @Mapping(target = "nombreCommandes",
             expression = "java(entity.getCommandes() != null ? entity.getCommandes().size() : 0)")
    @Mapping(target = "nombreProduits",
             expression = "java(entity.getProduits() != null ? entity.getProduits().size() : 0)")
    FournisseurResponse toResponse(Fournisseur entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "commandes", ignore = true)
    @Mapping(target = "produits", ignore = true)
    Fournisseur toEntity(FournisseurRequest request);

    List<FournisseurResponse> toResponseList(List<Fournisseur> entities);
}
