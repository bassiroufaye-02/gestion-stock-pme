# 📊 Gestion Stock PME - Documentation Complète

## 🎯 Vue d'ensemble du Projet

**Gestion Stock PME** est une plateforme complète de gestion de stock, de commandes clients et d'authentification pour petites et moyennes entreprises.

### Fonctionnalités Principales

| Module | Statut | Description |
|--------|--------|-------------|
| **Authentification & Sécurité** | ✅ | JWT, RBAC, Refresh tokens |
| **Gestion Produits** | ✅ | CRUD, catégories, alertes stock |
| **Gestion Clients** | ✅ NEW | Création, modification, liste |
| **Commandes Clients** | ✅ NEW | BROUILLON → LIVREE, gestion stock |
| **Mouvements Stock** | ✅ | Entrée/Sortie/Ajustement |
| **Rapports** | 🔜 | À venir |

## 🏗️ Architecture

```
Frontend (React/Vue)
        ↓
   API REST (Spring Boot 3.3)
        ↓
   ┌─────────────────────────────┐
   │ Controllers                 │
   │ - AuthController            │
   │ - ProduitController         │
   │ - ClientController (NEW)    │
   │ - CommandeClientController  │
   │ - CategorieController       │
   │ - StockController           │
   └─────────────────────────────┘
        ↓
   ┌─────────────────────────────┐
   │ Services (Métier)           │
   │ - AuthService               │
   │ - ProduitServiceImpl         │
   │ - ClientServiceImpl (NEW)    │
   │ - CommandeClientServiceImpl  │
   │ - CategorieService          │
   │ - StockService              │
   └─────────────────────────────┘
        ↓
   ┌─────────────────────────────┐
   │ Repositories (Data)         │
   │ - UtilisateurRepository     │
   │ - ProduitRepository         │
   │ - ClientRepository (NEW)    │
   │ - CommandeClientRepository  │
   │ - CategorieRepository       │
   │ - MouvementStockRepository  │
   │ - RefreshTokenRepository    │
   └─────────────────────────────┘
        ↓
   MySQL Database (WAMP)
```

## 🔐 Sécurité

### Systèmes d'Authentification
- **JWT Bearer Token**: Pour l'authentification sans état
- **Refresh Token**: Pour renouveler l'accès sans se reconnecter
- **Role-Based Access Control (RBAC)**: 3 rôles - ADMIN, GESTIONNAIRE, EMPLOYE
- **@PreAuthorize**: Annotations pour contrôle d'accès granulaire

### Endpoints Protégés
```
ADMIN uniquement:
  - DELETE /api/v1/clients/{id}        (Désactiver client)

ADMIN, GESTIONNAIRE:
  - POST /api/v1/clients                (Créer client)
  - PUT /api/v1/clients/{id}            (Modifier client)
  - POST /api/v1/commandes              (Créer commande)
  - POST /api/v1/commandes/{id}/confirmer

Authentifiés (tout rôle):
  - GET /api/v1/clients/{id}
  - GET /api/v1/clients
  - GET /api/v1/commandes
```

## 📊 Schéma Base de Données

### Tables Principales

**Utilisateurs & Authentification**
```
utilisateurs
├── id (PK)
├── email (UNIQUE)
├── mot_de_passe (bcrypt)
├── nom, prenom
├── actif (Boolean)
└── Timestamps

utilisateurs_roles (Many-to-Many)
├── utilisateur_id (FK)
└── role_id (FK)

roles
├── id (PK)
├── nom (ROLE_ADMIN, ROLE_GESTIONNAIRE, ROLE_EMPLOYE)
```

**Produits & Stock**
```
categorie
├── id (PK)
├── code (UNIQUE)
├── libelle, description
├── actif

produits
├── id (PK)
├── reference (UNIQUE)
├── designation
├── quantite_stock, seuil_alerte
├── prix_achat, prix_vente
├── categorie_id (FK)

mouvements_stock
├── id (PK)
├── type_mouvement (ENTREE/SORTIE/AJUSTEMENT)
├── quantite
├── produit_id (FK)
├── utilisateur_id (FK)
└── Timestamps
```

**Clients & Commandes (NEW)**
```
clients
├── id (PK)
├── code (UNIQUE)
├── raison_sociale
├── email, telephone
├── adresse, ville
├── actif

commandes_clients
├── id (PK)
├── numero_commande (UNIQUE, auto: CMD-202606-0001)
├── date_commande, date_livraison_prevue
├── statut (BROUILLON/CONFIRMEE/EN_PREPARATION/EXPEDIEE/LIVREE/ANNULEA)
├── montant_ht, montant_tva, montant_ttc
├── client_id (FK)
├── traite_par_id (FK → utilisateurs)

lignes_commandes_clients
├── id (PK)
├── quantite, prix_unitaire_ht
├── montant_ligne_ht (calculé)
├── commande_id (FK)
├── produit_id (FK)
```

## 🚀 Démarrage Rapide

### Prérequis
```
Java 21, Maven 3.8+, MySQL 8.0+, WAMP/XAMPP
```

### Installation (3 étapes)

1. **Préparez la base de données**:
   ```bash
   mysql -u root -e "CREATE DATABASE gestion_stock_pme;"
   ```

2. **Compilez l'application**:
   ```bash
   mvn clean package -DskipTests
   ```

3. **Démarrez le serveur**:
   ```bash
   java -jar target/gestion-stock-pme-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
   ```

Accédez à: **http://localhost:8080/swagger-ui.html**

## 📚 Documentation

| Document | Contenu |
|----------|---------|
| **[INSTALLATION.md](./INSTALLATION.md)** | Guide d'installation complète |
| **[API_TESTING.md](./API_TESTING.md)** | Exemples d'utilisation des APIs |
| **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** | Solution aux erreurs courantes |

## 🧪 APIs Principales

### Authentification
```bash
POST   /api/v1/auth/inscription
POST   /api/v1/auth/connexion
POST   /api/v1/auth/refresh
POST   /api/v1/auth/deconnexion
```

### Clients (NEW)
```bash
POST   /api/v1/clients                    (Créer)
GET    /api/v1/clients/{id}               (Récupérer)
GET    /api/v1/clients                    (Lister)
GET    /api/v1/clients/recherche          (Rechercher)
PUT    /api/v1/clients/{id}               (Modifier)
DELETE /api/v1/clients/{id}               (Désactiver)
```

### Commandes (NEW)
```bash
POST   /api/v1/commandes                  (Créer)
GET    /api/v1/commandes/{id}             (Récupérer)
GET    /api/v1/commandes                  (Lister toutes)
GET    /api/v1/commandes/client/{id}      (Par client)
GET    /api/v1/commandes/statut/{statut}  (Par statut)
GET    /api/v1/commandes/numero/{num}     (Par numéro)
POST   /api/v1/commandes/{id}/confirmer   (Confirmer)
PUT    /api/v1/commandes/{id}/statut      (Changer statut)
POST   /api/v1/commandes/{id}/lignes      (Ajouter ligne)
DELETE /api/v1/commandes/{id}/lignes/{id} (Supprimer ligne)
```

### Produits
```bash
POST   /api/v1/produits
GET    /api/v1/produits/{id}
GET    /api/v1/produits
GET    /api/v1/produits/recherche
GET    /api/v1/produits/alertes
DELETE /api/v1/produits/{id}
```

## 📋 État des Migrations

| Version | Fichier | Tables | État |
|---------|---------|--------|------|
| V1 | `V1__init_schema.sql` | 8 tables de base | ✅ Appliquée |
| V2 | `V2__data_initial.sql` | Données initiales | ✅ Appliquée |
| V3 | `V3__clients_et_commandes.sql` | clients, commandes_clients, lignes | ✅ NEW |
| V4 | `V4__data_clients_demo.sql` | 3 clients de test | ✅ NEW |

## 🔄 Workflow Commande Typique

```
1. CLIENT CRÉE COMMANDE (Statut: BROUILLON)
   ├── Ajoute des lignes de produits
   ├── Modifie les prix si besoin
   └── Montants calculés automatiquement

2. CLIENT CONFIRME (Statut: CONFIRMEE)
   ├── Stock saisi et décrémenté
   ├── Validation du stock disponible
   └── Montants finalisés

3. PRÉPARATION (Statut: EN_PREPARATION)
   └── L'équipe prépare la commande

4. EXPÉDITION (Statut: EXPEDIEE)
   └── Commande envoyée

5. LIVRAISON (Statut: LIVREE)
   └── Date livraison réelle enregistrée

OU: ANNULATION (Statut: ANNULEA)
   ├── Si BROUILLON: Aucun impact
   └── Si CONFIRMEE: Stock remis
```

## 📊 Stack Technique

| Composant | Version | Description |
|-----------|---------|-------------|
| **Java** | 21 | Langage principal |
| **Spring Boot** | 3.3.0 | Framework web |
| **Spring Security** | 6.3.0 | Authentification/Autorisation |
| **Spring Data JPA** | 3.3.0 | Accès données |
| **MySQL / MariaDB** | 8.0+ | Base de données |
| **Flyway** | 9.22.3 | Migrations DB |
| **Lombok** | 1.18.30 | Réduction boilerplate |
| **JWT (jjwt)** | 0.13.0 | Tokens JWT |
| **Springdoc** | 2.0.4 | Swagger/OpenAPI |
| **JUnit 5** | 5.10.0 | Tests unitaires |
| **Mockito** | 5.3.1 | Mocking |

## 🎓 Concepts Clés

### 1. **Enumerations**
```java
StatutCommande: BROUILLON, CONFIRMEE, EN_PREPARATION, EXPEDIEE, LIVREE, ANNULEA
TypeMouvement: ENTREE, SORTIE, AJUSTEMENT
```

### 2. **Calculs Automatiques**
```
montant_ligne_ht = quantite × prix_unitaire_ht
montant_ht = SUM(montants des lignes)
montant_tva = montant_ht × (taux_tva / 100)
montant_ttc = montant_ht + montant_tva
```

### 3. **Auditing (Spring Data)**
Chaque entité hérite de BaseEntity:
```
created_at    (Timestamp)
updated_at    (Timestamp)
created_by    (Email utilisateur)
updated_by    (Email utilisateur)
```

### 4. **Validations**
- `@NotNull`, `@NotBlank`, `@Email`
- `@Min(1)`, `@Max()`
- `@DecimalMin("0.01")`
- Custom validators pour métier

## 🚨 Gestion d'Erreurs

Tous les endpoints retournent une `ProblemDetail`:

```json
{
  "type": "https://pme.com/errors/business",
  "title": "Erreur métier",
  "status": 409,
  "detail": "Un client avec le code 'CLI-001' existe déjà",
  "instance": "/api/v1/clients",
  "timestamp": "2026-06-06T11:15:00Z",
  "exceptionType": "BusinessException",
  "message": "Un client avec le code 'CLI-001' existe déjà"
}
```

## 💡 Bonnes Pratiques Appliquées

✅ **Clean Code**
- Noms explicites
- Méthodes courtes
- DRY (Don't Repeat Yourself)

✅ **Architecture en couches**
- Controller → Service → Repository
- Séparation des responsabilités

✅ **Transactions**
- `@Transactional` sur services
- `@Transactional(readOnly=true)` pour lectures

✅ **Logging**
- `@Slf4j` (SLF4J via Lombok)
- Logs D DEBUG, INFO, WARN, ERROR

✅ **Validation**
- Validation côté serveur
- `@Valid` sur DTOs

✅ **Documentation**
- Javadoc sur méthodes
- Swagger/OpenAPI annotations

## 🔧 Maintenance

### Logs
```bash
# Afficher les logs en temps réel
tail -f nohup.out

# Chercher les erreurs
grep ERROR nohup.out

# Par niveau
grep "WARN\|ERROR" nohup.out
```

### Santé de l'Application
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
curl http://localhost:8080/actuator/metrics
```

### Redémarrage
```bash
# Arrêter l'application
pkill -f "gestion-stock-pme"

# Redémarrer
java -jar target/gestion-stock-pme-1.0.0-SNAPSHOT.jar &
```

## 📞 Support & Contribution

### Signaler un Bug
1. Vérifiez [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
2. Activez le DEBUG logging
3. Collectez les logs complets
4. Décrivez les pas pour reproduire

### Contribuer
1. Créez une branche feature
2. Écrivez des tests
3. Commitez avec messages clairs
4. Créez une Pull Request

## 📄 Licence

Ce projet est fourni à titre d'exemple éducatif.

---

**Dernière mise à jour**: 2026-06-06  
**Version**: 1.0.0-SNAPSHOT  
**Statut**: ✅ En production

