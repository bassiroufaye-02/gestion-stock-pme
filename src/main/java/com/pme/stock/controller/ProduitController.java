package com.pme.stock.controller;

import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.service.ProduitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
@Tag(name = "Produits", description = "Gestion du catalogue produits")
@SecurityRequirement(name = "bearerAuth")
public class ProduitController {

    private final ProduitService produitService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Créer un nouveau produit")
    public ResponseEntity<ProduitResponse> creer(@Valid @RequestBody ProduitRequest request) {
        ProduitResponse produit = produitService.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(produit.getId()).toUri();
        return ResponseEntity.created(location).body(produit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un produit par son ID")
    public ResponseEntity<ProduitResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.trouverParId(id));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Récupérer un produit par sa référence")
    public ResponseEntity<ProduitResponse> trouverParReference(@PathVariable String reference) {
        return ResponseEntity.ok(produitService.trouverParReference(reference));
    }

    @GetMapping
    @Operation(summary = "Lister tous les produits (paginé)")
    public ResponseEntity<Page<ProduitResponse>> listerTous(
            @PageableDefault(size = 20, sort = "designation") Pageable pageable) {
        return ResponseEntity.ok(produitService.listerTous(pageable));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des produits par désignation ou référence")
    public ResponseEntity<Page<ProduitResponse>> rechercher(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(produitService.rechercher(q, pageable));
    }

    @GetMapping("/categorie/{categorieId}")
    @Operation(summary = "Lister les produits d'une catégorie")
    public ResponseEntity<Page<ProduitResponse>> listerParCategorie(
            @PathVariable Long categorieId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(produitService.listerParCategorie(categorieId, pageable));
    }

    @GetMapping("/alertes")
    @Operation(summary = "Lister les produits en alerte de stock")
    public ResponseEntity<List<ProduitResponse>> listerAlertes() {
        return ResponseEntity.ok(produitService.listerProduitsEnAlerte());
    }

    @GetMapping("/alertes/count")
    @Operation(summary = "Nombre de produits en alerte de stock")
    public ResponseEntity<Map<String, Long>> compterAlertes() {
        return ResponseEntity.ok(Map.of("count", produitService.compterProduitsEnAlerte()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Modifier un produit")
    public ResponseEntity<ProduitResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver un produit (soft delete)")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        produitService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
