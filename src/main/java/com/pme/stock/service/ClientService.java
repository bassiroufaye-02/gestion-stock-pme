package com.pme.stock.service;

import com.pme.stock.dto.request.ClientRequest;
import com.pme.stock.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
    ClientResponse creer(ClientRequest request);
    ClientResponse trouverParId(Long id);
    Page<ClientResponse> listerActifs(Pageable pageable);
    ClientResponse modifier(Long id, ClientRequest request);
    void desactiver(Long id);
    Page<ClientResponse> rechercher(String search, Pageable pageable);
    Page<ClientResponse> listerTous(Pageable pageable);
    Page<ClientResponse> rechercherTous(String search, Pageable pageable);
    ClientResponse reactiver(Long id);
}
