package com.pme.stock.repository;

import com.pme.stock.entity.Categorie;
import com.pme.stock.entity.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

import com.pme.stock.config.JpaAuditingConfig;
import com.pme.stock.config.AuditorAwareImpl;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class})
@org.springframework.test.context.ContextConfiguration(classes = com.pme.stock.GestionStockPmeApplication.class)
@ActiveProfiles("test")
@DisplayName("ProduitRepository - Tests JPA")
class ProduitRepositoryTest {

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    private Categorie categorie;

    @BeforeEach
    void setUp() {
        produitRepository.deleteAll();
        categorieRepository.deleteAll();

        categorie = categorieRepository.save(Categorie.builder()
                .code("INFO")
                .libelle("Informatique")
                .actif(true)
                .build());

        // 3 produits : 1 OK, 1 en alerte, 1 en rupture
        produitRepository.saveAll(List.of(
            Produit.builder().reference("REF-001").designation("PC Portable")
                    .prixAchat(BigDecimal.TEN).prixVente(BigDecimal.TEN)
                    .quantiteStock(20).seuilAlerte(5).actif(true).categorie(categorie).build(),
            Produit.builder().reference("REF-002").designation("Souris Logitech")
                    .prixAchat(BigDecimal.TEN).prixVente(BigDecimal.TEN)
                    .quantiteStock(3).seuilAlerte(5).actif(true).categorie(categorie).build(),
            Produit.builder().reference("REF-003").designation("Clavier USB")
                    .prixAchat(BigDecimal.TEN).prixVente(BigDecimal.TEN)
                    .quantiteStock(0).seuilAlerte(5).actif(true).categorie(categorie).build()
        ));
    }

    @Test
    @DisplayName("✅ findByReference doit retourner le bon produit")
    void findByReference_existant_doitRetournerProduit() {
        Optional<Produit> result = produitRepository.findByReference("REF-001");

        assertThat(result).isPresent();
        assertThat(result.get().getDesignation()).isEqualTo("PC Portable");
    }

    @Test
    @DisplayName("✅ findByReference inexistant doit retourner Optional vide")
    void findByReference_inexistant_doitRetournerVide() {
        Optional<Produit> result = produitRepository.findByReference("INCONNU");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ existsByReference doit retourner vrai pour une référence existante")
    void existsByReference_existant_doitRetournerVrai() {
        assertThat(produitRepository.existsByReference("REF-001")).isTrue();
        assertThat(produitRepository.existsByReference("INEXISTANT")).isFalse();
    }

    @Test
    @DisplayName("✅ findProduitsEnAlerte doit retourner les produits avec stock <= seuil")
    void findProduitsEnAlerte_doitRetournerProduitsEnAlerte() {
        List<Produit> alertes = produitRepository.findProduitsEnAlerte();

        // REF-002 (stock=3, seuil=5) et REF-003 (stock=0, seuil=5) sont en alerte
        assertThat(alertes).hasSize(2);
        assertThat(alertes).extracting(Produit::getReference)
                .containsExactlyInAnyOrder("REF-002", "REF-003");
    }

    @Test
    @DisplayName("✅ countProduitsEnAlerte doit retourner le bon compte")
    void countProduitsEnAlerte_doitRetournerBonCompte() {
        long count = produitRepository.countProduitsEnAlerte();
        assertThat(count).isEqualTo(2);
    }
}
