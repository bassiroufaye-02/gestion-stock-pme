package com.pme.stock.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.request.ClientRequest;
import com.pme.stock.dto.request.CommandeClientRequest;
import com.pme.stock.dto.request.LigneCommandeRequest;
import com.pme.stock.dto.request.MouvementStockRequest;
import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.request.ConnexionRequest;
import com.pme.stock.dto.response.AuthResponse;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.dto.response.ClientResponse;
import com.pme.stock.dto.response.CommandeClientResponse;
import com.pme.stock.dto.response.DashboardResponse;
import com.pme.stock.dto.response.MouvementStockResponse;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.dto.response.StockValeurResponse;
import com.pme.stock.entity.Categorie;
import com.pme.stock.entity.Client;
import com.pme.stock.entity.Fournisseur;
import com.pme.stock.entity.MouvementStock;
import com.pme.stock.entity.Produit;
import com.pme.stock.entity.Role;
import com.pme.stock.entity.Utilisateur;
import com.pme.stock.repository.*;
import lombok.Data;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("StockFlowIntegrationTest - Tests end-to-end")
class StockFlowIntegrationTest {

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;

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
    private String adminToken;

    @BeforeEach
    void setUp() {
        if (restTemplate.getRestTemplate().getRequestFactory() instanceof org.springframework.http.client.SimpleClientHttpRequestFactory rf) {
            rf.setOutputStreaming(false);
        }
        baseUrl = "http://localhost:" + port + "/api/v1";

        // Nettoyage de la base de données dans l'ordre pour respecter les contraintes d'intégrité
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

        // Enregistrer les rôles de base
        Role adminRole = roleRepository.save(Role.builder().nom("ROLE_ADMIN").build());
        roleRepository.save(Role.builder().nom("ROLE_GESTIONNAIRE").build());
        roleRepository.save(Role.builder().nom("ROLE_EMPLOYE").build());

        // Créer l'administrateur
        Utilisateur admin = Utilisateur.builder()
                .nom("Admin")
                .prenom("Test")
                .email("admin@pme.com")
                .motDePasse(passwordEncoder.encode("Admin@123"))
                .roles(Set.of(adminRole))
                .actif(true)
                .build();
        utilisateurRepository.save(admin);

        // Récupérer le token admin
        ConnexionRequest connRequest = new ConnexionRequest();
        connRequest.setEmail("admin@pme.com");
        connRequest.setMotDePasse("Admin@123");

        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                baseUrl + "/auth/connexion",
                connRequest,
                AuthResponse.class
        );
        adminToken = authResponse.getBody().getAccessToken();
    }

    private HttpHeaders headersAvecToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @Data
    public static class TestPageResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    @Test
    @DisplayName("Test 1: Cycle de stock complet")
    void cycleCompletStock_creationProduitEntreeSortieCommande_doitMettreAJourStockCorrectement() throws Exception {
        HttpHeaders headers = headersAvecToken(adminToken);

        // 1. POST /categories -> créer une catégorie
        CategorieRequest catRequest = new CategorieRequest();
        catRequest.setCode("INFO");
        catRequest.setLibelle("Informatique");
        catRequest.setDescription("Matériel informatique");

        ResponseEntity<CategorieResponse> catResp = restTemplate.exchange(
                baseUrl + "/categories",
                HttpMethod.POST,
                new HttpEntity<>(catRequest, headers),
                CategorieResponse.class
        );
        assertThat(catResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long catId = catResp.getBody().getId();

        // 2. POST /produits -> créer un produit avec quantiteStock = 0
        ProduitRequest prodRequest = new ProduitRequest();
        prodRequest.setReference("REF-TEST-001");
        prodRequest.setDesignation("Produit Test");
        prodRequest.setDescription("Description Produit Test");
        prodRequest.setPrixAchat(BigDecimal.valueOf(1000));
        prodRequest.setPrixVente(BigDecimal.valueOf(1500));
        prodRequest.setQuantiteStock(0);
        prodRequest.setSeuilAlerte(10);
        prodRequest.setCategorieId(catId);

        ResponseEntity<ProduitResponse> prodResp = restTemplate.exchange(
                baseUrl + "/produits",
                HttpMethod.POST,
                new HttpEntity<>(prodRequest, headers),
                ProduitResponse.class
        );
        assertThat(prodResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long prodId = prodResp.getBody().getId();
        assertThat(prodResp.getBody().getQuantiteStock()).isEqualTo(0);

        // 3. POST /stock/mouvements -> ENTREE de 100 unités
        MouvementStockRequest mvtRequest = new MouvementStockRequest();
        mvtRequest.setTypeMouvement(MouvementStock.TypeMouvement.ENTREE);
        mvtRequest.setQuantite(100);
        mvtRequest.setMotif("Entrée de stock initiale");
        mvtRequest.setProduitId(prodId);

        ResponseEntity<MouvementStockResponse> mvtResp = restTemplate.exchange(
                baseUrl + "/stock/mouvements",
                HttpMethod.POST,
                new HttpEntity<>(mvtRequest, headers),
                MouvementStockResponse.class
        );
        assertThat(mvtResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 3b. POST /stock/mouvements -> SORTIE de 10 unités pour avoir 2 mouvements dans l'historique
        MouvementStockRequest mvtRequestOut = new MouvementStockRequest();
        mvtRequestOut.setTypeMouvement(MouvementStock.TypeMouvement.SORTIE);
        mvtRequestOut.setQuantite(10);
        mvtRequestOut.setMotif("Sortie manuelle de test");
        mvtRequestOut.setProduitId(prodId);

        ResponseEntity<MouvementStockResponse> mvtOutResp = restTemplate.exchange(
                baseUrl + "/stock/mouvements",
                HttpMethod.POST,
                new HttpEntity<>(mvtRequestOut, headers),
                MouvementStockResponse.class
        );
        assertThat(mvtOutResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Vérifier GET /produits/{id} -> quantiteStock = 90 (100 - 10)
        ResponseEntity<ProduitResponse> prodGetResp = restTemplate.exchange(
                baseUrl + "/produits/" + prodId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProduitResponse.class
        );
        assertThat(prodGetResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(prodGetResp.getBody().getQuantiteStock()).isEqualTo(90);

        // 4. POST /clients -> créer un client
        ClientRequest cliRequest = new ClientRequest();
        cliRequest.setCode("CLI-TEST-001");
        cliRequest.setRaisonSociale("Client Test SARL");
        cliRequest.setEmail("client.test@pme.com");

        ResponseEntity<ClientResponse> cliResp = restTemplate.exchange(
                baseUrl + "/clients",
                HttpMethod.POST,
                new HttpEntity<>(cliRequest, headers),
                ClientResponse.class
        );
        assertThat(cliResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long cliId = cliResp.getBody().getId();

        // 5. POST /commandes -> créer une commande avec une ligne de 20 unités (90 - 20 = 70)
        CommandeClientRequest cmdRequest = new CommandeClientRequest();
        cmdRequest.setClientId(cliId);
        cmdRequest.setTauxTVA(BigDecimal.valueOf(18.00));
        
        LigneCommandeRequest ligneReq = new LigneCommandeRequest();
        ligneReq.setProduitId(prodId);
        ligneReq.setQuantite(20);
        ligneReq.setPrixUnitaireHT(BigDecimal.valueOf(1500));
        cmdRequest.setLignes(List.of(ligneReq));

        ResponseEntity<CommandeClientResponse> cmdResp = restTemplate.exchange(
                baseUrl + "/commandes",
                HttpMethod.POST,
                new HttpEntity<>(cmdRequest, headers),
                CommandeClientResponse.class
        );
        assertThat(cmdResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long cmdId = cmdResp.getBody().getId();

        // 6. POST /commandes/{id}/confirmer -> Confirmer la commande
        ResponseEntity<CommandeClientResponse> confirmResp = restTemplate.exchange(
                baseUrl + "/commandes/" + cmdId + "/confirmer",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                CommandeClientResponse.class
        );
        assertThat(confirmResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Vérifier GET /produits/{id} -> quantiteStock = 70 (100 - 30)
        ResponseEntity<ProduitResponse> prodGetResp2 = restTemplate.exchange(
                baseUrl + "/produits/" + prodId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProduitResponse.class
        );
        assertThat(prodGetResp2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(prodGetResp2.getBody().getQuantiteStock()).isEqualTo(70);

        // 7. GET /stock/mouvements/produit/{id} -> doit contenir 2 mouvements
        ResponseEntity<String> mvtsGetResp = restTemplate.exchange(
                baseUrl + "/stock/mouvements/produit/" + prodId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(mvtsGetResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        TestPageResponse<MouvementStockResponse> page = objectMapper.readValue(
                mvtsGetResp.getBody(),
                new TypeReference<TestPageResponse<MouvementStockResponse>>() {}
        );
        assertThat(page.getContent()).hasSize(2); // 1 entrée manuelle, 1 sortie manuelle

        // 8. GET /dashboard/stock/valeur -> vérifier que la valeur du stock reflète bien les 70 unités restantes
        ResponseEntity<StockValeurResponse> dashboardStockResp = restTemplate.exchange(
                baseUrl + "/dashboard/stock/valeur",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                StockValeurResponse.class
        );
        assertThat(dashboardStockResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        StockValeurResponse stockValeur = dashboardStockResp.getBody();
        assertThat(stockValeur).isNotNull();
        // 70 * 1000 (prixAchat) = 70000
        assertThat(stockValeur.getValeurTotaleAchat().stripTrailingZeros()).isEqualTo(BigDecimal.valueOf(70000).setScale(2).stripTrailingZeros());
        // 70 * 1500 (prixVente) = 105000
        assertThat(stockValeur.getValeurTotaleVente().stripTrailingZeros()).isEqualTo(BigDecimal.valueOf(105000).setScale(2).stripTrailingZeros());
    }

    @Test
    @DisplayName("Test 2: Sortie de stock insuffisante")
    void sortieStockInsuffisant_doitRetournerErreurMetier() {
        HttpHeaders headers = headersAvecToken(adminToken);

        // Créer Categorie + Produit avec stock = 5
        Categorie cat = categorieRepository.save(Categorie.builder().code("CAT").libelle("Cat").actif(true).build());
        Produit prod = produitRepository.save(Produit.builder()
                .reference("REF-LIMIT")
                .designation("Produit Limite")
                .prixAchat(BigDecimal.valueOf(10))
                .prixVente(BigDecimal.valueOf(20))
                .quantiteStock(5)
                .seuilAlerte(2)
                .actif(true)
                .categorie(cat)
                .build());

        // POST /stock/mouvements -> SORTIE de 10 unités (stock = 5)
        MouvementStockRequest mvtRequest = new MouvementStockRequest();
        mvtRequest.setTypeMouvement(MouvementStock.TypeMouvement.SORTIE);
        mvtRequest.setQuantite(10);
        mvtRequest.setMotif("Sortie excessive");
        mvtRequest.setProduitId(prod.getId());

        ResponseEntity<Object> mvtResp = restTemplate.exchange(
                baseUrl + "/stock/mouvements",
                HttpMethod.POST,
                new HttpEntity<>(mvtRequest, headers),
                Object.class
        );

        // Doit retourner 4xx d'erreur métier
        assertThat(mvtResp.getStatusCode().is4xxClientError()).isTrue();

        // Le stock du produit doit être resté à 5
        Produit prodVerif = produitRepository.findById(prod.getId()).orElseThrow();
        assertThat(prodVerif.getQuantiteStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("Test 3: Produit en alerte stock dans le dashboard")
    void produitEnAlerteStock_doitApparaitreDansDashboard() {
        HttpHeaders headers = headersAvecToken(adminToken);

        // Créer Categorie + Produit avec stock = 3 et seuilAlerte = 10
        Categorie cat = categorieRepository.save(Categorie.builder().code("ALERT").libelle("Alerte").actif(true).build());
        produitRepository.save(Produit.builder()
                .reference("REF-ALERT")
                .designation("Produit Alerte")
                .prixAchat(BigDecimal.valueOf(100))
                .prixVente(BigDecimal.valueOf(150))
                .quantiteStock(3)
                .seuilAlerte(10)
                .actif(true)
                .categorie(cat)
                .build());

        // GET /dashboard
        ResponseEntity<DashboardResponse> dashResp = restTemplate.exchange(
                baseUrl + "/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DashboardResponse.class
            );

        assertThat(dashResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        DashboardResponse dashboard = dashResp.getBody();
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getProduitsEnAlerte()).hasSize(1);
        assertThat(dashboard.getProduitsEnAlerte().get(0).getReference()).isEqualTo("REF-ALERT");
    }
}
