package com.pme.stock.service.impl;

import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.entity.Categorie;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.CategorieMapper;
import com.pme.stock.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final CategorieMapper categorieMapper;

    @Transactional
    public CategorieResponse creer(CategorieRequest request) {
        if (categorieRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Une catégorie avec le code '" + request.getCode() + "' existe déjà");
        }
        Categorie categorie = Categorie.builder()
                .code(request.getCode().toUpperCase())
                .libelle(request.getLibelle())
                .description(request.getDescription())
                .build();
        return categorieMapper.toResponse(categorieRepository.save(categorie));
    }

    @Transactional
    public CategorieResponse modifier(Long id, CategorieRequest request) {
        Categorie categorie = trouverOuException(id);
        categorie.setLibelle(request.getLibelle());
        categorie.setDescription(request.getDescription());
        return categorieMapper.toResponse(categorieRepository.save(categorie));
    }

    @Transactional(readOnly = true)
    public CategorieResponse trouverParId(Long id) {
        return categorieMapper.toResponse(trouverOuException(id));
    }

    @Transactional(readOnly = true)
    public List<CategorieResponse> listerActives() {
        return categorieMapper.toResponseList(categorieRepository.findAllByActifTrue());
    }

    @Transactional(readOnly = true)
    public List<CategorieResponse> listerToutes() {
        return categorieMapper.toResponseList(categorieRepository.findAll());
    }

    @Transactional
    public void desactiver(Long id) {
        Categorie categorie = trouverOuException(id);
        categorie.setActif(false);
        categorieRepository.save(categorie);
    }

    private Categorie trouverOuException(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie", id));
    }
}
