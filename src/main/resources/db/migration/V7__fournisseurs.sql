-- V7__fournisseurs.sql

-- ============================================================
-- TABLE FOURNISSEURS
-- ============================================================
CREATE TABLE fournisseurs (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    code            VARCHAR(20)     NOT NULL,
    raison_sociale  VARCHAR(200)    NOT NULL,
    email           VARCHAR(150)    UNIQUE,
    telephone       VARCHAR(20),
    adresse         VARCHAR(500),
    ville           VARCHAR(100),
    pays            VARCHAR(100)    DEFAULT 'Sénégal',
    actif           TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    created_by      VARCHAR(150),
    updated_by      VARCHAR(150),
    PRIMARY KEY (id),
    UNIQUE KEY uk_fournisseurs_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- ADD RELATION TO PRODUITS
-- ============================================================
ALTER TABLE produits
    ADD COLUMN fournisseur_id BIGINT NULL,
    ADD CONSTRAINT fk_produits_fournisseur FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs (id) ON DELETE SET NULL;

-- ============================================================
-- DONNÉES DE TEST
-- ============================================================
INSERT INTO fournisseurs (code, raison_sociale, email, telephone, ville, pays, actif, created_at, updated_at)
VALUES
    ('FOUR-001', 'Tech Supplies Dakar', 'contact@techsupplies.sn', '+221 33 820 00 00', 'Dakar', 'Sénégal', 1, NOW(), NOW()),
    ('FOUR-002', 'Global Import SARL', 'info@globalimport.sn', '+221 33 821 00 00', 'Thiès', 'Sénégal', 1, NOW(), NOW()),
    ('FOUR-003', 'Bureau Distribution SN', 'bd@bureaudist.sn', '+221 33 822 00 00', 'Dakar', 'Sénégal', 1, NOW(), NOW());
