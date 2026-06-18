package com.pme.stock.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("CommandeClient")
class CommandeClientTest {

    @Test
    @DisplayName("calculerMontants ignore les montants de ligne null")
    void calculerMontants_ligneSansMontant_neProvoquePasNpe() {
        CommandeClient commande = new CommandeClient();
        commande.setTauxTVA(new BigDecimal("18.00"));

        LigneCommandeClient ligne = new LigneCommandeClient();
        ligne.setQuantite(2);
        ligne.setPrixUnitaireHT(new BigDecimal("10.00"));
        commande.setLignes(List.of(ligne));

        assertThatCode(commande::calculerMontants).doesNotThrowAnyException();
        assertThat(commande.getMontantHT()).isEqualByComparingTo("20.00");
        assertThat(commande.getMontantTTC()).isEqualByComparingTo("23.60");
    }
}
