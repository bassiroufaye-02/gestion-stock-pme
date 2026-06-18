package com.pme.stock.dto;

import com.pme.stock.dto.response.*;
import com.pme.stock.dto.request.RefreshTokenRequest;
import com.pme.stock.dto.request.ReceptionCommandeRequest;
import com.pme.stock.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Response DTO Builders - Tests de couverture")
class BuildersTest {

    @Test
    @DisplayName("✅ AuthResponse Builder")
    void testAuthResponseBuilder() {
        AuthResponse res = AuthResponse.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .email("test@pme.com")
                .nomComplet("John Doe")
                .roles(Collections.singleton("ROLE_USER"))
                .build();

        assertThat(res.getAccessToken()).isEqualTo("token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh");
        assertThat(res.getTokenType()).isEqualTo("Bearer");
        assertThat(res.getExpiresIn()).isEqualTo(3600L);
        assertThat(res.getEmail()).isEqualTo("test@pme.com");
        assertThat(res.getNomComplet()).isEqualTo("John Doe");
        assertThat(res.getRoles()).contains("ROLE_USER");

        AuthResponse empty = new AuthResponse();
        empty.setAccessToken("token");
        assertThat(empty.getAccessToken()).isEqualTo("token");
    }

    @Test
    @DisplayName("✅ CategorieResponse Builder")
    void testCategorieResponseBuilder() {
        CategorieResponse res = CategorieResponse.builder()
                .id(1L)
                .code("CODE")
                .libelle("Libelle")
                .description("Desc")
                .actif(true)
                .nombreProduits(5)
                .build();

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getCode()).isEqualTo("CODE");
        assertThat(res.getLibelle()).isEqualTo("Libelle");
        assertThat(res.getDescription()).isEqualTo("Desc");
        assertThat(res.getActif()).isTrue();
        assertThat(res.getNombreProduits()).isEqualTo(5);


    }

    @Test
    @DisplayName("✅ ClientResponse Builder")
    void testClientResponseBuilder() {
        LocalDateTime now = LocalDateTime.now();
        ClientResponse res = new ClientResponse();
        res.setId(1L);
        res.setCode("C01");
        res.setRaisonSociale("Raison");
        res.setEmail("email@pme.com");
        res.setTelephone("12345");
        res.setAdresse("Adr");
        res.setVille("Dakar");
        res.setActif(true);
        res.setCreatedAt(now);

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getCode()).isEqualTo("C01");
        assertThat(res.getRaisonSociale()).isEqualTo("Raison");
        assertThat(res.getEmail()).isEqualTo("email@pme.com");
        assertThat(res.getTelephone()).isEqualTo("12345");
        assertThat(res.getAdresse()).isEqualTo("Adr");
        assertThat(res.getVille()).isEqualTo("Dakar");
        assertThat(res.getActif()).isTrue();
        assertThat(res.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("✅ CommandeClientResponse Builder")
    void testCommandeClientResponseBuilder() {
        LocalDate now = LocalDate.now();
        CommandeClientResponse res = new CommandeClientResponse();
        res.setId(1L);
        res.setNumeroCommande("CMD-01");
        res.setDateCommande(now);
        res.setStatut(com.pme.stock.entity.StatutCommande.BROUILLON);
        res.setMontantHT(BigDecimal.TEN);
        res.setMontantTVA(BigDecimal.ONE);
        res.setMontantTTC(BigDecimal.valueOf(11));
        res.setTauxTVA(BigDecimal.valueOf(10));
        res.setClientId(2L);
        res.setClientRaisonSociale("Client");
        res.setTraitePar("User");
        res.setLignes(Collections.emptyList());

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getNumeroCommande()).isEqualTo("CMD-01");
        assertThat(res.getDateCommande()).isEqualTo(now);
        assertThat(res.getStatut()).isEqualTo(com.pme.stock.entity.StatutCommande.BROUILLON);
        assertThat(res.getMontantHT()).isEqualTo(BigDecimal.TEN);
        assertThat(res.getMontantTVA()).isEqualTo(BigDecimal.ONE);
        assertThat(res.getMontantTTC()).isEqualTo(BigDecimal.valueOf(11));
        assertThat(res.getTauxTVA()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(res.getClientId()).isEqualTo(2L);
        assertThat(res.getClientRaisonSociale()).isEqualTo("Client");
        assertThat(res.getTraitePar()).isEqualTo("User");
        assertThat(res.getLignes()).isEmpty();
    }

    @Test
    @DisplayName("✅ LigneCommandeResponse Builder")
    void testLigneCommandeResponseBuilder() {
        LigneCommandeResponse res = new LigneCommandeResponse();
        res.setId(1L);
        res.setProduitId(2L);
        res.setProduitReference("P01");
        res.setProduitDesignation("Prod");
        res.setQuantite(3);
        res.setPrixUnitaireHT(BigDecimal.TEN);
        res.setMontantLigneHT(BigDecimal.valueOf(30));

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getProduitId()).isEqualTo(2L);
        assertThat(res.getProduitReference()).isEqualTo("P01");
        assertThat(res.getProduitDesignation()).isEqualTo("Prod");
        assertThat(res.getQuantite()).isEqualTo(3);
        assertThat(res.getPrixUnitaireHT()).isEqualTo(BigDecimal.TEN);
        assertThat(res.getMontantLigneHT()).isEqualTo(BigDecimal.valueOf(30));
    }

    @Test
    @DisplayName("✅ MouvementStockResponse Builder")
    void testMouvementStockResponseBuilder() {
        LocalDateTime now = LocalDateTime.now();
        MouvementStockResponse res = MouvementStockResponse.builder()
                .id(1L)
                .typeMouvement(com.pme.stock.entity.MouvementStock.TypeMouvement.ENTREE)
                .quantite(10)
                .motif("Motif")
                .produitId(2L)
                .produitReference("P01")
                .produitDesignation("Prod")
                .utilisateurEmail("email@pme.com")
                .createdAt(now)
                .build();

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getTypeMouvement()).isEqualTo(com.pme.stock.entity.MouvementStock.TypeMouvement.ENTREE);
        assertThat(res.getQuantite()).isEqualTo(10);
        assertThat(res.getMotif()).isEqualTo("Motif");
        assertThat(res.getProduitId()).isEqualTo(2L);
        assertThat(res.getProduitReference()).isEqualTo("P01");
        assertThat(res.getProduitDesignation()).isEqualTo("Prod");
        assertThat(res.getUtilisateurEmail()).isEqualTo("email@pme.com");
        assertThat(res.getCreatedAt()).isEqualTo(now);

        MouvementStockResponse empty = new MouvementStockResponse();
        empty.setId(1L);
        assertThat(empty.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("✅ ProduitResponse Builder")
    void testProduitResponseBuilder() {
        LocalDateTime now = LocalDateTime.now();
        ProduitResponse res = ProduitResponse.builder()
                .id(1L)
                .reference("REF")
                .designation("Desg")
                .description("Desc")
                .prixAchat(BigDecimal.ONE)
                .prixVente(BigDecimal.TEN)
                .quantiteStock(10)
                .seuilAlerte(3)
                .uniteMesure("Unit")
                .actif(true)
                .enAlerte(false)
                .enRupture(false)
                .categorieId(2L)
                .categorieLibelle("Cat")
                .createdAt(now)
                .updatedAt(now)
                .createdBy("User")
                .build();

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getReference()).isEqualTo("REF");
        assertThat(res.getDesignation()).isEqualTo("Desg");
        assertThat(res.getDescription()).isEqualTo("Desc");
        assertThat(res.getPrixAchat()).isEqualTo(BigDecimal.ONE);
        assertThat(res.getPrixVente()).isEqualTo(BigDecimal.TEN);
        assertThat(res.getQuantiteStock()).isEqualTo(10);
        assertThat(res.getSeuilAlerte()).isEqualTo(3);
        assertThat(res.getUniteMesure()).isEqualTo("Unit");
        assertThat(res.getActif()).isTrue();
        assertThat(res.getEnAlerte()).isFalse();
        assertThat(res.getEnRupture()).isFalse();
        assertThat(res.getCategorieId()).isEqualTo(2L);
        assertThat(res.getCategorieLibelle()).isEqualTo("Cat");
        assertThat(res.getCreatedAt()).isEqualTo(now);
        assertThat(res.getUpdatedAt()).isEqualTo(now);
        assertThat(res.getCreatedBy()).isEqualTo("User");


    }

    @Test
    @DisplayName("✅ PageResponse Builder")
    void testPageResponseBuilder() {
        Page<String> page = new PageImpl<>(List.of("A", "B"), PageRequest.of(0, 10), 2);
        PageResponse<String> res = PageResponse.from(page);

        assertThat(res.getContent()).containsExactly("A", "B");
        assertThat(res.getPage()).isEqualTo(0);
        assertThat(res.getSize()).isEqualTo(10);
        assertThat(res.getTotalElements()).isEqualTo(2L);
        assertThat(res.getTotalPages()).isEqualTo(1);
        assertThat(res.isFirst()).isTrue();
        assertThat(res.isLast()).isTrue();
    }

    @Test
    @DisplayName("✅ MouvementStockResponse fromEntity")
    void testMouvementStockResponseFromEntity() {
        Produit produit = Produit.builder().id(2L).reference("REF").designation("Desg").build();
        Utilisateur user = Utilisateur.builder().id(3L).email("user@pme.com").build();
        MouvementStock movement = MouvementStock.builder()
                .id(1L)
                .typeMouvement(MouvementStock.TypeMouvement.ENTREE)
                .quantite(10)
                .motif("Motif")
                .produit(produit)
                .utilisateur(user)
                .createdAt(LocalDateTime.now())
                .createdBy("creator")
                .build();

        MouvementStockResponse res = MouvementStockResponse.fromEntity(movement);
        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getProduitId()).isEqualTo(2L);
        assertThat(res.getUtilisateurEmail()).isEqualTo("user@pme.com");

        // Null case
        MouvementStock movementNull = MouvementStock.builder().build();
        MouvementStockResponse resNull = MouvementStockResponse.fromEntity(movementNull);
        assertThat(resNull.getProduitId()).isNull();
        assertThat(resNull.getUtilisateurEmail()).isNull();
    }

    @Test
    @DisplayName("✅ RefreshTokenRequest cover")
    void testRefreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token");
        assertThat(request.getRefreshToken()).isEqualTo("token");
        assertThat(request.toString()).contains("token");

        RefreshTokenRequest other = new RefreshTokenRequest();
        other.setRefreshToken("token");
        assertThat(request).isEqualTo(other);
        assertThat(request.hashCode()).isEqualTo(other.hashCode());
    }

    @Test
    @DisplayName("✅ FournisseurResponse Builder")
    void testFournisseurResponseBuilder() {
        FournisseurResponse res = FournisseurResponse.builder()
                .id(1L)
                .code("FOUR-001")
                .raisonSociale("Dakar Fournitures SARL")
                .email("contact@dakarfournitures.sn")
                .telephone("+221 77 000 0001")
                .adresse("Rue 10, Plateau")
                .ville("Dakar")
                .pays("Sénégal")
                .actif(true)
                .nombreProduits(5)
                .nombreCommandes(3)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getCode()).isEqualTo("FOUR-001");
        assertThat(res.getRaisonSociale()).isEqualTo("Dakar Fournitures SARL");
        assertThat(res.getNombreProduits()).isEqualTo(5);
        assertThat(res.getNombreCommandes()).isEqualTo(3);
    }

    @Test
    @DisplayName("✅ CommandeFournisseurResponse Builder")
    void testCommandeFournisseurResponseBuilder() {
        LigneCommandeFournisseurResponse ligne = LigneCommandeFournisseurResponse.builder()
                .id(1L)
                .quantiteCommandee(50)
                .quantiteRecue(50)
                .prixUnitaireAchat(new BigDecimal("1500.00"))
                .montantLigneHT(new BigDecimal("75000.00"))
                .produitId(10L)
                .produitReference("PROD-010")
                .produitDesignation("Ramette papier A4")
                .receptionComplete(true)
                .build();

        CommandeFournisseurResponse res = CommandeFournisseurResponse.builder()
                .id(1L)
                .numeroCommande("CF-2026-00001")
                .dateCommande(LocalDate.now())
                .dateCommandePrevue(LocalDate.now().plusDays(7))
                .dateReception(LocalDate.now())
                .statut(StatutCommandeFournisseur.RECUE)
                .montantHT(new BigDecimal("75000.00"))
                .montantTVA(new BigDecimal("13500.00"))
                .montantTTC(new BigDecimal("88500.00"))
                .tauxTVA(new BigDecimal("18.00"))
                .notes("Livraison urgente")
                .fournisseurId(1L)
                .fournisseurRaisonSociale("Dakar Fournitures SARL")
                .creePar("admin@pme.com")
                .lignes(List.of(ligne))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(res.getNumeroCommande()).isEqualTo("CF-2026-00001");
        assertThat(res.getStatut()).isEqualTo(StatutCommandeFournisseur.RECUE);
        assertThat(res.getLignes()).hasSize(1);
        assertThat(res.getLignes().get(0).getReceptionComplete()).isTrue();

        // Constructeur vide + setters (pattern existant dans le fichier)
        CommandeFournisseurResponse empty = new CommandeFournisseurResponse();
        empty.setNumeroCommande("CF-2026-00002");
        assertThat(empty.getNumeroCommande()).isEqualTo("CF-2026-00002");
    }

    @Test
    @DisplayName("✅ DashboardResponse Builder")
    void testDashboardResponseBuilder() {
        StockValeurResponse stock = StockValeurResponse.builder()
                .valeurTotaleAchat(new BigDecimal("4500000"))
                .valeurTotaleVente(new BigDecimal("6200000"))
                .margePotentielle(new BigDecimal("1700000"))
                .nombreProduitsActifs(142)
                .quantiteTotaleStock(8450)
                .build();

        CommandeStatutStatResponse stat = CommandeStatutStatResponse.builder()
                .statut(StatutCommande.LIVREE)
                .nombreCommandes(37)
                .montantTotalTTC(new BigDecimal("2150000"))
                .build();

        ProduitTopVenteResponse top = ProduitTopVenteResponse.builder()
                .produitId(12L)
                .reference("PROD-0012")
                .designation("Ramette papier A4 80g")
                .quantiteTotaleCommandee(320)
                .chiffreAffairesHT(new BigDecimal("960000"))
                .build();

        ChiffreAffairesMoisResponse ca = ChiffreAffairesMoisResponse.builder()
                .annee(2026).mois(6)
                .chiffreAffairesTTC(new BigDecimal("3450000"))
                .nombreCommandesLivrees(28)
                .build();

        DashboardResponse dashboard = DashboardResponse.builder()
                .genereLe(LocalDateTime.now())
                .stock(stock)
                .produitsEnAlerte(Collections.emptyList())
                .nombreProduitsEnRupture(2)
                .commandesParStatut(List.of(stat))
                .topProduits(List.of(top))
                .chiffreAffairesMoisCourant(ca)
                .nombreFournisseursActifs(3)
                .nombreCommandesFournisseursEnAttente(1)
                .build();

        assertThat(dashboard.getStock().getMargePotentielle()).isEqualTo(new BigDecimal("1700000"));
        assertThat(dashboard.getCommandesParStatut()).hasSize(1);
        assertThat(dashboard.getTopProduits().get(0).getReference()).isEqualTo("PROD-0012");
        assertThat(dashboard.getNombreFournisseursActifs()).isEqualTo(3);
    }

    @Test
    @DisplayName("✅ StockValeurResponse / CommandeStatutStatResponse / ProduitTopVenteResponse / ChiffreAffairesMoisResponse - Setters")
    void testDashboardDtosSetters() {
        StockValeurResponse s = new StockValeurResponse();
        s.setValeurTotaleAchat(BigDecimal.TEN);
        assertThat(s.getValeurTotaleAchat()).isEqualTo(BigDecimal.TEN);

        CommandeStatutStatResponse c = new CommandeStatutStatResponse();
        c.setNombreCommandes(5);
        assertThat(c.getNombreCommandes()).isEqualTo(5);

        ProduitTopVenteResponse p = new ProduitTopVenteResponse();
        p.setReference("REF-1");
        assertThat(p.getReference()).isEqualTo("REF-1");

        ChiffreAffairesMoisResponse ca = new ChiffreAffairesMoisResponse();
        ca.setAnnee(2026);
        assertThat(ca.getAnnee()).isEqualTo(2026);
    }

    @Test
    @DisplayName("✅ ReceptionCommandeRequest - Setters")
    void testReceptionCommandeRequestSetters() {
        ReceptionCommandeRequest request = new ReceptionCommandeRequest();
        request.setCommandeId(1L);
        request.setNotes("Réception complète");

        ReceptionCommandeRequest.ReceptionLigneRequest ligne = new ReceptionCommandeRequest.ReceptionLigneRequest();
        ligne.setLigneId(10L);
        ligne.setQuantiteRecue(50);

        request.setLignes(List.of(ligne));

        assertThat(request.getCommandeId()).isEqualTo(1L);
        assertThat(request.getNotes()).isEqualTo("Réception complète");
        assertThat(request.getLignes()).hasSize(1);
        assertThat(request.getLignes().get(0).getLigneId()).isEqualTo(10L);
        assertThat(request.getLignes().get(0).getQuantiteRecue()).isEqualTo(50);
    }

    @Test
    @DisplayName("✅ LigneCommandeFournisseurResponse - Additional coverage")
    void testLigneCommandeFournisseurResponseAdditional() {
        LigneCommandeFournisseurResponse ligne = new LigneCommandeFournisseurResponse();
        ligne.setId(1L);
        ligne.setQuantiteCommandee(100);
        ligne.setQuantiteRecue(50);
        ligne.setPrixUnitaireAchat(new BigDecimal("1000"));
        ligne.setMontantLigneHT(new BigDecimal("50000"));
        ligne.setProduitId(5L);
        ligne.setProduitReference("PROD-005");
        ligne.setProduitDesignation("Produit Test");
        ligne.setReceptionComplete(false);

        assertThat(ligne.getId()).isEqualTo(1L);
        assertThat(ligne.getQuantiteCommandee()).isEqualTo(100);
        assertThat(ligne.getQuantiteRecue()).isEqualTo(50);
        assertThat(ligne.getPrixUnitaireAchat()).isEqualTo(new BigDecimal("1000"));
        assertThat(ligne.getMontantLigneHT()).isEqualTo(new BigDecimal("50000"));
        assertThat(ligne.getProduitId()).isEqualTo(5L);
        assertThat(ligne.getProduitReference()).isEqualTo("PROD-005");
        assertThat(ligne.getProduitDesignation()).isEqualTo("Produit Test");
        assertThat(ligne.getReceptionComplete()).isFalse();
    }
}
