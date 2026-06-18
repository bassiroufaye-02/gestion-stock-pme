CREATE TABLE clients (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(20)  NOT NULL UNIQUE,
    raison_sociale VARCHAR(200) NOT NULL,
    email          VARCHAR(150) UNIQUE,
    telephone      VARCHAR(20),
    adresse        VARCHAR(500),
    ville          VARCHAR(100),
    actif          BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by     VARCHAR(150),
    updated_by     VARCHAR(150)
);

CREATE TABLE commandes_clients (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_commande       VARCHAR(20)    NOT NULL UNIQUE,
    date_commande         DATE           NOT NULL,
    date_livraison_prevue DATE,
    date_livraison_reelle DATE,
    statut                ENUM('BROUILLON','CONFIRMEE','EN_PREPARATION','EXPEDIEE','LIVREE','ANNULEE') NOT NULL DEFAULT 'BROUILLON',
    montant_ht            DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    montant_tva           DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    montant_ttc           DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    taux_tva              DECIMAL(5,2)   NOT NULL DEFAULT 18.00,
    notes                 TEXT,
    client_id             BIGINT         NOT NULL,
    traite_par_id         BIGINT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by            VARCHAR(150),
    updated_by            VARCHAR(150),
    FOREIGN KEY (client_id)     REFERENCES clients(id) ON DELETE RESTRICT,
    FOREIGN KEY (traite_par_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
);

CREATE TABLE lignes_commandes_clients (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantite          INT            NOT NULL CHECK (quantite > 0),
    prix_unitaire_ht  DECIMAL(10,2)  NOT NULL,
    montant_ligne_ht  DECIMAL(12,2)  NOT NULL,
    commande_id       BIGINT         NOT NULL,
    produit_id        BIGINT         NOT NULL,
    FOREIGN KEY (commande_id) REFERENCES commandes_clients(id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id)  REFERENCES produits(id) ON DELETE RESTRICT
);

CREATE INDEX idx_commandes_client  ON commandes_clients(client_id);
CREATE INDEX idx_commandes_statut  ON commandes_clients(statut);
CREATE INDEX idx_commandes_date    ON commandes_clients(date_commande);
CREATE INDEX idx_lignes_commande   ON lignes_commandes_clients(commande_id);
CREATE INDEX idx_lignes_produit    ON lignes_commandes_clients(produit_id);
