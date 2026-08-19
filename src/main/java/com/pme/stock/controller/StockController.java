package com.pme.stock.controller;

import com.pme.stock.dto.request.MouvementStockRequest;
import com.pme.stock.dto.response.MouvementStockResponse;
import com.pme.stock.dto.response.PageResponse;
import com.pme.stock.entity.MouvementStock;
import com.pme.stock.mapper.MouvementStockMapper;
import com.pme.stock.service.impl.StockService;
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

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Mouvements de Stock", description = "Entrées, sorties et ajustements de stock")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
public class StockController {

    private final StockService stockService;
    private final MouvementStockMapper mouvementStockMapper;

    // Permet d'enregistrer une entrée, une sortie ou un ajustement de stock pour refléter l'état réel du magasin.
    @PostMapping("/mouvements")
    @Operation(summary = "Enregistrer un mouvement de stock (entrée/sortie/ajustement)")
    public ResponseEntity<MouvementStockResponse> effectuerMouvement(@Valid @RequestBody MouvementStockRequest request) {
        MouvementStock mouvement = stockService.effectuerMouvement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mouvementStockMapper.toResponse(mouvement));
    }

    // Permet de consulter l'historique de stock d'un produit pour suivre ses variations et diagnostiquer les écarts.
    @GetMapping("/mouvements/produit/{produitId}")
    @Operation(summary = "Historique des mouvements d'un produit")
    public ResponseEntity<PageResponse<MouvementStockResponse>> listerMouvements(
            @PathVariable Long produitId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<MouvementStock> mouvements = stockService.listerMouvementsProduit(produitId, pageable);
        Page<MouvementStockResponse> response = mouvements.map(mouvementStockMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(response));
    }
}
