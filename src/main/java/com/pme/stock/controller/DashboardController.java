package com.pme.stock.controller;

import com.pme.stock.dto.response.ChiffreAffairesMoisResponse;
import com.pme.stock.dto.response.CommandeStatutStatResponse;
import com.pme.stock.dto.response.DashboardResponse;
import com.pme.stock.dto.response.ProduitTopVenteResponse;
import com.pme.stock.dto.response.StockValeurResponse;
import com.pme.stock.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "API de statistiques et tableau de bord pour la PME")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer le snapshot complet du tableau de bord",
            description = "Retourne en un seul appel tous les indicateurs clés : valeur du stock, " +
                    "alertes, répartition des commandes par statut, top produits, CA du mois.")
    @ApiResponse(responseCode = "200", description = "Snapshot du dashboard généré avec succès")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.genererDashboard());
    }

    @GetMapping("/stock/valeur")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Valeur totale du stock",
            description = "Calcule la valeur du stock au prix d'achat et au prix de vente, et la marge potentielle.")
    @ApiResponse(responseCode = "200", description = "Valeur du stock calculée")
    public ResponseEntity<StockValeurResponse> getValeurStock() {
        return ResponseEntity.ok(dashboardService.calculerValeurStock());
    }

    @GetMapping("/commandes/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Statistiques des commandes par statut",
            description = "Retourne le nombre et le montant total TTC des commandes, groupés par statut.")
    @ApiResponse(responseCode = "200", description = "Statistiques calculées")
    public ResponseEntity<List<CommandeStatutStatResponse>> getStatsCommandes() {
        return ResponseEntity.ok(dashboardService.statistiquesCommandesParStatut());
    }

    @GetMapping("/produits/top")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Top produits les plus commandés",
            description = "Retourne les produits les plus commandés (hors commandes annulées), triés par quantité totale décroissante.")
    @ApiResponse(responseCode = "200", description = "Top produits calculé")
    public ResponseEntity<List<ProduitTopVenteResponse>> getTopProduits(
            @RequestParam(name = "limite", defaultValue = "5")
            @Parameter(description = "Nombre de produits à retourner", example = "5") int limite) {
        return ResponseEntity.ok(dashboardService.topProduitsVendus(limite));
    }

    @GetMapping("/ca/mensuel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Chiffre d'affaires d'un mois donné",
            description = "Calcule le CA TTC des commandes livrées pour une année/mois donnés. " +
                    "Par défaut, retourne le mois courant si aucun paramètre n'est fourni.")
    @ApiResponse(responseCode = "200", description = "Chiffre d'affaires calculé")
    public ResponseEntity<ChiffreAffairesMoisResponse> getChiffreAffairesMois(
            @RequestParam(required = false) @Parameter(description = "Année", example = "2026") Integer annee,
            @RequestParam(required = false) @Parameter(description = "Mois (1-12)", example = "6") Integer mois) {
        LocalDate now = LocalDate.now();
        int a = (annee != null) ? annee : now.getYear();
        int m = (mois != null) ? mois : now.getMonthValue();
        return ResponseEntity.ok(dashboardService.chiffreAffairesMois(a, m));
    }
}
