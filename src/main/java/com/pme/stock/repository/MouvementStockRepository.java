package com.pme.stock.repository;

import com.pme.stock.entity.MouvementStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
    @Query(value = "SELECT m FROM MouvementStock m " +
                   "LEFT JOIN FETCH m.produit " +
                   "LEFT JOIN FETCH m.utilisateur " +
                   "WHERE m.produit.id = :produitId",
           countQuery = "SELECT COUNT(m) FROM MouvementStock m WHERE m.produit.id = :produitId")
    Page<MouvementStock> findByProduitId(@Param("produitId") Long produitId, Pageable pageable);
}
