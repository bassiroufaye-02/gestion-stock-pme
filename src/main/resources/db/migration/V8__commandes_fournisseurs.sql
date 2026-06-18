-- V8__commandes_fournisseurs.sql

-- ============================================================
-- CONVERSION InnoDB (WAMP utilise MyISAM par défaut)
-- Les FK des migrations V1-V7 ont été ignorées sur MyISAM.
-- ============================================================
ALTER TABLE roles ENGINE=InnoDB;
ALTER TABLE utilisateurs ENGINE=InnoDB;
ALTER TABLE utilisateurs_roles ENGINE=InnoDB;
ALTER TABLE refresh_tokens ENGINE=InnoDB;
ALTER TABLE categories ENGINE=InnoDB;
ALTER TABLE produits ENGINE=InnoDB;
ALTER TABLE mouvements_stock ENGINE=InnoDB;
ALTER TABLE clients ENGINE=InnoDB;
ALTER TABLE commandes_clients ENGINE=InnoDB;
ALTER TABLE lignes_commandes_clients ENGINE=InnoDB;

-- ============================================================
-- TABLE COMMANDES FOURNISSEURS
-- ============================================================
CREATE TABLE commandes_fournisseurs (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    numero_commande         VARCHAR(30)     NOT NULL,
    date_commande           DATE            NOT NULL,
    date_commande_prevue    DATE,
    date_reception          DATE,
    statut                  VARCHAR(30)     NOT NULL DEFAULT 'BROUILLON',
    montant_ht              DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    montant_tva             DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    montant_ttc             DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    taux_tva                DECIMAL(5,2)    NOT NULL DEFAULT 18.00,
    notes                   TEXT,
    fournisseur_id          BIGINT          NOT NULL,
    cree_par_id             BIGINT,
    created_at              DATETIME(6),
    updated_at              DATETIME(6),
    created_by              VARCHAR(150),
    updated_by              VARCHAR(150),
    PRIMARY KEY (id),
    UNIQUE KEY uk_commandes_fournisseurs_numero (numero_commande),
    CONSTRAINT fk_commandes_four_fournisseur FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id) ON DELETE RESTRICT,
    CONSTRAINT fk_commandes_four_utilisateur FOREIGN KEY (cree_par_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE LIGNES COMMANDES FOURNISSEURS
-- ============================================================
CREATE TABLE lignes_commandes_fournisseurs (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    quantite_commandee  INT             NOT NULL,
    quantite_recue      INT             NOT NULL DEFAULT 0,
    prix_unitaire_achat DECIMAL(15,2)   NOT NULL,
    montant_ligne_ht    DECIMAL(15,2),
    commande_id         BIGINT          NOT NULL,
    produit_id          BIGINT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_lgn_four_commande FOREIGN KEY (commande_id) REFERENCES commandes_fournisseurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_lgn_four_produit FOREIGN KEY (produit_id) REFERENCES produits(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
