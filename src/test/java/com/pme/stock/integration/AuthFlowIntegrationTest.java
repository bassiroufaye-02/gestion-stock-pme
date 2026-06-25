package com.pme.stock.integration;

import com.pme.stock.dto.request.ConnexionRequest;
import com.pme.stock.dto.request.InscriptionRequest;
import com.pme.stock.dto.request.RefreshTokenRequest;
import com.pme.stock.dto.response.AuthResponse;
import com.pme.stock.entity.Role;
import com.pme.stock.entity.Utilisateur;
import com.pme.stock.repository.RoleRepository;
import com.pme.stock.repository.UtilisateurRepository;
import com.pme.stock.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("AuthFlowIntegrationTest - Tests end-to-end")
class AuthFlowIntegrationTest {

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @Autowired private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private String baseUrl;

    @BeforeEach
    void setUp() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setOutputStreaming(false);
        restTemplate.getRestTemplate().setRequestFactory(factory);
        baseUrl = "http://localhost:" + port + "/api/v1";

        // Nettoyage de la base de données dans l'ordre des dépendances
        refreshTokenRepository.deleteAll();
        utilisateurRepository.deleteAll();
        roleRepository.deleteAll();

        // Recréer les rôles nécessaires
        roleRepository.save(Role.builder().nom("ROLE_ADMIN").build());
        roleRepository.save(Role.builder().nom("ROLE_GESTIONNAIRE").build());
        roleRepository.save(Role.builder().nom("ROLE_EMPLOYE").build());
    }

    private HttpHeaders headersAvecToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Test 1: Inscription puis connexion")
    void inscription_puisConnexion_doitFonctionnerDeBoutEnBout() {
        // 1. Inscription d'un nouveau gestionnaire
        InscriptionRequest inscription = new InscriptionRequest();
        inscription.setNom("Faye");
        inscription.setPrenom("Bassirou");
        inscription.setEmail("bassirou@pme.com");
        inscription.setMotDePasse("Pass@123!");
        inscription.setRoles(Set.of("ROLE_GESTIONNAIRE"));

        ResponseEntity<AuthResponse> inscriptionResponse = restTemplate.postForEntity(
                baseUrl + "/auth/inscription",
                inscription,
                AuthResponse.class
        );

        assertThat(inscriptionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AuthResponse responseBody = inscriptionResponse.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getAccessToken()).isNotEmpty();
        assertThat(responseBody.getRoles()).contains("ROLE_GESTIONNAIRE");

        // 2. Connexion avec les mêmes identifiants
        ConnexionRequest connexion = new ConnexionRequest();
        connexion.setEmail("bassirou@pme.com");
        connexion.setMotDePasse("Pass@123!");

        ResponseEntity<AuthResponse> connexionResponse = restTemplate.postForEntity(
                baseUrl + "/auth/connexion",
                connexion,
                AuthResponse.class
        );

        assertThat(connexionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse connBody = connexionResponse.getBody();
        assertThat(connBody).isNotNull();
        assertThat(connBody.getAccessToken()).isNotEmpty();
        assertThat(connBody.getRoles()).contains("ROLE_GESTIONNAIRE");
    }

    @Test
    @DisplayName("Test 2: Mauvais mot de passe")
    void connexion_mauvaisMotDePasse_doitRetourner401() {
        // Enregistrer d'abord un utilisateur valide
        Role role = roleRepository.findByNom("ROLE_GESTIONNAIRE").orElseThrow();
        Utilisateur utilisateur = Utilisateur.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@pme.com")
                .motDePasse(passwordEncoder.encode("Pass@123!"))
                .roles(Set.of(role))
                .actif(true)
                .build();
        utilisateurRepository.save(utilisateur);

        // Tentative de connexion avec un mauvais mot de passe
        ConnexionRequest connexion = new ConnexionRequest();
        connexion.setEmail("jean.dupont@pme.com");
        connexion.setMotDePasse("MauvaisPass");

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        try {
            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(baseUrl + "/auth/connexion"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(connexion)
                    ))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(
                    httpRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test 3: Accéder à une ressource protégée sans token")
    void accederRessourceProtegee_sansToken_doitRetourner401() {
        ResponseEntity<Object> response = restTemplate.getForEntity(
                baseUrl + "/produits",
                Object.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Test 4: Accéder à une ressource protégée avec un token valide")
    void accederRessourceProtegee_avecTokenValide_doitRetourner200() {
        // Inscrire/Connecter un admin
        Role adminRole = roleRepository.findByNom("ROLE_ADMIN").orElseThrow();
        Utilisateur admin = Utilisateur.builder()
                .nom("Admin")
                .prenom("Test")
                .email("admin.test@pme.com")
                .motDePasse(passwordEncoder.encode("Admin@123"))
                .roles(Set.of(adminRole))
                .actif(true)
                .build();
        utilisateurRepository.save(admin);

        ConnexionRequest connexion = new ConnexionRequest();
        connexion.setEmail("admin.test@pme.com");
        connexion.setMotDePasse("Admin@123");

        ResponseEntity<AuthResponse> connexionResponse = restTemplate.postForEntity(
                baseUrl + "/auth/connexion",
                connexion,
                AuthResponse.class
        );
        assertThat(connexionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = connexionResponse.getBody().getAccessToken();

        // GET /produits avec le token
        HttpHeaders headers = headersAvecToken(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/produits",
                HttpMethod.GET,
                requestEntity,
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Test 5: Refresh token nominal")
    void refreshToken_casNominal_doitGenererNouveauAccessToken() {
        // Inscrire/Connecter un utilisateur
        Role role = roleRepository.findByNom("ROLE_EMPLOYE").orElseThrow();
        Utilisateur employe = Utilisateur.builder()
                .nom("Employe")
                .prenom("Test")
                .email("employe.test@pme.com")
                .motDePasse(passwordEncoder.encode("Employe@123"))
                .roles(Set.of(role))
                .actif(true)
                .build();
        utilisateurRepository.save(employe);

        ConnexionRequest connexion = new ConnexionRequest();
        connexion.setEmail("employe.test@pme.com");
        connexion.setMotDePasse("Employe@123");

        ResponseEntity<AuthResponse> connexionResponse = restTemplate.postForEntity(
                baseUrl + "/auth/connexion",
                connexion,
                AuthResponse.class
        );
        assertThat(connexionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse body = connexionResponse.getBody();
        String oldAccessToken = body.getAccessToken();
        String refreshToken = body.getRefreshToken();

        // Attendre 1 seconde pour garantir un timestamp de JWT différent (iat / exp)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Refresh token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        ResponseEntity<AuthResponse> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/auth/refresh",
                refreshRequest,
                AuthResponse.class
        );

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse refreshBody = refreshResponse.getBody();
        assertThat(refreshBody).isNotNull();
        assertThat(refreshBody.getAccessToken()).isNotEmpty();
        assertThat(refreshBody.getAccessToken()).isNotEqualTo(oldAccessToken);
    }

    @Test
    @DisplayName("Test 6: Déconnexion nominale")
    void deconnexion_casNominal_doitInvaliderLeRefreshToken() {
        // Inscrire/Connecter un utilisateur
        Role role = roleRepository.findByNom("ROLE_EMPLOYE").orElseThrow();
        Utilisateur employe = Utilisateur.builder()
                .nom("Employe")
                .prenom("Test")
                .email("employe.test@pme.com")
                .motDePasse(passwordEncoder.encode("Employe@123"))
                .roles(Set.of(role))
                .actif(true)
                .build();
        utilisateurRepository.save(employe);

        ConnexionRequest connexion = new ConnexionRequest();
        connexion.setEmail("employe.test@pme.com");
        connexion.setMotDePasse("Employe@123");

        ResponseEntity<AuthResponse> connexionResponse = restTemplate.postForEntity(
                baseUrl + "/auth/connexion",
                connexion,
                AuthResponse.class
        );
        assertThat(connexionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse body = connexionResponse.getBody();
        String accessToken = body.getAccessToken();
        String refreshToken = body.getRefreshToken();

        // Déconnexion
        HttpHeaders headers = headersAvecToken(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Void> deconnexionResponse = restTemplate.postForEntity(
                baseUrl + "/auth/deconnexion",
                requestEntity,
                Void.class
        );
        assertThat(deconnexionResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Tentative de refresh
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        ResponseEntity<Object> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/auth/refresh",
                refreshRequest,
                Object.class
        );
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
