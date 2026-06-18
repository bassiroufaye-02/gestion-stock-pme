# 📦 Guide d'Installation Complet

## Prérequis Système

### Logiciels requis
- **Java 21** (JDK)
- **Maven 3.8+**
- **MySQL 8.0+** ou MariaDB 10.5+
- **WAMP** ou **XAMPP** (fourni avec MySQL et Apache)
- **Git** (optionnel, pour cloner le repo)

### Versions testées
- Java: OpenJDK 21.0.1
- Maven: 3.9.x
- MySQL: 8.0.35
- Spring Boot: 3.3.0

## 🗂️ Structure du Projet

```
gestion-stock-pme/
├── src/
│   ├── main/
│   │   ├── java/com/pme/stock/
│   │   │   ├── controller/         # REST Controllers
│   │   │   ├── service/            # Services métier
│   │   │   ├── entity/             # JPA Entities
│   │   │   ├── dto/                # DTOs (Request/Response)
│   │   │   ├── repository/         # Spring Data Repositories
│   │   │   ├── security/           # JWT, Security Config
│   │   │   └── exception/          # Exception Handlers
│   │   └── resources/
│   │       ├── db/migration/       # Flyway migrations (V1, V2, V3, V4)
│   │       ├── application.yml     # Config globale
│   │       ├── application-dev.yml # Config développement
│   │       └── application-prod.yml# Config production
│   └── test/
├── pom.xml                         # Maven configuration
├── README.md                       # Documentation générale
├── API_TESTING.md                  # Guide de test des APIs
├── TROUBLESHOOTING.md              # Guide de dépannage
└── Dockerfile, docker-compose.yml  # Pour containerisation

Entités principales:
  - Utilisateur (User)
  - Role (ROLE_ADMIN, ROLE_GESTIONNAIRE, ROLE_EMPLOYE)
  - Client (NEW)
  - CommandeClient (NEW)
  - LigneCommandeClient (NEW)
  - Produit
  - Categorie
  - MouvementStock
  - RefreshToken
```

## 🚀 Installation Étape par Étape

### Étape 1: Préparer la Base de Données

#### Option A: Avec WAMP (Windows)

1. **Lancez WAMP**:
   - Double-cliquez sur `wampmanager.exe`
   - Attendez que l'icône dans la barre d'état système devienne verte

2. **Accédez à phpMyAdmin**:
   - Clic droit sur l'icône WAMP → "phpMyAdmin"
   - Ou allez à: http://localhost/phpmyadmin

3. **Créez la base de données**:
   - Cliquez sur l'onglet "SQL"
   - Exécutez:
   ```sql
   CREATE DATABASE gestion_stock_pme CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

#### Option B: En ligne de commande

```bash
# Connectez-vous à MySQL
mysql -u root -h localhost

# Créez la base
CREATE DATABASE gestion_stock_pme CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

#### Option C: Auto-création par Flyway

L'application créera automatiquement la base si elle n'existe pas (voir `application-dev.yml`):
```yaml
datasource:
  url: jdbc:mysql://localhost:3306/gestion_stock_pme?createDatabaseIfNotExist=true
```

### Étape 2: Compiler l'Application

```bash
# Allez au répertoire du projet
cd C:\chemin\vers\gestion-stock-pme

# Compilez avec Maven
mvn clean compile -DskipTests

# Créez un package JAR
mvn clean package -DskipTests
```

### Étape 3: Initialiser la Base de Données

Les migrations Flyway s'exécutent automatiquement au démarrage:

| Migration | Description |
|-----------|-------------|
| V1__init_schema.sql | Schéma initial (tables, clés étrangères) |
| V2__data_initial.sql | Données de test (utilisateurs, rôles, produits) |
| V3__clients_et_commandes.sql | Tables clients et commandes (NEW) |
| V4__data_clients_demo.sql | Données de test clients (NEW) |

### Étape 4: Démarrer l'Application

#### Option A: Via Maven

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### Option B: Via JAR

```bash
# Générez le JAR
mvn clean package -DskipTests

# Lancez-le
java -jar target/gestion-stock-pme-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=dev
```

#### Option C: Avec variables d'environnement

```bash
set SPRING_PROFILES_ACTIVE=dev
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/gestion_stock_pme
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=

java -jar target/gestion-stock-pme-1.0.0-SNAPSHOT.jar
```

### Étape 5: Vérifier le Démarrage

1. **Attendez ce message dans les logs**:
   ```
   [INFO] Tomcat started on port(s): 8080 (http) with context path ''
   [INFO] Started GestionStockPmeApplication in 12.345 seconds
   ```

2. **Testez l'application**:
   ```bash
   curl http://localhost:8080/actuator/health
   # Réponse: {"status":"UP"}
   ```

3. **Accédez à Swagger UI**:
   - Ouvrez: http://localhost:8080/swagger-ui.html

## 🔍 Vérifications Post-Installation

### Base de Données

```bash
# Vérifiez les tables créées
mysql gestion_stock_pme -u root -e "SHOW TABLES;"
```

Expected output:
```
categorie
clients          ← NEW
commandes_clients     ← NEW
lignes_commandes_clients ← NEW
mouvements_stock
produits
refresh_tokens
roles
utilisateurs
utilisateurs_roles
```

### Données de Test

```bash
# Vérifiez les rôles
mysql gestion_stock_pme -u root -e "SELECT * FROM roles;"

# Vérifiez les utilisateurs de test
mysql gestion_stock_pme -u root -e "SELECT email FROM utilisateurs LIMIT 5;"

# Vérifiez les clients de test
mysql gestion_stock_pme -u root -e "SELECT * FROM clients LIMIT 3;"
```

### Comptabilité JWT

```bash
# Vérifiez la clé JWT en configuration
grep "app.jwt.secret" src/main/resources/application.yml
```

## 🛠️ Configuration Personnalisée

### Pour Changer le Port

*Fichier: `application.yml`*
```yaml
server:
  port: 9090  # Au lieu de 8080
```

### Pour Changer la Base Données

*Fichier: `application-dev.yml`*
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/autre_db
    username: utilisateur
    password: motdepasse
```

### Pour Activer le Logging SQL

*Fichier: `application-dev.yml`*
```yaml
spring:
  jpa:
    show-sql: true                    # Affiche les requêtes SQL
  logging:
    level:
      org.hibernate.SQL: DEBUG        # Logs Hibernate en DEBUG
```

## 🐳 Déploiement avec Docker (Optionnel)

### Prerequis
- Docker installé
- docker-compose installé

### Commandes

```bash
# Démarrer les services (MySQL + App)
docker-compose up -d

# Vérifier les logs
docker-compose logs -f app

# Arrêter les services
docker-compose down

# Supprimer tous les conteneurs et volumes
docker-compose down -v
```

## 📋 Checklist d'Installation

- [ ] Java 21 installé (`java -version`)
- [ ] Maven installé (`mvn -version`)
- [ ] MySQL en cours d'exécution et accessible
- [ ] Base de données `gestion_stock_pme` créée
- [ ] Variable JAVA_HOME configurée
- [ ] Protection firewall désactivée pour localhost:8080 (si nécessaire)
- [ ] Compilation sans erreurs (`mvn clean compile -DskipTests`)
- [ ] Application démarre sans erreurs
- [ ] Swagger UI accessible à http://localhost:8080/swagger-ui.html
- [ ] Health check passe: `/actuator/health`

## 🔐 Se Connecter (Premières Étapes)

### Utilisateurs de Test (from V2__data_initial.sql)

```
Email: admin@example.com
Mot de passe: admin123

Email: gestionnaire@example.com
Mot de passe: gestionnaire123

Email: employe@example.com
Mot de passe: employe123
```

### Test de Connexion

```bash
curl -X POST http://localhost:8080/api/v1/auth/connexion \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "motDePasse": "admin123"
  }'
```

## 🆘 En Cas de Problème

1. **Consultez TROUBLESHOOTING.md**
2. **Activez le DEBUG logging** dans `application-dev.yml`
3. **Vérifiez la connectivité MySQL**:
   ```bash
   mysql -u root -h localhost gestion_stock_pme -e "SELECT 1;"
   ```
4. **Nettoyez et réinstallez**:
   ```bash
   mvn clean
   rm -rf target/
   mvn clean package -DskipTests
   ```
5. **Consultez les logs complets**:
   ```bash
   java -jar target/*.jar 2>&1 | tee app.log
   ```

## 📞 Support

Pour plus d'aide:
- Consultez la documentation Swagger UI
- Vérifiez les fichiers de log dans `/target/`
- Lisez TROUBLESHOOTING.md pour les erreurs courantes
- Consultez API_TESTING.md pour des exemples d'utilisation

