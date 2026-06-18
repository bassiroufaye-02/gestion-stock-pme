package com.pme.stock.mapper;

import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.entity.Categorie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategorieMapper {

    @Mapping(target = "nombreProduits",
             expression = "java(entity.getProduits() != null ? entity.getProduits().size() : 0)")
    CategorieResponse toResponse(Categorie entity);

    /**
     * Mappe une CategorieRequest vers une Categorie.
     * Les champs gérés automatiquement (id, actif, produits) et les champs hérités
     * de BaseEntity ne sont pas dans le builder Lombok, ils sont ignorés explicitement.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "produits", ignore = true)
    Categorie toEntity(CategorieRequest request);

    List<CategorieResponse> toResponseList(List<Categorie> entities);
}
