package com.pme.stock.repository;

import com.pme.stock.entity.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    boolean existsByCode(String code);
    boolean existsByEmail(String email);
    boolean existsByCodeAndIdNot(String code, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<Fournisseur> findByCode(String code);
    List<Fournisseur> findAllByActifTrue();

    @Query("SELECT COUNT(cf) > 0 FROM CommandeFournisseur cf WHERE cf.fournisseur.id = :id AND cf.statut IN ('BROUILLON', 'ENVOYEE', 'RECUE_PARTIELLE')")
    boolean hasCommandesEnCours(@Param("id") Long id);

    @Query("SELECT f FROM Fournisseur f WHERE f.actif = true AND " +
           "(LOWER(f.raisonSociale) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Fournisseur> rechercher(@Param("search") String search, Pageable pageable);

    @Query("SELECT f FROM Fournisseur f WHERE " +
           "(LOWER(f.raisonSociale) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(f.code) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Fournisseur> rechercherIncluantInactifs(@Param("search") String search, Pageable pageable);
}
