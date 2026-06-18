-- =====================================================
-- V2 : Données initiales
-- =====================================================

-- Insertion des rôles
INSERT INTO roles (nom) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_GESTIONNAIRE'),
    ('ROLE_EMPLOYE');

-- Insertion de l'admin par défaut
-- Mot de passe : Admin@123 (BCrypt encodé)
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, actif, created_by) VALUES
    ('Admin', 'Système', 'admin@pme.com', '$2a$12$DAGHY8lfuKZbcpLbTH7ZLOYxINdltV7/RBnlA2fMeA5giKtsSv/tS', TRUE, 'SYSTEM');

-- Attribution du rôle ADMIN à l'utilisateur admin
INSERT INTO utilisateurs_roles (utilisateur_id, role_id)
SELECT u.id, r.id FROM utilisateurs u, roles r
WHERE u.email = 'admin@pme.com' AND r.nom = 'ROLE_ADMIN';

-- Catégories de démo
INSERT INTO categories (code, libelle, description, created_by) VALUES
    ('INFO', 'Informatique',       'Matériel et accessoires informatiques', 'SYSTEM'),
    ('BURO', 'Bureautique',        'Fournitures de bureau',                  'SYSTEM'),
    ('ELEC', 'Électronique',       'Composants et équipements électroniques','SYSTEM');

-- Produits de démo
INSERT INTO produits (reference, designation, prix_achat, prix_vente, quantite_stock, seuil_alerte, categorie_id, created_by)
SELECT 'PROD-001', 'Ordinateur Portable 15"',  450.00, 750.00, 10, 3, c.id, 'SYSTEM' FROM categories c WHERE c.code = 'INFO';

INSERT INTO produits (reference, designation, prix_achat, prix_vente, quantite_stock, seuil_alerte, categorie_id, created_by)
SELECT 'PROD-002', 'Souris sans fil Logitech', 12.00,  25.00,  2,  5, c.id, 'SYSTEM' FROM categories c WHERE c.code = 'INFO';

INSERT INTO produits (reference, designation, prix_achat, prix_vente, quantite_stock, seuil_alerte, categorie_id, created_by)
SELECT 'PROD-003', 'Ramette papier A4 500f',   3.50,   7.00,   50, 10, c.id, 'SYSTEM' FROM categories c WHERE c.code = 'BURO';
