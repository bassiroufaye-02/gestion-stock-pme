package com.pme.stock.repository;

import com.pme.stock.entity.LigneCommandeClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneCommandeClientRepository extends JpaRepository<LigneCommandeClient, Long> {

    @Query("SELECT l.produit.id, l.produit.reference, l.produit.designation, " +
           "SUM(l.quantite), SUM(l.montantLigneHT) " +
           "FROM LigneCommandeClient l " +
           "WHERE l.commande.statut <> 'ANNULEE' " +
           "GROUP BY l.produit.id, l.produit.reference, l.produit.designation " +
           "ORDER BY SUM(l.quantite) DESC")
    List<Object[]> topProduitsCommandes(Pageable pageable);
}
