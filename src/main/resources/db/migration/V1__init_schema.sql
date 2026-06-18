-- =====================================================
-- V1 : Schéma initial - Gestion Stock PME
-- =====================================================

-- Table des rôles
CREATE TABLE roles (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom        VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- Table des utilisateurs
CREATE TABLE utilisateurs (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom                  VARCHAR(100) NOT NULL,
    prenom               VARCHAR(100) NOT NULL,
    email                VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe         VARCHAR(255) NOT NULL,
    actif                BOOLEAN     DEFAULT TRUE,
    created_at           TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by           VARCHAR(150),
    updated_by           VARCHAR(150)
);

-- Table de liaison utilisateurs <-> rôles
CREATE TABLE utilisateurs_roles (
    utilisateur_id BIGINT NOT NULL,
    role_id        BIGINT NOT NULL,
    PRIMARY KEY (utilisateur_id, role_id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id)        REFERENCES roles(id)        ON DELETE CASCADE
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(250) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,
    utilisateur_id BIGINT   NOT NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- Table des catégories de produits
CREATE TABLE categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    libelle     VARCHAR(100) NOT NULL,
    description TEXT,
    actif       BOOLEAN     DEFAULT TRUE,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(150),
    updated_by  VARCHAR(150)
);

-- Table des produits
CREATE TABLE produits (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference          VARCHAR(100)   NOT NULL UNIQUE,
    designation        VARCHAR(255)   NOT NULL,
    description        TEXT,
    prix_achat         DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    prix_vente         DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    quantite_stock     INT            NOT NULL DEFAULT 0,
    seuil_alerte       INT            NOT NULL DEFAULT 5,
    unite_mesure       VARCHAR(50)    DEFAULT 'unité',
    actif              BOOLEAN        DEFAULT TRUE,
    categorie_id       BIGINT,
    created_at         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by         VARCHAR(150),
    updated_by         VARCHAR(150),
    FOREIGN KEY (categorie_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT chk_prix_achat  CHECK (prix_achat  >= 0),
    CONSTRAINT chk_prix_vente  CHECK (prix_vente  >= 0),
    CONSTRAINT chk_quantite    CHECK (quantite_stock >= 0),
    CONSTRAINT chk_seuil       CHECK (seuil_alerte >= 0)
);

-- Mouvements de stock (entrée / sortie / ajustement)
CREATE TABLE mouvements_stock (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_mouvement ENUM('ENTREE', 'SORTIE', 'AJUSTEMENT') NOT NULL,
    quantite       INT            NOT NULL,
    motif          VARCHAR(255),
    produit_id     BIGINT         NOT NULL,
    utilisateur_id BIGINT,
    created_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(150),
    FOREIGN KEY (produit_id)     REFERENCES produits(id)     ON DELETE RESTRICT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE SET NULL,
    CONSTRAINT chk_quantite_mouvement CHECK (quantite > 0)
);

-- Indexes pour performances
CREATE INDEX idx_produits_reference     ON produits(reference);
CREATE INDEX idx_produits_categorie     ON produits(categorie_id);
CREATE INDEX idx_produits_stock_alert   ON produits(quantite_stock, seuil_alerte);
CREATE INDEX idx_mouvements_produit     ON mouvements_stock(produit_id);
CREATE INDEX idx_mouvements_date        ON mouvements_stock(created_at);
CREATE INDEX idx_utilisateurs_email     ON utilisateurs(email);
