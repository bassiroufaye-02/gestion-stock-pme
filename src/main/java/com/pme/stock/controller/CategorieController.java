package com.pme.stock.controller;

import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.service.impl.CategorieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Catégories", description = "Gestion des catégories de produits")
@SecurityRequirement(name = "bearerAuth")
public class CategorieController {

    private final CategorieService categorieService;

    // Permet d'ajouter une nouvelle famille de produits pour organiser le catalogue.
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Créer une nouvelle catégorie")
    public ResponseEntity<CategorieResponse> creer(@Valid @RequestBody CategorieRequest request) {
        CategorieResponse cat = categorieService.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cat.getId()).toUri();
        return ResponseEntity.created(location).body(cat);
    }

    // Permet de retrouver rapidement une catégorie via son identifiant pour la consulter ou la modifier.
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une catégorie par son ID")
    public ResponseEntity<CategorieResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(categorieService.trouverParId(id));
    }

    // Permet de voir les catégories actives utilisées par le catalogue pour organiser les produits.
    @GetMapping
    @Operation(summary = "Lister toutes les catégories actives")
    public ResponseEntity<List<CategorieResponse>> listerActives() {
        return ResponseEntity.ok(categorieService.listerActives());
    }

    // Permet à l'administrateur de visualiser l'ensemble des catégories, y compris les désactivées.
    @GetMapping("/toutes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister toutes les catégories — Admin uniquement")
    public ResponseEntity<List<CategorieResponse>> listerToutes() {
        return ResponseEntity.ok(categorieService.listerToutes());
    }

    // Permet de corriger ou de compléter les informations d'une catégorie existante.
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Modifier une catégorie")
    public ResponseEntity<CategorieResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody CategorieRequest request) {
        return ResponseEntity.ok(categorieService.modifier(id, request));
    }

    // Permet de masquer une catégorie sans la supprimer, afin de conserver l'historique et la cohérence du catalogue.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver une catégorie (soft delete)")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        categorieService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
