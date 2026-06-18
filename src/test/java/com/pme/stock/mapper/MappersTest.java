package com.pme.stock.mapper;

import com.pme.stock.dto.request.CategorieRequest;
import com.pme.stock.dto.request.ClientRequest;
import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.request.ProduitRequest;
import com.pme.stock.dto.response.*;
import com.pme.stock.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Mappers MapStruct - Tests unitaires")
class MappersTest {

    private final CategorieMapper categorieMapper = Mappers.getMapper(CategorieMapper.class);
    private final ClientMapper clientMapper = Mappers.getMapper(ClientMapper.class);
    private final ProduitMapper produitMapper = Mappers.getMapper(ProduitMapper.class);
    private final MouvementStockMapper mouvementStockMapper = Mappers.getMapper(MouvementStockMapper.class);
    private final CommandeClientMapper commandeClientMapper = Mappers.getMapper(CommandeClientMapper.class);
    private final FournisseurMapper fournisseurMapper = Mappers.getMapper(FournisseurMapper.class);
    private final CommandeFournisseurMapper commandeFournisseurMapper = Mappers.getMapper(CommandeFournisseurMapper.class);

    @Test
    @DisplayName("✅ CategorieMapper - toResponse & toResponseList & toEntity")
    void testCategorieMapper() {
        // test toResponse with null products
        Categorie categorie = Categorie.builder()
                .id(1L)
                .code("CODE")
                .libelle("Libelle")
                .description("Desc")
                .actif(true)
                .produits(null)
                .build();
        
        CategorieResponse response = categorieMapper.toResponse(categorie);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("CODE");
        assertThat(response.getLibelle()).isEqualTo("Libelle");
        assertThat(response.getDescription()).isEqualTo("Desc");
        assertThat(response.getActif()).isTrue();
        assertThat(response.getNombreProduits()).isEqualTo(0);

        // test toResponse with products
        categorie.setProduits(List.of(new Produit(), new Produit()));
        response = categorieMapper.toResponse(categorie);
        assertThat(response.getNombreProduits()).isEqualTo(2);

        // test toResponseList
        List<CategorieResponse> list = categorieMapper.toResponseList(List.of(categorie));
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getNombreProduits()).isEqualTo(2);

        // test toEntity
        CategorieRequest request = new CategorieRequest();
        request.setCode("NEW");
        request.setLibelle("New Libelle");
        request.setDescription("New Desc");
        Categorie entity = categorieMapper.toEntity(request);
        assertThat(entity).isNotNull();
        assertThat(entity.getCode()).isEqualTo("NEW");
        assertThat(entity.getLibelle()).isEqualTo("New Libelle");
        assertThat(entity.getDescription()).isEqualTo("New Desc");

        // null checks
        assertThat(categorieMapper.toResponse(null)).isNull();
        assertThat(categorieMapper.toEntity(null)).isNull();
        assertThat(categorieMapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("✅ ClientMapper - toResponse & toResponseList & toEntity")
    void testClientMapper() {
        LocalDateTime now = LocalDateTime.now();
        Client client = new Client();
        client.setId(10L);
        client.setRaisonSociale("Client Enterprise");
        client.setEmail("client@enterprise.com");
        client.setTelephone("123456789");
        client.setAdresse("123 Street");
        client.setCreatedAt(now);

        ClientResponse response = clientMapper.toResponse(client);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getRaisonSociale()).isEqualTo("Client Enterprise");
        assertThat(response.getEmail()).isEqualTo("client@enterprise.com");
        assertThat(response.getTelephone()).isEqualTo("123456789");
        assertThat(response.getAdresse()).isEqualTo("123 Street");
        assertThat(response.getCreatedAt()).isEqualTo(now);

        List<ClientResponse> list = clientMapper.toResponseList(List.of(client));
        assertThat(list).hasSize(1);

        ClientRequest request = new ClientRequest();
        request.setRaisonSociale("New Raison");
        request.setEmail("new@client.com");
        request.setTelephone("987654321");
        request.setAdresse("456 Avenue");
        Client entity = clientMapper.toEntity(request);
        assertThat(entity).isNotNull();
        assertThat(entity.getRaisonSociale()).isEqualTo("New Raison");
        assertThat(entity.getEmail()).isEqualTo("new@client.com");
        assertThat(entity.getTelephone()).isEqualTo("987654321");
        assertThat(entity.getAdresse()).isEqualTo("456 Avenue");

        // null checks
        assertThat(clientMapper.toResponse(null)).isNull();
        assertThat(clientMapper.toEntity(null)).isNull();
        assertThat(clientMapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("✅ ProduitMapper - toResponse & toResponseList & toEntity")
    void testProduitMapper() {
        Categorie categorie = Categorie.builder().id(5L).libelle("Electronics").build();
        Produit produit = Produit.builder()
                .id(100L)
                .reference("PROD-100")
                .designation("Smartphone")
                .prixAchat(BigDecimal.valueOf(200.00))
                .prixVente(BigDecimal.valueOf(399.99))
                .quantiteStock(5)
                .seuilAlerte(10)
                .actif(true)
                .categorie(categorie)
                .build();

        ProduitResponse response = produitMapper.toResponse(produit);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getReference()).isEqualTo("PROD-100");
        assertThat(response.getDesignation()).isEqualTo("Smartphone");
        assertThat(response.getPrixAchat()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(response.getPrixVente()).isEqualTo(BigDecimal.valueOf(399.99));
        assertThat(response.getQuantiteStock()).isEqualTo(5);
        assertThat(response.getSeuilAlerte()).isEqualTo(10);
        assertThat(response.getActif()).isTrue();
        assertThat(response.getCategorieId()).isEqualTo(5L);
        assertThat(response.getCategorieLibelle()).isEqualTo("Electronics");
        assertThat(response.getEnAlerte()).isTrue();
        assertThat(response.getEnRupture()).isFalse();

        // test with null category
        produit.setCategorie(null);
        response = produitMapper.toResponse(produit);
        assertThat(response.getCategorieId()).isNull();
        assertThat(response.getCategorieLibelle()).isNull();

        List<ProduitResponse> list = produitMapper.toResponseList(List.of(produit));
        assertThat(list).hasSize(1);

        ProduitRequest request = new ProduitRequest();
        request.setReference("REQ-01");
        request.setDesignation("Laptop");
        request.setPrixAchat(BigDecimal.valueOf(500.00));
        request.setPrixVente(BigDecimal.valueOf(899.99));
        request.setQuantiteStock(2);
        request.setSeuilAlerte(1);
        Produit entity = produitMapper.toEntity(request);
        assertThat(entity).isNotNull();
        assertThat(entity.getReference()).isEqualTo("REQ-01");
        assertThat(entity.getDesignation()).isEqualTo("Laptop");
        assertThat(entity.getPrixAchat()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(entity.getPrixVente()).isEqualTo(BigDecimal.valueOf(899.99));
        assertThat(entity.getQuantiteStock()).isEqualTo(2);
        assertThat(entity.getSeuilAlerte()).isEqualTo(1);

        // null checks
        assertThat(produitMapper.toResponse(null)).isNull();
        assertThat(produitMapper.toEntity(null)).isNull();
        assertThat(produitMapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("✅ MouvementStockMapper - toResponse & toResponseList")
    void testMouvementStockMapper() {
        Produit produit = Produit.builder().id(2L).reference("REF-P").designation("Designation-P").build();
        Utilisateur utilisateur = Utilisateur.builder().id(3L).email("user@pme.com").build();
        MouvementStock movement = MouvementStock.builder()
                .id(1L)
                .typeMouvement(MouvementStock.TypeMouvement.ENTREE)
                .quantite(15)
                .motif("Incoming stock")
                .produit(produit)
                .utilisateur(utilisateur)
                .build();

        MouvementStockResponse response = mouvementStockMapper.toResponse(movement);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTypeMouvement()).isEqualTo(MouvementStock.TypeMouvement.ENTREE);
        assertThat(response.getQuantite()).isEqualTo(15);
        assertThat(response.getMotif()).isEqualTo("Incoming stock");
        assertThat(response.getProduitId()).isEqualTo(2L);
        assertThat(response.getProduitReference()).isEqualTo("REF-P");
        assertThat(response.getProduitDesignation()).isEqualTo("Designation-P");
        assertThat(response.getUtilisateurEmail()).isEqualTo("user@pme.com");

        // null references check
        movement.setProduit(null);
        movement.setUtilisateur(null);
        response = mouvementStockMapper.toResponse(movement);
        assertThat(response.getProduitId()).isNull();
        assertThat(response.getProduitReference()).isNull();
        assertThat(response.getProduitDesignation()).isNull();
        assertThat(response.getUtilisateurEmail()).isNull();

        List<MouvementStockResponse> list = mouvementStockMapper.toResponseList(List.of(movement));
        assertThat(list).hasSize(1);

        // null checks
        assertThat(mouvementStockMapper.toResponse(null)).isNull();
        assertThat(mouvementStockMapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("✅ CommandeClientMapper - toResponse & toLigneResponse & toResponseList")
    void testCommandeClientMapper() {
        Client client = new Client();
        client.setId(10L);
        client.setRaisonSociale("A Corp");

        Utilisateur user = Utilisateur.builder().id(11L).email("agent@pme.com").build();
        
        CommandeClient cmd = new CommandeClient();
        cmd.setId(100L);
        cmd.setNumeroCommande("CMD-100");
        cmd.setDateCommande(java.time.LocalDate.now());
        cmd.setStatut(StatutCommande.BROUILLON);
        cmd.setMontantHT(BigDecimal.valueOf(1000.00));
        cmd.setMontantTVA(BigDecimal.valueOf(180.00));
        cmd.setMontantTTC(BigDecimal.valueOf(1180.00));
        cmd.setTauxTVA(BigDecimal.valueOf(18.00));
        cmd.setClient(client);
        cmd.setTraitePar(user);

        CommandeClientResponse response = commandeClientMapper.toResponse(cmd);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getNumeroCommande()).isEqualTo("CMD-100");
        assertThat(response.getStatut()).isEqualTo(StatutCommande.BROUILLON);
        assertThat(response.getMontantHT()).isEqualTo(BigDecimal.valueOf(1000.00));
        assertThat(response.getMontantTVA()).isEqualTo(BigDecimal.valueOf(180.00));
        assertThat(response.getMontantTTC()).isEqualTo(BigDecimal.valueOf(1180.00));
        assertThat(response.getTauxTVA()).isEqualTo(BigDecimal.valueOf(18.00));
        assertThat(response.getClientId()).isEqualTo(10L);
        assertThat(response.getClientRaisonSociale()).isEqualTo("A Corp");
        assertThat(response.getTraitePar()).isEqualTo("agent@pme.com");

        // null properties check
        cmd.setClient(null);
        cmd.setTraitePar(null);
        cmd.setMontantHT(null);
        cmd.setMontantTVA(null);
        cmd.setMontantTTC(null);
        cmd.setTauxTVA(null);
        response = commandeClientMapper.toResponse(cmd);
        assertThat(response.getClientId()).isNull();
        assertThat(response.getClientRaisonSociale()).isNull();
        assertThat(response.getTraitePar()).isNull();
        assertThat(response.getMontantHT()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getMontantTVA()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getMontantTTC()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getTauxTVA()).isEqualTo(new BigDecimal("18.00"));

        // test toLigneResponse
        Produit prod = Produit.builder().id(99L).reference("REF-P").designation("Smartphone").build();
        LigneCommandeClient ligne = new LigneCommandeClient();
        ligne.setId(200L);
        ligne.setProduit(prod);
        ligne.setQuantite(2);
        ligne.setPrixUnitaireHT(BigDecimal.valueOf(500.00));
        ligne.setMontantLigneHT(BigDecimal.valueOf(1000.00));

        LigneCommandeResponse ligneResponse = commandeClientMapper.toLigneResponse(ligne);
        assertThat(ligneResponse).isNotNull();
        assertThat(ligneResponse.getProduitId()).isEqualTo(99L);
        assertThat(ligneResponse.getProduitReference()).isEqualTo("REF-P");
        assertThat(ligneResponse.getProduitDesignation()).isEqualTo("Smartphone");
        assertThat(ligneResponse.getPrixUnitaireHT()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(ligneResponse.getMontantLigneHT()).isEqualTo(BigDecimal.valueOf(1000.00));

        // null fields on ligne
        ligne.setProduit(null);
        ligne.setPrixUnitaireHT(null);
        ligne.setMontantLigneHT(null);
        ligneResponse = commandeClientMapper.toLigneResponse(ligne);
        assertThat(ligneResponse.getProduitId()).isNull();
        assertThat(ligneResponse.getProduitReference()).isNull();
        assertThat(ligneResponse.getProduitDesignation()).isNull();
        assertThat(ligneResponse.getPrixUnitaireHT()).isEqualTo(BigDecimal.ZERO);
        assertThat(ligneResponse.getMontantLigneHT()).isEqualTo(BigDecimal.ZERO);

        List<CommandeClientResponse> list = commandeClientMapper.toResponseList(List.of(cmd));
        assertThat(list).hasSize(1);

        // null checks
        assertThat(commandeClientMapper.toResponse(null)).isNull();
        assertThat(commandeClientMapper.toLigneResponse(null)).isNull();
        assertThat(commandeClientMapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("✅ FournisseurMapper - toResponse & toResponseList & toEntity")
    void testFournisseurMapper() {
        Fournisseur fournisseur = Fournisseur.builder()
                .id(1L)
                .code("FOUR-001")
                .raisonSociale("Dakar Fournitures SARL")
                .email("contact@dakarfournitures.sn")
                .actif(true)
                .produits(null)
                .commandes(null)
                .build();

        FournisseurResponse response = fournisseurMapper.toResponse(fournisseur);
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("FOUR-001");
        assertThat(response.getNombreProduits()).isEqualTo(0);
        assertThat(response.getNombreCommandes()).isEqualTo(0);

        fournisseur.setProduits(List.of(new Produit(), new Produit()));
        fournisseur.setCommandes(List.of(new CommandeFournisseur()));
        response = fournisseurMapper.toResponse(fournisseur);
        assertThat(response.getNombreProduits()).isEqualTo(2);
        assertThat(response.getNombreCommandes()).isEqualTo(1);

        List<FournisseurResponse> list = fournisseurMapper.toResponseList(List.of(fournisseur));
        assertThat(list).hasSize(1);

        FournisseurRequest request = new FournisseurRequest();
        request.setCode("FOUR-002");
        request.setRaisonSociale("Nouveau Fournisseur");
        request.setEmail("nouveau@test.sn");
        Fournisseur entity = fournisseurMapper.toEntity(request);
        assertThat(entity.getCode()).isEqualTo("FOUR-002");
        assertThat(entity.getRaisonSociale()).isEqualTo("Nouveau Fournisseur");
    }

    @Test
    @DisplayName("✅ CommandeFournisseurMapper - toResponse & toLigneResponse")
    void testCommandeFournisseurMapper() {
        Fournisseur fournisseur = Fournisseur.builder().id(1L).raisonSociale("Dakar Fournitures SARL").build();
        Utilisateur utilisateur = Utilisateur.builder().id(1L).email("admin@pme.com").build();
        Produit produit = Produit.builder().id(10L).reference("PROD-010").designation("Ramette papier").build();

        LigneCommandeFournisseur ligne = LigneCommandeFournisseur.builder()
                .id(1L)
                .quantiteCommandee(50)
                .quantiteRecue(50)
                .prixUnitaireAchat(new BigDecimal("1500"))
                .montantLigneHT(new BigDecimal("75000"))
                .produit(produit)
                .build();

        CommandeFournisseur commande = CommandeFournisseur.builder()
                .id(1L)
                .numeroCommande("CF-2026-00001")
                .statut(StatutCommandeFournisseur.RECUE)
                .fournisseur(fournisseur)
                .creePar(utilisateur)
                .lignes(List.of(ligne))
                .build();

        CommandeFournisseurResponse response = commandeFournisseurMapper.toResponse(commande);
        assertThat(response.getFournisseurId()).isEqualTo(1L);
        assertThat(response.getFournisseurRaisonSociale()).isEqualTo("Dakar Fournitures SARL");
        assertThat(response.getCreePar()).isEqualTo("admin@pme.com");

        // Sans utilisateur (creePar null)
        commande.setCreePar(null);
        response = commandeFournisseurMapper.toResponse(commande);
        assertThat(response.getCreePar()).isNull();

        LigneCommandeFournisseurResponse ligneResponse = commandeFournisseurMapper.toLigneResponse(ligne);
        assertThat(ligneResponse.getProduitId()).isEqualTo(10L);
        assertThat(ligneResponse.getProduitReference()).isEqualTo("PROD-010");
        assertThat(ligneResponse.getReceptionComplete()).isTrue();

        // Null checks
        assertThat(commandeFournisseurMapper.toResponse(null)).isNull();
        assertThat(commandeFournisseurMapper.toLigneResponse(null)).isNull();

        // Test with null produit on ligne
        ligne.setProduit(null);
        ligneResponse = commandeFournisseurMapper.toLigneResponse(ligne);
        assertThat(ligneResponse.getProduitId()).isNull();
        assertThat(ligneResponse.getProduitReference()).isNull();
        assertThat(ligneResponse.getProduitDesignation()).isNull();
    }
}
