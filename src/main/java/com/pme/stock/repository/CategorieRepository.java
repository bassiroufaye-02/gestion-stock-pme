package com.pme.stock.repository;

import com.pme.stock.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    Optional<Categorie> findByCode(String code);
    boolean existsByCode(String code);
    List<Categorie> findAllByActifTrue();
}
