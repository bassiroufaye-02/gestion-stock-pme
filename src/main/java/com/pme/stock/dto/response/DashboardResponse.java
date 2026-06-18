package com.pme.stock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    @Schema(description = "Date/heure de génération du snapshot")
    private LocalDateTime genereLe;

    private StockValeurResponse stock;

    @Schema(description = "Liste des produits en alerte de stock (quantité <= seuil)")
    private List<ProduitResponse> produitsEnAlerte;

    @Schema(description = "Nombre de produits en rupture totale (quantité = 0)")
    private long nombreProduitsEnRupture;

    @Schema(description = "Répartition des commandes par statut")
    private List<CommandeStatutStatResponse> commandesParStatut;

    @Schema(description = "Top 5 des produits les plus commandés")
    private List<ProduitTopVenteResponse> topProduits;

    private ChiffreAffairesMoisResponse chiffreAffairesMoisCourant;

    @Schema(description = "Nombre de fournisseurs actifs (0 si module fournisseur absent)")
    private long nombreFournisseursActifs;

    @Schema(description = "Nombre de commandes fournisseurs en attente de réception")
    private long nombreCommandesFournisseursEnAttente;
}
