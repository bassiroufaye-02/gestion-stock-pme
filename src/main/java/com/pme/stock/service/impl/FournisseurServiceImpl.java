package com.pme.stock.service.impl;

import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.response.FournisseurResponse;
import com.pme.stock.entity.Fournisseur;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.FournisseurMapper;
import com.pme.stock.repository.FournisseurRepository;
import com.pme.stock.service.FournisseurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FournisseurServiceImpl implements FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final FournisseurMapper fournisseurMapper;

    @Override
    @Transactional
    public FournisseurResponse creer(FournisseurRequest request) {
        String code = request.getCode().toUpperCase().trim();
        if (fournisseurRepository.existsByCode(code)) {
            throw new BusinessException("Un fournisseur avec le code '" + code + "' existe déjà");
        }
        if (request.getEmail() != null && fournisseurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un fournisseur avec l'email '" + request.getEmail() + "' existe déjà");
        }

        Fournisseur fournisseur = fournisseurMapper.toEntity(request);
        fournisseur.setCode(code);
        fournisseur.setActif(true);

        fournisseur = fournisseurRepository.save(fournisseur);
        log.info("Fournisseur créé : {} - {}", fournisseur.getCode(), fournisseur.getRaisonSociale());
        return fournisseurMapper.toResponse(fournisseur);
    }

    @Override
    @Transactional
    public FournisseurResponse modifier(Long id, FournisseurRequest request) {
        Fournisseur fournisseur = trouverOuException(id);
        String code = request.getCode().toUpperCase().trim();

        if (fournisseurRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessException("Un fournisseur avec le code '" + code + "' existe déjà");
        }
        if (request.getEmail() != null && fournisseurRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException("Un fournisseur avec l'email '" + request.getEmail() + "' existe déjà");
        }

        fournisseur.setCode(code);
        fournisseur.setRaisonSociale(request.getRaisonSociale());
        fournisseur.setEmail(request.getEmail());
        fournisseur.setTelephone(request.getTelephone());
        fournisseur.setAdresse(request.getAdresse());
        fournisseur.setVille(request.getVille());
        fournisseur.setPays(request.getPays() != null ? request.getPays() : "Sénégal");

        fournisseur = fournisseurRepository.save(fournisseur);
        log.info("Fournisseur modifié : {} - {}", fournisseur.getCode(), fournisseur.getRaisonSociale());
        return fournisseurMapper.toResponse(fournisseur);
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurResponse trouverParId(Long id) {
        return fournisseurMapper.toResponse(trouverOuException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurResponse trouverParCode(String code) {
        Fournisseur fournisseur = fournisseurRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur avec code " + code));
        return fournisseurMapper.toResponse(fournisseur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FournisseurResponse> listerActifs() {
        return fournisseurRepository.findAllByActifTrue().stream()
                .map(fournisseurMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FournisseurResponse> rechercher(String search, Pageable pageable) {
        return fournisseurRepository.rechercher(search, pageable)
                .map(fournisseurMapper::toResponse);
    }

    @Override
    @Transactional
    public void desactiver(Long id) {
        Fournisseur fournisseur = trouverOuException(id);
        if (fournisseurRepository.hasCommandesEnCours(id)) {
            throw new BusinessException("Impossible de désactiver : des commandes sont en cours");
        }
        fournisseur.setActif(false);
        fournisseurRepository.save(fournisseur);
        log.info("Fournisseur désactivé : {} - {}", fournisseur.getCode(), fournisseur.getRaisonSociale());
    }

    private Fournisseur trouverOuException(Long id) {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", id));
    }
}
