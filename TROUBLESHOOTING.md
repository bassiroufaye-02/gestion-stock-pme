# Guide de Dépannage - Erreur 500

## Erreur: `POST /api/v1/auth/connexion` retourne 500

### Causes possibles et solutions

#### 1. **Base de données non disponible**
- **Symptôme**: Impossible de se connecter à MySQL
- **Solutions**:
  - Vérifiez que WAMP/XAMPP est en cours d'exécution
  - Assurez-vous que MySQL sur le port 3306
  - Testez : `mysql -u root -h localhost` dans la console
  - Vérifiez la configuration dans `application-dev.yml` (ligne 3)

#### 2. **Migrations Flyway échouées**
- **Symptôme**: Erreurs de schéma de base de données
- **Vérification**:
  - Recherchez les erreurs dans les logs contenant "Flyway"
  - Vérifiez que les fichiers migration V1__* à V4__* existent dans `src/main/resources/db/migration`
  - **Solution**: Supprimez la base de données et relancez l'application
    ```sql
    DROP DATABASE gestion_stock_pme;
    ```

#### 3. **Utilisateur de test non créé**
- **Symptôme**: Erreur "Utilisateur introuvable" lors de la connexion
- **Solutions**:
  - La base doit avoir des données initiales (V2__data_initial.sql)
  - Insérez manuellement un utilisateur de test:
    ```sql
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, actif) 
    VALUES ('Admin', 'Test', 'admin@example.com', '$2a$10$...', true);
    ```

#### 4. **Erreur de chiffrement du mot de passe**
- **Symptôme**: AuthenticationManager échoue
- **Vérification**:
  - Vérifiez que BCryptPasswordEncoder est configuré
  - Les mots de passe en DB doivent être hachés en BCrypt
  - Regardez les logs pour "BadCredentialsException"

#### 5. **JWT Secret manquant ou invalide**
- **Symptôme**: Erreur lors de la génération du token JWT
- **Vérification**:
  - Vérifiez `app.jwt.secret` dans `application.yml` (ligne 22)
  - La clé doit être au moins 32 caractères

#### 6. **Roles de test non initialisés**
- **Symptôme**: Lors de l'inscription, erreur "Rôle inconnu"
- **Solution**:
  - Vérifiez que V2__data_initial.sql insère les rôles ROLE_ADMIN, ROLE_GESTIONNAIRE, ROLE_EMPLOYE
  - Requête de test:
    ```sql
    SELECT * FROM roles;
    ```

### Étapes de diagnostic

1. **Activez le logging DEBUG**:
   ```yaml
   logging:
     level:
       com.pme.stock: DEBUG
       org.springframework.security: DEBUG
   ```

2. **Consultez les logs complets**:
   - Recherchez les erreurs : `ERROR`, `Exception`, `Failed`

3. **Testez avec curl**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/connexion \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@example.com","motDePasse":"password"}'
   ```

4. **Vérifiez la santé de l'application**:
   ```
   GET http://localhost:8080/actuator/health
   ```

### Mots de passe de test

Les utilisateurs insérés par V 2__data_initial.sql:
- Email: `admin@example.com`
- Email: `gestionnaire@example.com`
- Email: `employe@example.com`

Les mots de passe sont configurés dans la migration (hachés en BCrypt).

### Logs à vérifier

Cherchez dans les logs:
```
[ERROR] - Indique une erreur
[WARN] Flyway - Problème de migration
[DEBUG] o.s.s.a.AuthenticationManager - Détails d'authentification
[DEBUG] com.pme.stock.service.impl.AuthService - Logs des services
```

### Code de sortie d'erreur 500 amélioré

Depuis la dernière mise à jour, les erreurs 500 contiennent:
- `exceptionType`: Le type exact de l'exception (ex: `NullPointerException`)
- `message`: Le message d'erreur détaillé
- `timestamp`: L'heure exacte

Exemple:
```json
{
  "type": "https://pme.com/errors/internal",
  "title": "Erreur interne",
  "status": 500,
  "detail": "Une erreur interne s'est produite",
  "exceptionType": "DataIntegrityViolationException",
  "message": "Utilisateur déjà existant",
  "timestamp": "2026-06-06T11:08:00Z"
}
```

### Besoin d'aide?

1. Copiez les logs complets
2. Exécutez: `mvn clean package -DskipTests` 
3. Vérifiez les fichiers de migration dans `target/classes/db/migration`
4. Assurez-vous que la DB est vide pour un redémarrage frais

