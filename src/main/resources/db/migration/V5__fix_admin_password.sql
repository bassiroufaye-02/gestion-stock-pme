-- Correction du hash BCrypt du compte admin (mot de passe : Admin@123)
UPDATE utilisateurs
SET mot_de_passe = '$2a$12$DAGHY8lfuKZbcpLbTH7ZLOYxINdltV7/RBnlA2fMeA5giKtsSv/tS'
WHERE email = 'admin@pme.com';
