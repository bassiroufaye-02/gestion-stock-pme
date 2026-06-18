package com.pme.stock.repository;

import com.pme.stock.entity.CommandeFournisseur;
import com.pme.stock.entity.StatutCommandeFournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {

    Optional<CommandeFournisseur> findByNumeroCommande(String numero);

    @Query("SELECT c FROM CommandeFournisseur c LEFT JOIN FETCH c.fournisseur LEFT JOIN FETCH c.lignes l LEFT JOIN FETCH l.produit WHERE c.id = :id")
    Optional<CommandeFournisseur> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT c FROM CommandeFournisseur c LEFT JOIN FETCH c.fournisseur ORDER BY c.dateCommande DESC")
    Page<CommandeFournisseur> findAllWithFournisseur(Pageable pageable);

    @Query("SELECT c FROM CommandeFournisseur c LEFT JOIN FETCH c.fournisseur WHERE c.fournisseur.id = :fournisseurId")
    Page<CommandeFournisseur> findByFournisseurId(@Param("fournisseurId") Long fournisseurId, Pageable pageable);

    @Query("SELECT c FROM CommandeFournisseur c LEFT JOIN FETCH c.fournisseur WHERE c.statut = :statut")
    Page<CommandeFournisseur> findByStatut(@Param("statut") StatutCommandeFournisseur statut, Pageable pageable);

    @Query("SELECT MAX(CAST(SUBSTRING(c.numeroCommande, 9) AS int)) FROM CommandeFournisseur c WHERE c.numeroCommande LIKE CONCAT(:prefixe, '%')")
    Integer findMaxSequenceNumber(@Param("prefixe") String prefixe);
}
