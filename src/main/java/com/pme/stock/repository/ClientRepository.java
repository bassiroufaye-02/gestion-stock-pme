package com.pme.stock.repository;

import com.pme.stock.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Client c WHERE c.actif = true")
    Page<Client> findAllActif(Pageable pageable);

    @Query(
            value = "SELECT c FROM Client c WHERE c.actif = true AND (LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.raisonSociale) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(c) FROM Client c WHERE c.actif = true AND (LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.raisonSociale) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> rechercher(@Param("search") String search, Pageable pageable);
}
