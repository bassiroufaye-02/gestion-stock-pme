package com.pme.stock.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.request.CommandeFournisseurRequest;
import com.pme.stock.dto.request.LigneCommandeFournisseurRequest;
import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.request.ConnexionRequest;
import com.pme.stock.dto.response.AuthResponse;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.dto.response.FournisseurResponse;
import com.pme.stock.dto.response.CommandeFournisseurResponse;
import com.pme.stock.dto.response.DashboardResponse;
import com.pme.stock.dto.response.MouvementStockResponse;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.entity.Categorie;
import com.pme.stock.entity.Fournisseur;
import com.pme.stock.entity.MouvementStock;
import com.pme.stock.entity.Produit;
import com.pme.stock.entity.Role;
import com.pme.stock.entity.StatutCommandeFournisseur;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("CommandeFournisseurFlowIntegrationTest - Tests end-to-end")
class CommandeFournisseurFlowIntegrationTest {

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

        // Nettoyage de la base de données dans l'ordre
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

        // Rôles
        Role adminRole = roleRepository.save(Role.builder().nom("ROLE_ADMIN").build());
        roleRepository.save(Role.builder().nom("ROLE_GESTIONNAIRE").build());
        roleRepository.save(Role.builder().nom("ROLE_EMPLOYE").build());

        // Admin
        Utilisateur admin = Utilisateur.builder()
                .nom("Admin")
                .prenom("Test")
                .email("admin@pme.com")
                .motDePasse(passwordEncoder.encode("Admin@123"))
                .roles(Set.of(adminRole))
                .actif(true)
                .build();
        utilisateurRepository.save(admin);

        // Connexion
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

    @Test
    @DisplayName("Test 1: Cycle de réception fournisseur nominal")
    void cycleCompletReceptionFournisseur_doitIncrementerStockEtCreerMouvement() throws Exception {
        HttpHeaders headers = headersAvecToken(adminToken);

        // 1. Créer catégorie, produit (stock=10), fournisseur
        CategorieRequest catReq = new CategorieRequest();
        catReq.setCode("CAT");
        catReq.setLibelle("Catégorie Test");
        ResponseEntity<CategorieResponse> catResp = restTemplate.postForEntity(baseUrl + "/categories", new HttpEntity<>(catReq, headers), CategorieResponse.class);
        Long catId = catResp.getBody().getId();

        ProduitRequest prodReq = new ProduitRequest();
        prodReq.setReference("PROD-FOUR");
        prodReq.setDesignation("Produit Fournisseur");
        prodReq.setPrixAchat(BigDecimal.valueOf(100));
        prodReq.setPrixVente(BigDecimal.valueOf(200));
        prodReq.setQuantiteStock(10);
        prodReq.setCategorieId(catId);
        ResponseEntity<ProduitResponse> prodResp = restTemplate.postForEntity(baseUrl + "/produits", new HttpEntity<>(prodReq, headers), ProduitResponse.class);
        Long prodId = prodResp.getBody().getId();

        FournisseurRequest fourReq = new FournisseurRequest();
        fourReq.setCode("FOUR-TEST");
        fourReq.setRaisonSociale("Fournisseur Test SARL");
        ResponseEntity<FournisseurResponse> fourResp = restTemplate.postForEntity(baseUrl + "/fournisseurs", new HttpEntity<>(fourReq, headers), FournisseurResponse.class);
        Long fourId = fourResp.getBody().getId();

        // 2. POST /commandes-fournisseurs -> BROUILLON avec ligne de 20 unités
        CommandeFournisseurRequest cmdReq = new CommandeFournisseurRequest();
        cmdReq.setFournisseurId(fourId);
        cmdReq.setTauxTVA(BigDecimal.valueOf(18.00));
        cmdReq.setDateCommandePrevue(LocalDate.now().plusDays(5));

        LigneCommandeFournisseurRequest ligneReq = new LigneCommandeFournisseurRequest();
        ligneReq.setProduitId(prodId);
        ligneReq.setQuantiteCommandee(20);
        ligneReq.setPrixUnitaireAchat(BigDecimal.valueOf(100));
        cmdReq.setLignes(List.of(ligneReq));

        ResponseEntity<CommandeFournisseurResponse> cmdResp = restTemplate.postForEntity(
                baseUrl + "/commandes-fournisseurs",
                new HttpEntity<>(cmdReq, headers),
                CommandeFournisseurResponse.class
        );
        assertThat(cmdResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long cmdId = cmdResp.getBody().getId();
        assertThat(cmdResp.getBody().getStatut()).isEqualTo(StatutCommandeFournisseur.BROUILLON);

        // 3. POST /commandes-fournisseurs/{id}/envoyer -> ENVOYEE
        ResponseEntity<CommandeFournisseurResponse> envResp = restTemplate.postForEntity(
                baseUrl + "/commandes-fournisseurs/" + cmdId + "/envoyer",
                new HttpEntity<>(headers),
                CommandeFournisseurResponse.class
        );
        assertThat(envResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(envResp.getBody().getStatut()).isEqualTo(StatutCommandeFournisseur.ENVOYEE);

        // 4. POST /commandes-fournisseurs/{id}/receptionner -> RECUE
        ResponseEntity<CommandeFournisseurResponse> recResp = restTemplate.postForEntity(
                baseUrl + "/commandes-fournisseurs/" + cmdId + "/receptionner",
                new HttpEntity<>(headers),
                CommandeFournisseurResponse.class
        );
        assertThat(recResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(recResp.getBody().getStatut()).isEqualTo(StatutCommandeFournisseur.RECUE);

        // 5. Vérifier GET /produits/{id} : quantiteStock = 30 (10 + 20)
        ResponseEntity<ProduitResponse> prodGet = restTemplate.exchange(
                baseUrl + "/produits/" + prodId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProduitResponse.class
        );
        assertThat(prodGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(prodGet.getBody().getQuantiteStock()).isEqualTo(30);

        // 6. GET /stock/mouvements/produit/{id} -> contient un mouvement ENTREE de 20
        ResponseEntity<String> mvtsGetResp = restTemplate.exchange(
                baseUrl + "/stock/mouvements/produit/" + prodId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(mvtsGetResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        StockFlowIntegrationTest.TestPageResponse<MouvementStockResponse> page = objectMapper.readValue(
                mvtsGetResp.getBody(),
                new TypeReference<StockFlowIntegrationTest.TestPageResponse<MouvementStockResponse>>() {}
        );
        assertThat(page.getContent()).isNotEmpty();
        MouvementStockResponse entryMvt = page.getContent().stream()
                .filter(m -> m.getMotif().contains(cmdResp.getBody().getNumeroCommande()))
                .findFirst().orElseThrow();
        assertThat(entryMvt.getQuantite()).isEqualTo(20);
        assertThat(entryMvt.getTypeMouvement()).isEqualTo(MouvementStock.TypeMouvement.ENTREE);

        // 7. GET /dashboard -> nombreCommandesFournisseursEnAttente ne compte plus cette commande
        ResponseEntity<DashboardResponse> dashResp = restTemplate.exchange(
                baseUrl + "/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DashboardResponse.class
        );
        assertThat(dashResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dashResp.getBody().getNombreCommandesFournisseursEnAttente()).isEqualTo(0);
    }

    @Test
    @Disabled("Non implémenté dans l'API ni dans le service - la réception est toujours totale dans le code actuel")
    @DisplayName("Test 2: Réception partielle")
    void receptionPartielle_doitGarderStatutRecuePartielle() {
        // Ce test est désactivé car la méthode de réception dans CommandeFournisseurServiceImpl
        // passe systématiquement toutes les lignes en reçues à 100% de leur quantité commandée,
        // et le statut de commande passe immédiatement à RECUE. Il n'existe pas d'endpoint ni de paramètre
        // HTTP pour gérer une réception partielle dans cette version de l'API.
    }
}
