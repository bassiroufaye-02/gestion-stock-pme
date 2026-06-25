package com.pme.stock.service.impl;

import com.pme.stock.dto.request.ClientRequest;
import com.pme.stock.dto.response.ClientResponse;
import com.pme.stock.entity.Client;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.ClientMapper;
import com.pme.stock.repository.ClientRepository;
import com.pme.stock.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientResponse creer(ClientRequest request) {
        // Vérifier l'unicité du code
        if (clientRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Un client avec le code '" + request.getCode() + "' existe déjà");
        }
        // Vérifier l'unicité de l'email si fourni
        if (request.getEmail() != null && clientRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un client avec l'email '" + request.getEmail() + "' existe déjà");
        }
        Client client = new Client();
        mapToEntity(request, client);
        client = clientRepository.save(client);
        log.info("Client créé : {} - {}", client.getCode(), client.getRaisonSociale());
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse trouverParId(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id: " + id));
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> listerActifs(Pageable pageable) {
        return clientRepository.findAllActif(pageable).map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> rechercher(String search, Pageable pageable) {
        return clientRepository.rechercher(search, pageable).map(clientMapper::toResponse);
    }

    @Override
    @Transactional
    public ClientResponse modifier(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id: " + id));

        // Vérifier l'unicité du code si modifié
        if (!client.getCode().equals(request.getCode()) && clientRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Le code '" + request.getCode() + "' est déjà utilisé");
        }
        // Vérifier l'unicité de l'email si modifié
        if (request.getEmail() != null && !request.getEmail().equals(client.getEmail()) && clientRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("L'email '" + request.getEmail() + "' est déjà utilisé");
        }

        mapToEntity(request, client);
        client = clientRepository.save(client);
        log.info("Client modifié : {} - {}", client.getCode(), client.getRaisonSociale());
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public void desactiver(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id: " + id));
        client.setActif(false);
        clientRepository.save(client);
        log.info("Client désactivé : {} - {}", client.getCode(), client.getRaisonSociale());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> listerTous(Pageable pageable) {
        return clientRepository.findAllIncludingInactifs(pageable).map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> rechercherTous(String search, Pageable pageable) {
        return clientRepository.rechercherIncluantInactifs(search, pageable).map(clientMapper::toResponse);
    }

    @Override
    @Transactional
    public ClientResponse reactiver(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id: " + id));
        client.setActif(true);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    private void mapToEntity(ClientRequest request, Client client) {
        client.setCode(request.getCode());
        client.setRaisonSociale(request.getRaisonSociale());
        client.setEmail(request.getEmail());
        client.setTelephone(request.getTelephone());
        client.setAdresse(request.getAdresse());
        client.setVille(request.getVille());
    }
}
