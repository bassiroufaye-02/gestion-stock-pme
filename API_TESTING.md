# Documentation - Nouvelles APIs Commande Client & Client

## 🚀 Démarrage de l'Application

### Prérequis
- Java 21+
- Maven 3.8+
- MySQL 8.0+ (WAMP/XAMPP)
- Base de données: `gestion_stock_pme`

### Étapes

1. **Démarrer WAMP/XAMPP** et assurez-vous que MySQL écoute sur `localhost:3306`

2. **Compiler et exécuter**:
   ```bash
   cd gestion-stock-pme
   mvn clean package -DskipTests
   java -jar target/gestion-stock-pme-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
   ```

3. **Vérifier le démarrage**:
   ```bash
   curl http://localhost:8080/actuator/health
   # Réponse attendue: {"status":"UP"}
   ```

## 🔐 Authentification

### 1. Inscription (Créer un compte)

```bash
curl -X POST http://localhost:8080/api/v1/auth/inscription \
  -H "Content-Type: application/json" \
  -d '{p
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com",
    "motDePasse": "SecurePass123",
    "roles": ["ROLE_GESTIONNAIRE"]
  }'
```

**Réponse (201 Created)**:
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "f47ac10b-...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "email": "jean.dupont@example.com",
  "nomComplet": "Jean Dupont",
  "roles": ["ROLE_GESTIONNAIRE"]
}
```

### 2. Connexion

```bash
curl -X POST http://localhost:8080/api/v1/auth/connexion \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "motDePasse": "admin123"
  }'
```

Utilisez l'`accessToken` dans toutes les requêtes suivantes:
```bash
-H "Authorization: Bearer <accessToken>"
```

## 👥 API Clients

### Créer un client (ADMIN, GESTIONNAIRE)

```bash
curl -X POST http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CLI-004",
    "raisonSociale": "Entreprise Nova SARL",
    "email": "contact@nova.sn",
    "telephone": "778888888",
    "adresse": "123 Avenue Lamine Guèye",
    "ville": "Dakar"
  }'
```

**Réponse (201 Created)**:
```json
{
  "id": 4,
  "code": "CLI-004",
  "raisonSociale": "Entreprise Nova SARL",
  "email": "contact@nova.sn",
  "telephone": "778888888",
  "adresse": "123 Avenue Lamine Guèye",
  "ville": "Dakar",
  "actif": true,
  "createdAt": "2026-06-06T11:15:00Z"
}
```

### Récupérer un client

```bash
curl -X GET http://localhost:8080/api/v1/clients/4 \
  -H "Authorization: Bearer <accessToken>"
```

### Lister les clients actifs (avec pagination)

```bash
curl -X GET "http://localhost:8080/api/v1/clients?page=0&size=20" \
  -H "Authorization: Bearer <accessToken>"
```

### Rechercher des clients

```bash
curl -X GET "http://localhost:8080/api/v1/clients/recherche?search=Alpha&page=0&size=20" \
  -H "Authorization: Bearer <accessToken>"
```

### Modifier un client

```bash
curl -X PUT http://localhost:8080/api/v1/clients/4 \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CLI-004",
    "raisonSociale": "Entreprise Nova SARL Modifiée",
    "email": "newemail@nova.sn",
    "telephone": "778888889"
  }'
```

### Désactiver un client (ADMIN)

```bash
curl -X DELETE http://localhost:8080/api/v1/clients/4 \
  -H "Authorization: Bearer <accessToken>"
```

## 📋 API Commandes Client

### Créer une commande (ADMIN, GESTIONNAIRE)

```bash
curl -X POST http://localhost:8080/api/v1/commandes \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "dateLivraisonPrevue": "2026-06-20",
    "tauxTVA": 18.00,
    "notes": "Livraison urgent",
    "lignes": [
      {
        "produitId": 1,
        "quantite": 5,
        "prixUnitaireHT": 10000
      },
      {
        "produitId": 2,
        "quantite": 3,
        "prixUnitaireHT": 15000
      }
    ]
  }'
```

**Réponse (201 Created)**:
```json
{
  "id": 1,
  "numeroCommande": "CMD-202606-0001",
  "dateCommande": "2026-06-06",
  "dateLivraisonPrevue": "2026-06-20",
  "statut": "BROUILLON",
  "montantHT": 95000,
  "montantTVA": 17100,
  "montantTTC": 112100,
  "tauxTVA": 18.00,
  "notes": "Livraison urgent",
  "clientId": 1,
  "clientRaisonSociale": "Entreprise Alpha SARL",
  "traitePar": null,
  "lignes": [
    {
      "id": 1,
      "quantite": 5,
      "prixUnitaireHT": 10000,
      "montantLigneHT": 50000,
      "produitId": 1,
      "produitReference": "REF-001",
      "produitDesignation": "Produit A"
    }
  ],
  "createdAt": "2026-06-06T11:15:00Z",
  "createdBy": "admin@example.com"
}
```

### Confirmer une commande (ADMIN, GESTIONNAIRE)

Confirmer diminue le stock des produits.

```bash
curl -X POST http://localhost:8080/api/v1/commandes/1/confirmer \
  -H "Authorization: Bearer <accessToken>"
```

**Changements**:
- Statut: `BROUILLON` → `CONFIRMEE`
- Stock des produits: décrémenté
- `traitePar`: Utilisateur qui confirme

### Ajouter une ligne à la commande (statut BROUILLON uniquement)

```bash
curl -X POST http://localhost:8080/api/v1/commandes/1/lignes \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "produitId": 3,
    "quantite": 2,
    "prixUnitaireHT": 20000
  }'
```

### Supprimer une ligne

```bash
curl -X DELETE http://localhost:8080/api/v1/commandes/1/lignes/1 \
  -H "Authorization: Bearer <accessToken>"
```

### Changer le statut d'une commande

Transitions possibles:
- `BROUILLON` → `CONFIRMEE` (via /confirmer)
- `CONFIRMEE` → `EN_PREPARATION`
- `EN_PREPARATION` → `EXPEDIEE`
- `EXPEDIEE` → `LIVREE`
- `CONFIRMEE` ou `BROUILLON` → `ANNULEE`

```bash
curl -X PUT "http://localhost:8080/api/v1/commandes/1/statut?nouveauStatut=EN_PREPARATION" \
  -H "Authorization: Bearer <accessToken>"
```

### Récupérer une commande

```bash
curl -X GET http://localhost:8080/api/v1/commandes/1 \
  -H "Authorization: Bearer <accessToken>"
```

### Lister toutes les commandes

```bash
curl -X GET "http://localhost:8080/api/v1/commandes?page=0&size=20" \
  -H "Authorization: Bearer <accessToken>"
```

### Lister les commandes d'un client

```bash
curl -X GET "http://localhost:8080/api/v1/commandes/client/1?page=0&size=20" \
  -H "Authorization: Bearer <accessToken>"
```

### Lister par statut

```bash
curl -X GET "http://localhost:8080/api/v1/commandes/statut/CONFIRMEE?page=0&size=20" \
  -H "Authorization: Bearer <accessToken>"
```

### Rechercher par numéro

```bash
curl -X GET http://localhost:8080/api/v1/commandes/numero/CMD-202606-0001 \
  -H "Authorization: Bearer <accessToken>"
```

## 🔀 Transitions de Statut

### État initial: BROUILLON
Édition libre de la commande:
- ✅ Ajouter/supprimer des lignes
- ✅ Modifier les montants automatiques
- ✅ Passer à CONFIRMEE ou ANNULEA

### CONFIRMEE
Stock décrémenté:
- ✅ Passer à EN_PREPARATION
- ✅ Retourner à ANNULEA (stock remis)
- ❌ Modification des lignes

### EN_PREPARATION
La commande est en préparation:
- ✅ Passer à EXPEDIEE
- ❌ Autres transitions

### EXPEDIEE
La commande est expédiée:
- ✅ Passer à LIVREE (date livraison réelle = maintenant)

### LIVREE / ANNULEE
États finaux:
- ❌ Aucune transition possible

## 🛠 Outils utiles

### Postman Collection
Importez dans Postman pour tester facilement:
```json
{ "info": { "name": "Gestion Stock PME", "version": "1.0" } }
```

### Documentation Swagger
Accédez à: http://localhost:8080/swagger-ui.html

### Logs en Temps Réel
```bash
tail -f nohup.out
```

## ✅ Checklist de Test

- [ ] Créer un client
- [ ] Lister les clients
- [ ] Créer une commande BROUILLON
- [ ] Ajouter des lignes à la commande
- [ ] Confirmer la commande (stock décrémenté)
- [ ] Changer le statut à EN_PREPARATION
- [ ] Changer à EXPEDIEE puis LIVREE
- [ ] Créer une autre commande et l'annuler
- [ ] Vérifier que le stock remonte après annulation

