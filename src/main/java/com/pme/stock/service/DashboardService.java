package com.pme.stock.service;

import com.pme.stock.dto.response.ChiffreAffairesMoisResponse;
import com.pme.stock.dto.response.CommandeStatutStatResponse;
import com.pme.stock.dto.response.DashboardResponse;
import com.pme.stock.dto.response.ProduitTopVenteResponse;
import com.pme.stock.dto.response.StockValeurResponse;

import java.util.List;

public interface DashboardService {

    DashboardResponse genererDashboard();

    StockValeurResponse calculerValeurStock();

    List<CommandeStatutStatResponse> statistiquesCommandesParStatut();

    List<ProduitTopVenteResponse> topProduitsVendus(int limite);

    ChiffreAffairesMoisResponse chiffreAffairesMois(int annee, int mois);
}
