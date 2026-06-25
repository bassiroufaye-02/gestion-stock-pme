package com.pme.stock.integration;

import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.request.ClientRequest;
import com.pme.stock.dto.request.CommandeClientRequest;
import com.pme.stock.dto.request.LigneCommandeRequest;
import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.request.ConnexionRequest;
import com.pme.stock.dto.response.AuthResponse;
import com.pme.stock.dto.response.CategorieResponse;
import com.pme.stock.dto.response.ClientResponse;
import com.pme.stock.dto.response.CommandeClientResponse;
import com.pme.stock.dto.response.ChiffreAffairesMoisResponse;
import com.pme.stock.dto.response.ProduitResponse;
import com.pme.stock.entity.Categorie;
import com.pme.stock.entity.Client;
import com.pme.stock.entity.Produit;
import com.pme.stock.entity.Role;
import com.pme.stock.entity.StatutCommande;
import com.pme.stock.entity.Utilisateur;
import com.pme.stock.repository.*;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("CommandeClientFlowIntegrationTest - Tests end-to-end")
class CommandeClientFlowIntegrationTest {

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
    private String adminToken;

    @BeforeEach
    void setUp() {
        if (restTemplate.getRestTemplate().getRequestFactory() instanceof org.springframework.http.client.SimpleClientHttpRequestFactory rf) {
            rf.setOutputStreaming(false);
        }
        baseUrl = "http://localhost:" + port + "/api/v1";

        // Nettoyage de la base de données
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
    @DisplayName("Test 1: Cycle complet commande client (Brouillon -> Livrée)")
    void cycleCompletCommandeClient_brouillonVersLivree_doitSuivreToutesLesTransitions() {
        HttpHeaders headers = headersAvecToken(adminToken);

        // 1. Créer catégorie, produit (stock=100), client
        CategorieRequest catReq = new CategorieRequest();
        catReq.setCode("CAT");
        catReq.setLibelle("Catégorie Test");
        ResponseEntity<CategorieResponse> catResp = restTemplate.postForEntity(baseUrl + "/categories", new HttpEntity<>(catReq, headers), CategorieResponse.class);
        Long catId = catResp.getBody().getId();

        ProduitRequest prodReq = new ProduitRequest();
        prodReq.setReference("PROD-CMD");
        prodReq.setDesignation("Produit Commande");
        prodReq.setPrixAchat(BigDecimal.valueOf(100));
        prodReq.setPrixVente(BigDecimal.valueOf(200));
        prodReq.setQuantiteStock(100);
        prodReq.setCategorieId(catId);
        ResponseEntity<ProduitResponse> prodResp = restTemplate.postForEntity(baseUrl + "/produits", new HttpEntity<>(prodReq, headers), ProduitResponse.class);
        Long prodId = prodResp.getBody().getId();

        ClientRequest cliReq = new ClientRequest();
        cliReq.setCode("CLI-CMD");
        cliReq.setRaisonSociale("Client Commande SARL");
        ResponseEntity<ClientResponse> cliResp = restTemplate.postForEntity(baseUrl + "/clients", new HttpEntity<>(cliReq, headers), ClientResponse.class);
        Long cliId = cliResp.getBody().getId();

        // 2. POST /commandes -> statut BROUILLON (sans lignes)
        CommandeClientRequest cmdReq = new CommandeClientRequest();
        cmdReq.setClientId(cliId);
        cmdReq.setTauxTVA(BigDecimal.valueOf(18.00));
        ResponseEntity<CommandeClientResponse> cmdResp = restTemplate.postForEntity(baseUrl + "/commandes", new HttpEntity<>(cmdReq, headers), CommandeClientResponse.class);
        assertThat(cmdResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long cmdId = cmdResp.getBody().getId();
        assertThat(cmdResp.getBody().getStatut()).isEqualTo(StatutCommande.BROUILLON);

        // 3. POST /commandes/{id}/lignes -> ajouter une ligne de 20 unités
        LigneCommandeRequest ligneReq = new LigneCommandeRequest();
        ligneReq.setProduitId(prodId);
        ligneReq.setQuantite(20);
        ligneReq.setPrixUnitaireHT(BigDecimal.valueOf(200));

        ResponseEntity<CommandeClientResponse> ligneResp = restTemplate.postForEntity(
                baseUrl + "/commandes/" + cmdId + "/lignes",
                new HttpEntity<>(ligneReq, headers),
                CommandeClientResponse.class
        );
        assertThat(ligneResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ligneResp.getBody().getLignes()).hasSize(1);

        // 4. POST /commandes/{id}/confirmer -> statut CONFIRMEE, stock décrémenté à 80
        ResponseEntity<CommandeClientResponse> confirmResp = restTemplate.postForEntity(
                baseUrl + "/commandes/" + cmdId + "/confirmer",
                new HttpEntity<>(headers),
                CommandeClientResponse.class
        );
        assertThat(confirmResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmResp.getBody().getStatut()).isEqualTo(StatutCommande.CONFIRMEE);

        // Vérifier stock produit = 80
        ProduitResponse prodGet = restTemplate.exchange(baseUrl + "/produits/" + prodId, HttpMethod.GET, new HttpEntity<>(headers), ProduitResponse.class).getBody();
        assertThat(prodGet.getQuantiteStock()).isEqualTo(80);

        // 5. Passer en EN_PREPARATION, EXPEDIEE puis LIVREE via PUT /commandes/{id}/statut
        ResponseEntity<CommandeClientResponse> prepResp = restTemplate.exchange(
                baseUrl + "/commandes/" + cmdId + "/statut?nouveauStatut=EN_PREPARATION",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                CommandeClientResponse.class
        );
        assertThat(prepResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(prepResp.getBody().getStatut()).isEqualTo(StatutCommande.EN_PREPARATION);

        ResponseEntity<CommandeClientResponse> expedieeResp = restTemplate.exchange(
                baseUrl + "/commandes/" + cmdId + "/statut?nouveauStatut=EXPEDIEE",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                CommandeClientResponse.class
        );
        assertThat(expedieeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(expedieeResp.getBody().getStatut()).isEqualTo(StatutCommande.EXPEDIEE);

        ResponseEntity<CommandeClientResponse> livreeResp = restTemplate.exchange(
                baseUrl + "/commandes/" + cmdId + "/statut?nouveauStatut=LIVREE",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                CommandeClientResponse.class
        );
        assertThat(livreeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(livreeResp.getBody().getStatut()).isEqualTo(StatutCommande.LIVREE);

        // 6. Vérifier le montantTTC calculé est cohérent (HT + TVA)
        // HT = 20 * 200 = 4000
        // TVA = 4000 * 0.18 = 720
        // TTC = 4720
        CommandeClientResponse cmdFinal = livreeResp.getBody();
        assertThat(cmdFinal.getMontantHT().stripTrailingZeros()).isEqualTo(BigDecimal.valueOf(4000).setScale(2).stripTrailingZeros());
        assertThat(cmdFinal.getMontantTTC().stripTrailingZeros()).isEqualTo(BigDecimal.valueOf(4720).setScale(2).stripTrailingZeros());

        // 7. GET /dashboard/ca/mensuel -> vérifier que cette commande contribue au CA
        LocalDate now = LocalDate.now();
        ResponseEntity<ChiffreAffairesMoisResponse> caResp = restTemplate.exchange(
                baseUrl + "/dashboard/ca/mensuel?annee=" + now.getYear() + "&mois=" + now.getMonthValue(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ChiffreAffairesMoisResponse.class
        );
        assertThat(caResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(caResp.getBody().getChiffreAffairesTTC().stripTrailingZeros()).isEqualTo(BigDecimal.valueOf(4720).setScale(2).stripTrailingZeros());
        assertThat(caResp.getBody().getNombreCommandesLivrees()).isEqualTo(1);
    }

    @Test
    @DisplayName("Test 2: Annulation de commande confirmée")
    void annulerCommandeConfirmee_doitRemettreLeStock() {
        HttpHeaders headers = headersAvecToken(adminToken);

        // Créer Catégorie, Produit (stock=50), Client
        Categorie cat = categorieRepository.save(Categorie.builder().code("CAT2").libelle("Cat2").actif(true).build());
        Produit prod = produitRepository.save(Produit.builder()
                .reference("PROD-CANCEL")
                .designation("Produit Cancel")
                .prixAchat(BigDecimal.valueOf(50))
                .prixVente(BigDecimal.valueOf(100))
                .quantiteStock(50)
                .seuilAlerte(5)
                .actif(true)
                .categorie(cat)
                .build());
        Client clientObj = new Client();
        clientObj.setCode("CLI-CANCEL");
        clientObj.setRaisonSociale("Client Cancel");
        clientObj.setActif(true);
        Client client = clientRepository.save(clientObj);

        // Créer commande en BROUILLON avec 10 unités
        CommandeClientRequest cmdReq = new CommandeClientRequest();
        cmdReq.setClientId(client.getId());
        LigneCommandeRequest ligneReq = new LigneCommandeRequest();
        ligneReq.setProduitId(prod.getId());
        ligneReq.setQuantite(10);
        ligneReq.setPrixUnitaireHT(BigDecimal.valueOf(100));
        cmdReq.setLignes(List.of(ligneReq));

        ResponseEntity<CommandeClientResponse> cmdResp = restTemplate.postForEntity(baseUrl + "/commandes", new HttpEntity<>(cmdReq, headers), CommandeClientResponse.class);
        Long cmdId = cmdResp.getBody().getId();

        // Confirmer commande (stock -> 40)
        restTemplate.postForEntity(baseUrl + "/commandes/" + cmdId + "/confirmer", new HttpEntity<>(headers), CommandeClientResponse.class);
        Produit prodAfterConfirm = produitRepository.findById(prod.getId()).orElseThrow();
        assertThat(prodAfterConfirm.getQuantiteStock()).isEqualTo(40);

        // Annuler commande (statut -> ANNULEE)
        ResponseEntity<CommandeClientResponse> cancelResp = restTemplate.exchange(
                baseUrl + "/commandes/" + cmdId + "/statut?nouveauStatut=ANNULEE",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                CommandeClientResponse.class
        );
        assertThat(cancelResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelResp.getBody().getStatut()).isEqualTo(StatutCommande.ANNULEE);

        // Vérifier stock retourné à 50
        Produit prodAfterCancel = produitRepository.findById(prod.getId()).orElseThrow();
        assertThat(prodAfterCancel.getQuantiteStock()).isEqualTo(50);
    }
}
