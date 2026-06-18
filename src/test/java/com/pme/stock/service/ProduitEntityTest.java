package com.pme.stock.service;

import com.pme.stock.entity.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Produit - Tests des méthodes métier de l'entité")
class ProduitEntityTest {

    private Produit produit;

    @BeforeEach
    void setUp() {
        produit = Produit.builder()
                .id(1L).reference("REF-001").designation("Test")
                .prixAchat(BigDecimal.TEN).prixVente(BigDecimal.TEN)
                .quantiteStock(10).seuilAlerte(5).actif(true).build();
    }

    @Test
    @DisplayName("✅ incrementerStock doit augmenter le stock")
    void incrementerStock_quantitePositive_doitAugmenterStock() {
        produit.incrementerStock(5);
        assertThat(produit.getQuantiteStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("❌ incrementerStock avec 0 doit lever IllegalArgumentException")
    void incrementerStock_quantiteZero_doitLeverException() {
        assertThatThrownBy(() -> produit.incrementerStock(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("✅ decrementerStock doit réduire le stock")
    void decrementerStock_quantiteSuffisante_doitReduireStock() {
        produit.decrementerStock(3);
        assertThat(produit.getQuantiteStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("❌ decrementerStock au-delà du stock doit lever IllegalStateException")
    void decrementerStock_stockInsuffisant_doitLeverException() {
        assertThatThrownBy(() -> produit.decrementerStock(20))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("insuffisant");
    }

    @Test
    @DisplayName("✅ decrementerStock exact doit passer le stock à 0")
    void decrementerStock_exact_doitPasserStockAZero() {
        produit.decrementerStock(10);
        assertThat(produit.getQuantiteStock()).isEqualTo(0);
        assertThat(produit.isEnRupture()).isTrue();
    }

    @Test
    @DisplayName("✅ isEnRuptureAlerte vrai si stock <= seuil")
    void isEnRuptureAlerte_stockInferieurOuEgalSeuil_doitRetournerVrai() {
        produit.setQuantiteStock(5);
        assertThat(produit.isEnRuptureAlerte()).isTrue();
        produit.setQuantiteStock(3);
        assertThat(produit.isEnRuptureAlerte()).isTrue();
    }

    @Test
    @DisplayName("✅ isEnRuptureAlerte faux si stock > seuil")
    void isEnRuptureAlerte_stockSuperieurSeuil_doitRetournerFaux() {
        produit.setQuantiteStock(6);
        assertThat(produit.isEnRuptureAlerte()).isFalse();
    }

    @Test
    @DisplayName("✅ isEnRupture vrai si stock == 0")
    void isEnRupture_stockZero_doitRetournerVrai() {
        produit.setQuantiteStock(0);
        assertThat(produit.isEnRupture()).isTrue();
    }
}
