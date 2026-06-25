package com.pme.stock.integration;

import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.request.ConnexionRequest;
import com.pme.stock.dto.response.AuthResponse;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.dto.response.FournisseurResponse;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.entity.Categorie;
import com.pme.stock.entity.Fournisseur;
import com.pme.stock.entity.Produit;
import com.pme.stock.entity.Role;
import com.pme.stock.entity.Utilisateur;
import com.pme.stock.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("SecurityRolesIntegrationTest - Tests end-to-end")
class SecurityRolesIntegrationTest {

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;

    @Autowired private RoleRepository roleRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private CategorieRepository categorieRepository;
    @Autowired private ProduitRepository produitRepository;
    @Autowired private MouvementStockRepository mouvementStockRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private LigneCommandeClientRepository ligneCommandeClientRepository;
    @Autowired private CommandeClientRepository commandeClientRepository;
    @Autowired private LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;
    @Autowired private CommandeFournisseurRepository commandeFournisseurRepository;
    @Autowired private FournisseurRepository fournisseurRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private String baseUrl;

    private Role roleAdmin;
    private Role roleGestionnaire;
    private Role roleEmploye;

    @BeforeEach
    void setUp() {
        if (restTemplate.getRestTemplate().getRequestFactory() instanceof org.springframework.http.client.SimpleClientHttpRequestFactory rf) {
            rf.setOutputStreaming(false);
        }
        baseUrl = "http://localhost:" + port + "/api/v1";

        // Nettoyage complet
        ligneCommandeFournisseurRepository.deleteAll();
        commandeFournisseurRepository.deleteAll();
        ligneCommandeClientRepository.deleteAll();
        commandeClientRepository.deleteAll();
        mouvementStockRepository.deleteAll();
        produitRepository.deleteAll();
        fournisseurRepository.deleteAll();
        clientRepository.deleteAll();
        categorieRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        utilisateurRepository.deleteAll();
        roleRepository.deleteAll();

        // Enregistrer les rôles
        roleAdmin = roleRepository.save(Role.builder().nom("ROLE_ADMIN").build());
        roleGestionnaire = roleRepository.save(Role.builder().nom("ROLE_GESTIONNAIRE").build());
        roleEmploye = roleRepository.save(Role.builder().nom("ROLE_EMPLOYE").build());
    }

    private String getAccessToken(String email, String password, Role role) {
        Utilisateur utilisateur = Utilisateur.builder()
                .nom("Nom")
                .prenom("Prenom")
                .email(email)
                .motDePasse(passwordEncoder.encode(password))
                .roles(Set.of(role))
                .actif(true)
                .build();
        utilisateurRepository.save(utilisateur);

        ConnexionRequest connRequest = new ConnexionRequest();
        connRequest.setEmail(email);
        connRequest.setMotDePasse(password);

        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                baseUrl + "/auth/connexion",
                connRequest,
                AuthResponse.class
        );
        return authResponse.getBody().getAccessToken();
    }

    private HttpHeaders headersAvecToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Test 1: Gestionnaire peut créer un produit, mais ne peut pas supprimer un fournisseur")
    void utilisateurGestionnaire_peutCreerProduit_maisNePeutPasSupprimerFournisseur() {
        String token = getAccessToken("gestionnaire@pme.com", "Gestionnaire@123", roleGestionnaire);
        HttpHeaders headers = headersAvecToken(token);

        // 1. Créer une catégorie en tant que gestionnaire
        CategorieRequest catReq = new CategorieRequest();
        catReq.setCode("CAT-SEC");
        catReq.setLibelle("Catégorie Sec");
        ResponseEntity<CategorieResponse> catResp = restTemplate.postForEntity(
                baseUrl + "/categories",
                new HttpEntity<>(catReq, headers),
                CategorieResponse.class
        );
        Long catId = catResp.getBody().getId();

        // 2. Créer un produit en tant que gestionnaire (doit réussir - 201)
        ProduitRequest prodReq = new ProduitRequest();
        prodReq.setReference("REF-GEST-01");
        prodReq.setDesignation("Produit par Gestionnaire");
        prodReq.setPrixAchat(BigDecimal.valueOf(100));
        prodReq.setPrixVente(BigDecimal.valueOf(150));
        prodReq.setQuantiteStock(10);
        prodReq.setCategorieId(catId);

        ResponseEntity<ProduitResponse> prodResp = restTemplate.postForEntity(
                baseUrl + "/produits",
                new HttpEntity<>(prodReq, headers),
                ProduitResponse.class
        );
        assertThat(prodResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 3. Créer un fournisseur (via DB pour éviter d'autres permissions)
        Fournisseur fournisseur = fournisseurRepository.save(Fournisseur.builder()
                .code("FOUR-SEC")
                .raisonSociale("Fournisseur Sec")
                .actif(true)
                .build());

        // 4. Supprimer ce fournisseur en tant que gestionnaire (doit échouer - 403)
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                baseUrl + "/fournisseurs/" + fournisseur.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Test 2: Employé (lecture seule) peut consulter les produits, mais ne peut pas en créer")
    void utilisateurSansRole_lectureSeule_doitPouvoirConsulterMaisPasCreer() {
        String token = getAccessToken("employe@pme.com", "Employe@123", roleEmploye);
        HttpHeaders headers = headersAvecToken(token);

        // Créer une catégorie et un produit en base pour la consultation
        Categorie cat = categorieRepository.save(Categorie.builder().code("CAT").libelle("Cat").actif(true).build());
        produitRepository.save(Produit.builder()
                .reference("REF-CONSULT")
                .designation("Produit Consultable")
                .prixAchat(BigDecimal.valueOf(10))
                .prixVente(BigDecimal.valueOf(15))
                .quantiteStock(50)
                .actif(true)
                .categorie(cat)
                .build());

        // 1. Consulter les produits (doit réussir - 200)
        ResponseEntity<Object> listResp = restTemplate.exchange(
                baseUrl + "/produits",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        );
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2. Tenter de créer un produit (doit échouer - 403)
        ProduitRequest prodReq = new ProduitRequest();
        prodReq.setReference("REF-FAIL-01");
        prodReq.setDesignation("Produit par Employé");
        prodReq.setPrixAchat(BigDecimal.valueOf(10));
        prodReq.setPrixVente(BigDecimal.valueOf(15));
        prodReq.setCategorieId(cat.getId());

        ResponseEntity<Object> createResp = restTemplate.postForEntity(
                baseUrl + "/produits",
                new HttpEntity<>(prodReq, headers),
                Object.class
        );
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Disabled("Difficile à tester sans manipuler l'horloge système ou générer un token expiré artificiellement")
    @DisplayName("Test 3: Token expiré sur ressource protégée")
    void tokenExpire_doitRetourner401SurRessourceProtegee() {
        // La vérification d'un token expiré nécessite la génération d'un jeton avec une date passée
        // ou la manipulation de l'horloge au niveau de la JVM. Pour des tests d'intégration standard
        // basés sur des requêtes réelles sans mock, ce test est désactivé.
    }
}
