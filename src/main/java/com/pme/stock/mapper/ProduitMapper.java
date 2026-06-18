package com.pme.stock.mapper;

import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.entity.Produit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProduitMapper {

    @Mapping(target = "enAlerte", expression = "java(entity.isEnRuptureAlerte())")
    @Mapping(target = "enRupture", expression = "java(entity.isEnRupture())")
    @Mapping(target = "categorieId",
             expression = "java(entity.getCategorie() != null ? entity.getCategorie().getId() : null)")
    @Mapping(target = "categorieLibelle",
             expression = "java(entity.getCategorie() != null ? entity.getCategorie().getLibelle() : null)")
    ProduitResponse toResponse(Produit entity);

    /**
     * Mappe une ProduitRequest vers un Produit.
     * Les champs hérités de BaseEntity ne sont pas dans le builder Lombok.
     * Le champ categorie est géré manuellement dans le service (lookup par ID).
     * Les champs actif et id sont gérés séparément.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "categorie", ignore = true)
    Produit toEntity(ProduitRequest request);

    List<ProduitResponse> toResponseList(List<Produit> entities);
}
