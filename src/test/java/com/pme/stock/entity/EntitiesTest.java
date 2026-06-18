package com.pme.stock.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA Entities - Tests de couverture")
class EntitiesTest {

    @Test
    @DisplayName("✅ Role - Coverage")
    void testRoleCoverage() {
        Role role = Role.builder().id(1L).nom("ROLE_ADMIN").build();
        assertThat(role.getId()).isEqualTo(1L);
        assertThat(role.getNom()).isEqualTo("ROLE_ADMIN");

        Role other = new Role();
        other.setId(1L);
        other.setNom("ROLE_ADMIN");
        assertThat(other.getId()).isEqualTo(1L);
        assertThat(other.getNom()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("✅ RefreshToken - Coverage")
    void testRefreshTokenCoverage() {
        Instant future = Instant.now().plusSeconds(3600);
        Instant past = Instant.now().minusSeconds(3600);
        Utilisateur user = Utilisateur.builder().id(1L).email("test@pme.com").build();

        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .token("uuid-token")
                .expiryDate(future)
                .utilisateur(user)
                .build();

        assertThat(token.getId()).isEqualTo(1L);
        assertThat(token.getToken()).isEqualTo("uuid-token");
        assertThat(token.getExpiryDate()).isEqualTo(future);
        assertThat(token.getUtilisateur()).isEqualTo(user);
        assertThat(token.isExpire()).isFalse();

        token.setExpiryDate(past);
        assertThat(token.isExpire()).isTrue();

        RefreshToken other = new RefreshToken();
        other.setId(2L);
        other.setToken("other-token");
        assertThat(other.getId()).isEqualTo(2L);
        assertThat(other.getToken()).isEqualTo("other-token");
    }

    @Test
    @DisplayName("✅ LigneCommandeClient - Coverage")
    void testLigneCommandeClientCoverage() throws Exception {
        LigneCommandeClient ligne = new LigneCommandeClient();
        ligne.setId(1L);
        ligne.setQuantite(5);
        ligne.setPrixUnitaireHT(BigDecimal.TEN);
        
        CommandeClient commande = new CommandeClient();
        ligne.setCommande(commande);
        
        Produit produit = new Produit();
        ligne.setProduit(produit);

        assertThat(ligne.getId()).isEqualTo(1L);
        assertThat(ligne.getQuantite()).isEqualTo(5);
        assertThat(ligne.getPrixUnitaireHT()).isEqualTo(BigDecimal.TEN);
        assertThat(ligne.getCommande()).isEqualTo(commande);
        assertThat(ligne.getProduit()).isEqualTo(produit);

        // Test private lifecycle method using reflection
        Method method = LigneCommandeClient.class.getDeclaredMethod("synchroniserMontantLigneHT");
        method.setAccessible(true);
        method.invoke(ligne);
        assertThat(ligne.getMontantLigneHT()).isEqualByComparingTo("50.00");

        // Test branch when quantite is null
        ligne.setQuantite(null);
        ligne.setMontantLigneHT(null);
        method.invoke(ligne);
        assertThat(ligne.getMontantLigneHT()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("✅ CommandeClient - State and lifecycle coverage")
    void testCommandeClientCoverage() throws Exception {
        CommandeClient cmd = new CommandeClient();
        cmd.setId(1L);
        cmd.setNumeroCommande("CMD-01");
        cmd.setDateCommande(LocalDate.now());
        cmd.setDateLivraisonPrevue(LocalDate.now());
        cmd.setDateLivraisonReelle(LocalDate.now());
        cmd.setStatut(StatutCommande.BROUILLON);
        cmd.setNotes("Notes");
        
        Client client = new Client();
        cmd.setClient(client);
        
        Utilisateur user = new Utilisateur();
        cmd.setTraitePar(user);

        assertThat(cmd.getId()).isEqualTo(1L);
        assertThat(cmd.getNumeroCommande()).isEqualTo("CMD-01");
        assertThat(cmd.getDateCommande()).isNotNull();
        assertThat(cmd.getDateLivraisonPrevue()).isNotNull();
        assertThat(cmd.getDateLivraisonReelle()).isNotNull();
        assertThat(cmd.getStatut()).isEqualTo(StatutCommande.BROUILLON);
        assertThat(cmd.getNotes()).isEqualTo("Notes");
        assertThat(cmd.getClient()).isEqualTo(client);
        assertThat(cmd.getTraitePar()).isEqualTo(user);

        // State machine checks
        assertThat(cmd.peutEtreModifiee()).isTrue();
        assertThat(cmd.peutEtreAnnulee()).isTrue();
        assertThat(cmd.peutEtreConfirmee()).isFalse(); // no lines

        cmd.setStatut(StatutCommande.CONFIRMEE);
        assertThat(cmd.peutEtreModifiee()).isFalse();
        assertThat(cmd.peutEtreAnnulee()).isTrue();
        assertThat(cmd.peutEtreConfirmee()).isFalse();

        cmd.setStatut(StatutCommande.EXPEDIEE);
        assertThat(cmd.peutEtreModifiee()).isFalse();
        assertThat(cmd.peutEtreAnnulee()).isFalse();

        // calculerMontants checks with null lines
        cmd.setLignes(null);
        cmd.calculerMontants();
        assertThat(cmd.getMontantHT()).isEqualTo(BigDecimal.ZERO);

        // Reflection for normaliserMontantsApresChargement
        cmd.setMontantHT(null);
        cmd.setMontantTVA(null);
        cmd.setMontantTTC(null);
        cmd.setTauxTVA(null);

        Method method = CommandeClient.class.getDeclaredMethod("normaliserMontantsApresChargement");
        method.setAccessible(true);
        method.invoke(cmd);

        assertThat(cmd.getMontantHT()).isEqualTo(BigDecimal.ZERO);
        assertThat(cmd.getMontantTVA()).isEqualTo(BigDecimal.ZERO);
        assertThat(cmd.getMontantTTC()).isEqualTo(BigDecimal.ZERO);
        assertThat(cmd.getTauxTVA()).isEqualByComparingTo("18.00");
    }

    @Test
    @DisplayName("✅ Fournisseur - Coverage")
    void testFournisseurCoverage() {
        Fournisseur f = Fournisseur.builder()
                .id(1L)
                .code("FOUR-001")
                .raisonSociale("Dakar Fournitures SARL")
                .email("contact@dakarfournitures.sn")
                .telephone("+221 77 000 0001")
                .adresse("Rue 10")
                .ville("Dakar")
                .pays("Sénégal")
                .actif(true)
                .build();

        assertThat(f.getId()).isEqualTo(1L);
        assertThat(f.getCode()).isEqualTo("FOUR-001");
        assertThat(f.getPays()).isEqualTo("Sénégal");
        assertThat(f.getProduits()).isNotNull().isEmpty();
        assertThat(f.getCommandes()).isNotNull().isEmpty();

        Fournisseur other = new Fournisseur();
        other.setCode("FOUR-002");
        other.setActif(false);
        assertThat(other.getCode()).isEqualTo("FOUR-002");
        assertThat(other.getActif()).isFalse();
    }

    @Test
    @DisplayName("✅ CommandeFournisseur - Coverage + méthodes métier")
    void testCommandeFournisseurCoverage() {
        Fournisseur fournisseur = Fournisseur.builder().id(1L).actif(true).build();
        Produit produit = Produit.builder().id(1L).quantiteStock(10).seuilAlerte(5).actif(true).build();

        LigneCommandeFournisseur ligne = LigneCommandeFournisseur.builder()
                .quantiteCommandee(20)
                .quantiteRecue(0)
                .prixUnitaireAchat(new BigDecimal("1000"))
                .produit(produit)
                .build();
        ligne.calculerMontantLigneHT();
        assertThat(ligne.getMontantLigneHT()).isEqualTo(new BigDecimal("20000"));
        assertThat(ligne.estTotalementRecue()).isFalse();

        ligne.setQuantiteRecue(20);
        assertThat(ligne.estTotalementRecue()).isTrue();

        List<LigneCommandeFournisseur> lignes = new ArrayList<>(List.of(ligne));
        CommandeFournisseur commande = CommandeFournisseur.builder()
                .id(1L)
                .numeroCommande("CF-2026-00001")
                .statut(StatutCommandeFournisseur.BROUILLON)
                .fournisseur(fournisseur)
                .lignes(lignes)
                .tauxTVA(new BigDecimal("18.00"))
                .build();
        ligne.setCommande(commande);

        commande.calculerMontants();
        assertThat(commande.getMontantHT()).isEqualTo(new BigDecimal("20000"));
        assertThat(commande.getMontantTTC()).isGreaterThan(commande.getMontantHT());

        assertThat(commande.peutEtreModifiee()).isTrue();
        assertThat(commande.peutEtreEnvoyee()).isTrue();
        assertThat(commande.peutEtreReceptionnee()).isFalse();
        assertThat(commande.peutEtreAnnulee()).isTrue();

        commande.setStatut(StatutCommandeFournisseur.ENVOYEE);
        assertThat(commande.peutEtreReceptionnee()).isTrue();
        assertThat(commande.peutEtreModifiee()).isFalse();

        commande.setStatut(StatutCommandeFournisseur.RECUE);
        assertThat(commande.peutEtreAnnulee()).isFalse();

        CommandeFournisseur other = new CommandeFournisseur();
        other.setNumeroCommande("CF-2026-00002");
        assertThat(other.getNumeroCommande()).isEqualTo("CF-2026-00002");

        // Test additional setters for coverage
        other.setDateCommande(LocalDate.now());
        other.setDateCommandePrevue(LocalDate.now().plusDays(7));
        other.setDateReception(LocalDate.now().plusDays(7));
        other.setMontantHT(new BigDecimal("10000"));
        other.setMontantTVA(new BigDecimal("1800"));
        other.setMontantTTC(new BigDecimal("11800"));
        other.setNotes("Test notes");
        assertThat(other.getDateCommande()).isNotNull();
        assertThat(other.getMontantHT()).isEqualTo(new BigDecimal("10000"));

        // Test normaliserMontantsApresChargement via reflection
        other.setMontantHT(null);
        other.setMontantTVA(null);
        other.setMontantTTC(null);
        other.setTauxTVA(null);

        try {
            java.lang.reflect.Method method = CommandeFournisseur.class.getDeclaredMethod("normaliserMontantsApresChargement");
            method.setAccessible(true);
            method.invoke(other);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(other.getMontantHT()).isEqualTo(BigDecimal.ZERO);
        assertThat(other.getMontantTVA()).isEqualTo(BigDecimal.ZERO);
        assertThat(other.getMontantTTC()).isEqualTo(BigDecimal.ZERO);
        assertThat(other.getTauxTVA()).isEqualByComparingTo("18.00");
    }

    @Test
    @DisplayName("✅ LigneCommandeFournisseur - Coverage isolé")
    void testLigneCommandeFournisseurCoverage() {
        LigneCommandeFournisseur ligne = new LigneCommandeFournisseur();
        ligne.setId(1L);
        ligne.setQuantiteCommandee(10);
        ligne.setQuantiteRecue(0);
        ligne.setPrixUnitaireAchat(new BigDecimal("500"));

        assertThat(ligne.getId()).isEqualTo(1L);
        assertThat(ligne.estTotalementRecue()).isFalse();

        ligne.setQuantiteRecue(10);
        assertThat(ligne.estTotalementRecue()).isTrue();
    }

    @Test
    @DisplayName("✅ StatutCommandeFournisseur - Coverage enum")
    void testStatutCommandeFournisseurCoverage() {
        assertThat(StatutCommandeFournisseur.values()).contains(
                StatutCommandeFournisseur.BROUILLON,
                StatutCommandeFournisseur.ENVOYEE,
                StatutCommandeFournisseur.RECUE_PARTIELLE,
                StatutCommandeFournisseur.RECUE,
                StatutCommandeFournisseur.ANNULEE
        );
        assertThat(StatutCommandeFournisseur.valueOf("RECUE")).isEqualTo(StatutCommandeFournisseur.RECUE);
    }
}
