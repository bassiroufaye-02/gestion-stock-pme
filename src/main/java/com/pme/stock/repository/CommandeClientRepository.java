package com.pme.stock.repository;

import com.pme.stock.entity.CommandeClient;
import com.pme.stock.entity.StatutCommande;
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
public interface CommandeClientRepository extends JpaRepository<CommandeClient, Long> {
    Optional<CommandeClient> findByNumeroCommande(String numeroCommande);

    boolean existsByNumeroCommande(String numeroCommande);

    Page<CommandeClient> findByClientId(Long clientId, Pageable pageable);

    Page<CommandeClient> findByStatut(StatutCommande statut, Pageable pageable);

    @Query(
            value = "SELECT c FROM CommandeClient c JOIN FETCH c.client",
            countQuery = "SELECT COUNT(c) FROM CommandeClient c")
    Page<CommandeClient> findAllWithClient(Pageable pageable);

    @Query(
            value = "SELECT c FROM CommandeClient c JOIN FETCH c.client WHERE c.client.id = :clientId",
            countQuery = "SELECT COUNT(c) FROM CommandeClient c WHERE c.client.id = :clientId")
    Page<CommandeClient> findByClientIdWithClient(@Param("clientId") Long clientId, Pageable pageable);

    @Query(
            value = "SELECT c FROM CommandeClient c JOIN FETCH c.client WHERE c.statut = :statut",
            countQuery = "SELECT COUNT(c) FROM CommandeClient c WHERE c.statut = :statut")
    Page<CommandeClient> findByStatutWithClient(@Param("statut") StatutCommande statut, Pageable pageable);

    @Query("SELECT MAX(CAST(SUBSTRING(c.numeroCommande, LENGTH(c.numeroCommande) - 3) AS INTEGER)) FROM CommandeClient c WHERE c.numeroCommande LIKE :prefixe%")
    Integer findMaxSequenceNumber(@Param("prefixe") String prefixe);

    @Query("SELECT c FROM CommandeClient c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.traitePar LEFT JOIN FETCH c.lignes l LEFT JOIN FETCH l.produit WHERE c.id = :id")
    Optional<CommandeClient> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT c FROM CommandeClient c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.traitePar LEFT JOIN FETCH c.lignes l LEFT JOIN FETCH l.produit WHERE c.numeroCommande = :numero")
    Optional<CommandeClient> findByNumeroCommandeWithRelations(@Param("numero") String numero);

    @Query("SELECT c.statut, COUNT(c), COALESCE(SUM(c.montantTTC), 0) " +
           "FROM CommandeClient c GROUP BY c.statut")
    List<Object[]> statistiquesParStatut();

    @Query("SELECT COALESCE(SUM(c.montantTTC), 0) FROM CommandeClient c " +
           "WHERE c.statut = 'LIVREE' AND YEAR(c.dateCommande) = :annee AND MONTH(c.dateCommande) = :mois")
    BigDecimal sumChiffreAffairesMois(@Param("annee") int annee, @Param("mois") int mois);

    @Query("SELECT COUNT(c) FROM CommandeClient c " +
           "WHERE c.statut = 'LIVREE' AND YEAR(c.dateCommande) = :annee AND MONTH(c.dateCommande) = :mois")
    long countCommandesLivreesMois(@Param("annee") int annee, @Param("mois") int mois);
}
