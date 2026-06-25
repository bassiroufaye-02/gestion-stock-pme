package com.pme.stock.service;

import com.pme.stock.dto.request.ClientRequest;
import com.pme.stock.dto.response.ClientResponse;
import com.pme.stock.entity.Client;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.exception.ResourceNotFoundException;
import com.pme.stock.mapper.ClientMapper;
import com.pme.stock.repository.ClientRepository;
import com.pme.stock.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService - Tests unitaires")
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client clientExistant;
    private ClientRequest request;
    private ClientResponse clientResponse;

    @BeforeEach
    void setUp() {
        clientExistant = new Client();
        clientExistant.setId(1L);
        clientExistant.setCode("CLI-001");
        clientExistant.setRaisonSociale("ACME Corp");
        clientExistant.setEmail("contact@acme.com");
        clientExistant.setTelephone("0600000000");
        clientExistant.setAdresse("1 rue de la Paix");
        clientExistant.setVille("Paris");
        clientExistant.setActif(true);

        request = new ClientRequest();
        request.setCode("CLI-001");
        request.setRaisonSociale("ACME Corp");
        request.setEmail("contact@acme.com");
        request.setTelephone("0600000000");
        request.setAdresse("1 rue de la Paix");
        request.setVille("Paris");

        clientResponse = new ClientResponse();
        clientResponse.setId(1L);
        clientResponse.setCode("CLI-001");
        clientResponse.setRaisonSociale("ACME Corp");
        clientResponse.setEmail("contact@acme.com");
        clientResponse.setActif(true);
    }

    // =====================================================================
    // Tests de création
    // =====================================================================

    @Nested
    @DisplayName("creer()")
    class CreerTests {

        @Test
        @DisplayName("✅ succès — client sauvegardé, response avec code correct")
        void creer_casNominal_doitRetournerResponse() {
            // GIVEN
            given(clientRepository.existsByCode("CLI-001")).willReturn(false);
            given(clientRepository.existsByEmail("contact@acme.com")).willReturn(false);
            given(clientRepository.save(any(Client.class))).willReturn(clientExistant);
            given(clientMapper.toResponse(clientExistant)).willReturn(clientResponse);

            // WHEN
            ClientResponse response = clientService.creer(request);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo("CLI-001");
            assertThat(response.getRaisonSociale()).isEqualTo("ACME Corp");
            then(clientRepository).should(times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ doublon code — doit lever BusinessException avec 'code'")
        void creer_codeDoublon_doitLeverBusinessException() {
            // GIVEN
            given(clientRepository.existsByCode("CLI-001")).willReturn(true);

            // WHEN / THEN
            assertThatThrownBy(() -> clientService.creer(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CLI-001");
            then(clientRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("❌ doublon email — doit lever BusinessException avec 'email'")
        void creer_emailDoublon_doitLeverBusinessException() {
            // GIVEN
            given(clientRepository.existsByCode("CLI-001")).willReturn(false);
            given(clientRepository.existsByEmail("contact@acme.com")).willReturn(true);

            // WHEN / THEN
            assertThatThrownBy(() -> clientService.creer(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("contact@acme.com");
            then(clientRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("✅ sans email — existsByEmail non appelé")
        void creer_sansEmail_pasDeVerificationEmail() {
            // GIVEN
            request.setEmail(null);
            given(clientRepository.existsByCode("CLI-001")).willReturn(false);
            given(clientRepository.save(any(Client.class))).willReturn(clientExistant);
            given(clientMapper.toResponse(any(Client.class))).willReturn(clientResponse);

            // WHEN
            clientService.creer(request);

            // THEN
            then(clientRepository).should(never()).existsByEmail(anyString());
        }
    }

    // =====================================================================
    // Tests de modification
    // =====================================================================

    @Nested
    @DisplayName("modifier()")
    class ModifierTests {

        @Test
        @DisplayName("✅ succès — modification réussie, response retournée")
        void modifier_casNominal_doitReussir() {
            // GIVEN
            given(clientRepository.findById(1L)).willReturn(Optional.of(clientExistant));
            given(clientRepository.save(any(Client.class))).willReturn(clientExistant);
            given(clientMapper.toResponse(any(Client.class))).willReturn(clientResponse);

            // WHEN
            ClientResponse response = clientService.modifier(1L, request);

            // THEN
            assertThat(response).isNotNull();
            then(clientRepository).should(times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ nouveau code déjà utilisé — doit lever BusinessException")
        void modifier_codeDoublon_doitLeverBusinessException() {
            // GIVEN
            request.setCode("CLI-002"); // Code différent de l'existant
            given(clientRepository.findById(1L)).willReturn(Optional.of(clientExistant));
            given(clientRepository.existsByCode("CLI-002")).willReturn(true);

            // WHEN / THEN
            assertThatThrownBy(() -> clientService.modifier(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CLI-002");
            then(clientRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("❌ nouvel email déjà utilisé — doit lever BusinessException")
        void modifier_emailDoublon_doitLeverBusinessException() {
            // GIVEN
            request.setEmail("nouveau@test.com"); // Email différent de l'existant
            given(clientRepository.findById(1L)).willReturn(Optional.of(clientExistant));
            given(clientRepository.existsByEmail("nouveau@test.com")).willReturn(true);

            // WHEN / THEN
            assertThatThrownBy(() -> clientService.modifier(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nouveau@test.com");
            then(clientRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void modifier_idInexistant_doitLeverResourceNotFoundException() {
            // GIVEN
            given(clientRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> clientService.modifier(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =====================================================================
    // Tests de recherche
    // =====================================================================

    @Nested
    @DisplayName("trouverParId()")
    class TrouverParIdTests {

        @Test
        @DisplayName("✅ id existant — retourne la response correcte")
        void trouverParId_existant_doitRetournerResponse() {
            // GIVEN
            given(clientRepository.findById(1L)).willReturn(Optional.of(clientExistant));
            given(clientMapper.toResponse(clientExistant)).willReturn(clientResponse);

            // WHEN
            ClientResponse response = clientService.trouverParId(1L);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getCode()).isEqualTo("CLI-001");
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void trouverParId_inexistant_doitLeverResourceNotFoundException() {
            // GIVEN
            given(clientRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> clientService.trouverParId(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =====================================================================
    // Tests de désactivation
    // =====================================================================

    @Nested
    @DisplayName("desactiver()")
    class DesactiverTests {

        @Test
        @DisplayName("✅ existant — actif = false puis sauvegardé")
        void desactiver_existant_doitSetActifFalse() {
            // GIVEN
            given(clientRepository.findById(1L)).willReturn(Optional.of(clientExistant));

            // WHEN
            clientService.desactiver(1L);

            // THEN
            assertThat(clientExistant.getActif()).isFalse();
            then(clientRepository).should(times(1)).save(argThat(c -> !c.getActif()));
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void desactiver_idInexistant_doitLeverResourceNotFoundException() {
            // GIVEN
            given(clientRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> clientService.desactiver(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
            then(clientRepository).should(never()).save(any());
        }
    }

    // =====================================================================
    // Tests de lister tous
    // =====================================================================

    @Nested
    @DisplayName("listerTous()")
    class ListerTousTests {

        @Test
        @DisplayName("✅ cas nominal — retourne tous les clients actifs et inactifs")
        void listerTous_casNominal_doitRetournerTousLesClients() {
            // GIVEN
            Client clientInactif = new Client();
            clientInactif.setId(2L);
            clientInactif.setCode("CLI-002");
            clientInactif.setRaisonSociale("Inactive Client");
            clientInactif.setActif(false);

            Page<Client> page = new PageImpl<>(List.of(clientExistant, clientInactif));
            given(clientRepository.findAllIncludingInactifs(PageRequest.of(0, 20))).willReturn(page);
            given(clientMapper.toResponse(any(Client.class))).willReturn(clientResponse);

            // WHEN
            var result = clientService.listerTous(PageRequest.of(0, 20));

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            then(clientRepository).should(times(1)).findAllIncludingInactifs(PageRequest.of(0, 20));
        }
    }

    // =====================================================================
    // Tests de rechercher tous
    // =====================================================================

    @Nested
    @DisplayName("rechercherTous()")
    class RechercherTousTests {

        @Test
        @DisplayName("✅ cas nominal — recherche tous les clients actifs et inactifs")
        void rechercherTous_casNominal_doitRetournerResultats() {
            // GIVEN
            Client clientInactif = new Client();
            clientInactif.setId(2L);
            clientInactif.setCode("CLI-002");
            clientInactif.setRaisonSociale("Inactive ACME");
            clientInactif.setActif(false);

            Page<Client> page = new PageImpl<>(List.of(clientExistant, clientInactif));
            given(clientRepository.rechercherIncluantInactifs("ACME", PageRequest.of(0, 20))).willReturn(page);
            given(clientMapper.toResponse(any(Client.class))).willReturn(clientResponse);

            // WHEN
            var result = clientService.rechercherTous("ACME", PageRequest.of(0, 20));

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            then(clientRepository).should(times(1)).rechercherIncluantInactifs("ACME", PageRequest.of(0, 20));
        }
    }

    // =====================================================================
    // Tests de réactivation
    // =====================================================================

    @Nested
    @DisplayName("reactiver()")
    class ReactiverTests {

        @Test
        @DisplayName("✅ cas nominal — actif = true puis sauvegardé")
        void reactiver_casNominal_doitSetActifTrue() {
            // GIVEN
            clientExistant.setActif(false);
            given(clientRepository.findById(1L)).willReturn(Optional.of(clientExistant));
            given(clientRepository.save(any(Client.class))).willReturn(clientExistant);
            given(clientMapper.toResponse(clientExistant)).willReturn(clientResponse);

            // WHEN
            ClientResponse response = clientService.reactiver(1L);

            // THEN
            assertThat(response).isNotNull();
            assertThat(clientExistant.getActif()).isTrue();
            then(clientRepository).should(times(1)).save(argThat(c -> c.getActif()));
        }

        @Test
        @DisplayName("✅ déjà actif — no-op silencieux, retourne response")
        void reactiver_dejaActif_noOpSilencieux() {
            // GIVEN
            clientExistant.setActif(true);
            given(clientRepository.findById(1L)).willReturn(Optional.of(clientExistant));
            given(clientRepository.save(any(Client.class))).willReturn(clientExistant);
            given(clientMapper.toResponse(clientExistant)).willReturn(clientResponse);

            // WHEN
            ClientResponse response = clientService.reactiver(1L);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getActif()).isTrue();
            then(clientRepository).should(times(1)).save(clientExistant);
        }

        @Test
        @DisplayName("❌ id inexistant — doit lever ResourceNotFoundException")
        void reactiver_idInexistant_doitLeverResourceNotFoundException() {
            // GIVEN
            given(clientRepository.findById(999L)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> clientService.reactiver(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
            then(clientRepository).should(never()).save(any());
        }
    }
}
