package com.pme.stock.repository;

import com.pme.stock.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    Optional<Produit> findByReference(String reference);

    boolean existsByReference(String reference);

    // Produits en alerte de stock (quantite <= seuil)
    @Query("SELECT p FROM Produit p WHERE p.actif = true AND p.quantiteStock <= p.seuilAlerte ORDER BY p.quantiteStock ASC")
    List<Produit> findProduitsEnAlerte();

    // Recherche par désignation ou référence avec pagination
    @Query("SELECT p FROM Produit p WHERE p.actif = true AND (LOWER(p.designation) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Produit> rechercherProduits(@Param("search") String search, Pageable pageable);

    // Produits par catégorie
    @Query("SELECT p FROM Produit p JOIN FETCH p.categorie c WHERE p.actif = true AND c.id = :categorieId")
    Page<Produit> findByCategorieId(@Param("categorieId") Long categorieId, Pageable pageable);

    // Nombre de produits en alerte
    @Query("SELECT COUNT(p) FROM Produit p WHERE p.actif = true AND p.quantiteStock <= p.seuilAlerte")
    long countProduitsEnAlerte();

    @Query("SELECT COALESCE(SUM(p.quantiteStock * p.prixAchat), 0) FROM Produit p WHERE p.actif = true")
    BigDecimal sumValeurStockAchat();

    @Query("SELECT COALESCE(SUM(p.quantiteStock * p.prixVente), 0) FROM Produit p WHERE p.actif = true")
    BigDecimal sumValeurStockVente();

    @Query("SELECT COALESCE(SUM(p.quantiteStock), 0) FROM Produit p WHERE p.actif = true")
    long sumQuantiteTotaleStock();

    long countByActifTrue();

    @Query("SELECT COUNT(p) FROM Produit p WHERE p.actif = true AND p.quantiteStock = 0")
    long countProduitsEnRupture();
}
