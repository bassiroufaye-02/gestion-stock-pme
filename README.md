# 🏭 Gestion Stock PME — Plateforme Spring Boot

> Projet Master Génie Logiciel — Plateforme de gestion de stock et commandes pour PME

---

## 🚀 Démarrage rapide

### Prérequis
- Java 21 (JDK)
- Maven 3.9+
- WAMP Server (MySQL sur port 3306)
- IntelliJ IDEA Ultimate

### 1. Configurer la base de données WAMP

Ouvrez phpMyAdmin ou MySQL Workbench et créez la base :
```sql
CREATE DATABASE IF NOT EXISTS gestion_stock_pme
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

> Le schéma sera automatiquement créé par **Flyway** au démarrage.

### 2. Vérifier la configuration

Dans `src/main/resources/application-dev.yml`, ajustez si nécessaire :
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gestion_stock_pme
    username: root
    password:        # Laissez vide si pas de mot de passe WAMP
```

### 3. Lancer l'application

```bash
mvn spring-boot:run
```

Ou directement depuis IntelliJ : clic droit sur `GestionStockPmeApplication` → **Run**

---

## 📋 URLs importantes

| Service | UR<br/>L |
|---------|-----|
| API Base | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Actuator Health | `http://localhost:8080/actuator/health` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

---

## 🔐 Authentification

### Connexion avec le compte admin de démo

```http
POST /api/v1/auth/connexion
Content-Type: application/json

{
  "email": "admin@pme.com",
  "motDePasse": "Admin@123"
}
```

**Réponse :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "uuid-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "email": "admin@pme.com",
  "nomComplet": "Système Admin",
  "roles": ["ROLE_ADMIN"]
}
```

### Utiliser le token dans Swagger
1. Ouvrir `http://localhost:8080/swagger-ui.html`
2. Cliquer **Authorize** (cadenas en haut)
3. Entrer : `Bearer eyJhbGciO...`

---

## 👥 Rôles et permissions

| Rôle | Lecture Produits | Écriture Produits | Mouvements Stock | Gestion Utilisateurs |
|------|:---:|:---:|:---:|:---:|
| `ROLE_ADMIN` | ✅ | ✅ | ✅ | ✅ |
| `ROLE_GESTIONNAIRE` | ✅ | ✅ | ✅ | ❌ |
| `ROLE_EMPLOYE` | ✅ | ❌ | ❌ | ❌ |

---

## 📦 Endpoints API

### Authentification (`/api/v1/auth`)
```
POST   /inscription    - Créer un compte
POST   /connexion      - Se connecter (obtenir JWT)
POST   /refresh        - Rafraîchir le token
POST   /deconnexion    - Se déconnecter
```

### Produits (`/api/v1/produits`)
```
GET    /               - Lister (paginé)
GET    /{id}           - Détails d'un produit
GET    /reference/{ref}- Chercher par référence
GET    /recherche?q=   - Recherche fulltext
GET    /categorie/{id} - Par catégorie
GET    /alertes        - Produits en alerte de stock
GET    /alertes/count  - Nombre d'alertes
POST   /               - Créer (ADMIN/GESTIONNAIRE)
PUT    /{id}           - Modifier (ADMIN/GESTIONNAIRE)
DELETE /{id}           - Désactiver (ADMIN)
```

### Catégories (`/api/v1/categories`)
```
GET    /               - Catégories actives
GET    /toutes         - Toutes les catégories (ADMIN)
GET    /{id}           - Détails
POST   /               - Créer (ADMIN/GESTIONNAIRE)
PUT    /{id}           - Modifier (ADMIN/GESTIONNAIRE)
DELETE /{id}           - Désactiver (ADMIN)
```

### Mouvements de Stock (`/api/v1/stock`)
```
POST   /mouvements                    - Enregistrer un mouvement
GET    /mouvements/produit/{produitId} - Historique
```

**Types de mouvements :**
- `ENTREE` : réapprovisionnement (augmente le stock)
- `SORTIE` : vente ou consommation (réduit le stock)
- `AJUSTEMENT` : correction manuelle du stock

---

## 🧪 Lancer les tests

```bash
# Tous les tests
mvn test

# Tests + rapport JaCoCo (couverture de code)
mvn verify

# Voir le rapport de couverture
open target/site/jacoco/index.html
```

---

## 🏗️ Architecture du projet

```
src/main/java/com/pme/stock/
├── config/             # SecurityConfig, OpenApiConfig, AuditorAware
├── controller/         # REST Controllers (Auth, Produit, Stock, Categorie)
├── dto/
│   ├── request/        # Objets reçus par l'API (validation @Valid)
│   └── response/       # Objets retournés par l'API
├── entity/             # Entités JPA (Produit, Categorie, Utilisateur, Role...)
├── exception/          # Exceptions métier + GlobalExceptionHandler
├── repository/         # Interfaces JpaRepository avec requêtes JPQL
├── security/
│   ├── filter/         # JwtAuthenticationFilter
│   └── service/        # JwtService, UserDetailsServiceImpl
└── service/
    └── impl/           # Logique métier (ProduitServiceImpl, StockService...)

src/main/resources/
├── db/migration/       # Scripts Flyway (V1__init_schema.sql, V2__data_initial.sql)
├── application.yml     # Config commune
├── application-dev.yml # Config développement (WAMP MySQL)
└── application-prod.yml# Config production (variables d'environnement)

src/test/java/com/pme/stock/
├── controller/         # Tests MockMvc (@WebMvcTest)
├── repository/         # Tests JPA (@DataJpaTest)
├── security/           # Tests JWT
└── service/            # Tests unitaires (JUnit5 + Mockito)
```

---

## 💡 Décisions architecturales

| Décision | Choix | Pourquoi |
|----------|-------|---------|
| Migrations DB | **Flyway** | Historique versionné et reproductible |
| Sécurité | **JWT stateless** | API REST sans session côté serveur |
| Tokens | **Access (15min) + Refresh (7j)** | Sécurité + confort utilisateur |
| Erreurs | **ProblemDetail (RFC 7807)** | Standard moderne pour les erreurs API |
| Audit | **Spring Data Auditing** | Traçabilité complète (créé par / modifié par) |
| Soft delete | **actif=false** | Conserver l'historique des données |
| Validation | **@Valid + DTO** | Jamais exposer les entités JPA directement |
| Tests | **JUnit5 + Mockito + MockMvc** | Couverture > 80% (JaCoCo) |

---

## ⚙️ Variables d'environnement (production)

```bash
DB_URL=jdbc:mysql://prod-server:3306/gestion_stock_pme
DB_USERNAME=app_user
DB_PASSWORD=mot_de_passe_securise
```
