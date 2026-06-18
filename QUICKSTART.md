# ⚡ QuickStart - Démarrage en 5 Minutes

## 🎯 Objectif

Vous avez l'application compilée et want to test it immediately sans lire tous les docs?  
Voici le chemin le plus rapide!

---

## ✅ Étape 1: Préparez la Base de Données (2 minutes)

### Option A: Interface graphique (WAMP)

1. **Lancez WAMP** (cliquez sur wampmanager.exe)
2. **Attendez que l'icône devienne verte** dans la barre d'état
3. **Clic droit** → "phpMyAdmin"
4. Dans phpMyAdmin → Onglet "SQL"
5. Collez ceci:
   ```sql
   CREATE DATABASE gestion_stock_pme CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
6. Cliquez sur "Exécuter" ✅

### Option B: En ligne de commande

```bash
mysql -u root -h localhost
CREATE DATABASE gestion_stock_pme CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

---

## ✅ Étape 2: Démarrez l'Application (1 minute)

### Ouvrez un terminal dans le dossier du projet

```bash
cd C:\chemin\vers\gestion-stock-pme
```

### Lancez l'application

```bash
java -jar target/gestion-stock-pme-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### Attendez ce message:

```
[INFO] Tomcat started on port(s): 8080 (http) with context path ''
[INFO] Started GestionStockPmeApplication in 12.345 seconds
```

✅ **L'application est prête!**

---

## ✅ Étape 3: Accédez à Swagger UI (30 secondes)

Ouvrez dans votre navigateur:

```
http://localhost:8080/swagger-ui.html
```

Vous devriez voir la **documentation interactive de toutes les APIs**.

---

## ✅ Étape 4: Testez une Connexion (1 minute)

### Utilisateurs de test disponibles:

```
Email: admin@example.com
Mot de passe: admin123

Email: gestionnaire@example.com
Mot de passe: gestionnaire123

Email: employe@example.com
Mot de passe: employe123
```

### Dans Swagger UI:

1. **Trouvez** le endpoint `/api/v1/auth/connexion`
2. **Cliquez** sur "Try it out"
3. **Collez** dans la boîte de texte:
   ```json
   {
     "email": "admin@example.com",
     "motDePasse": "admin123"
   }
   ```
4. **Cliquez** "Execute"
5. **Vous devez voir** une réponse 200 avec un `accessToken`

✅ **Authentification fonctionne!**

---

## ✅ Étape 5: Testez un Endpoint Client (NEW)

### Trouvez: `POST /api/v1/clients`

1. **Cliquez** sur "Try it out"
2. **Remplacez** l'`accessToken` dans le header (vu à l'étape 4)
   - Allez dans "Authorize" (en haut à droite)
   - Collez: `Bearer <votre_accessToken_from_step4>`
   - Cliquez "Authorize"
3. **Collez** dans Request body:
   ```json
   {
     "code": "CLI-TEST-001",
     "raisonSociale": "Ma Première Entreprise",
     "email": "contact@entreprise.com",
     "telephone": "778888888",
     "ville": "Dakar"
   }
   ```
4. **Cliquez** "Execute"
5. **Vous devez voir** une réponse 201 avec l'ID du client créé

✅ **Clients API fonctionne!**

---

## ✅ Étape 6: Testez une Commande (NEW)

### Créez une Commande: `POST /api/v1/commandes`

1. **Collez** dans Request body (utilisez le clientId reçu à l'étape 5):
   ```json
   {
     "clientId": 1,
     "dateLivraisonPrevue": "2026-06-20",
     "tauxTVA": 18.00,
     "notes": "Première commande de test",
     "lignes": [
       {
         "produitId": 1,
         "quantite": 5,
         "prixUnitaireHT": 10000
       }
     ]
   }
   ```
2. **Cliquez** "Execute"
3. **Vous devez voir** 201 avec `numeroCommande` format "CMD-202606-0001"

✅ **Commandes API fonctionne!**

---

## 🎉 Bravo! Vous avez Réussi! 🎉

Vous venez de:
- ✅ Configurer la base de données
- ✅ Démarrer l'application
- ✅ Vous connecter (JWT)
- ✅ Créer un client
- ✅ Créer une commande

---

## 🔄 Prochaines Actions

### Pour Approfondir

- **Lisez**: [API_TESTING.md](./API_TESTING.md) pour plus d'exemples
- **Installez**: Postman pour des tests plus avancés
- **Explorez**: Les autres endpoints dans Swagger

### En Cas d'Erreur 500

- **Consultez**: [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
- **Activez**: Debug logging dans `application-dev.yml`
- **Vérifiez**: Les logs dans le terminal

### Pour Comprendre l'Architecture

- **Lisez**: [README_COMPLETE.md](./README_COMPLETE.md)
- **Explorez**: La structure du code
- **Consultez**: [INSTALLATION.md](./INSTALLATION.md) pour les détails

---

## 📱 Commandes Utiles

### Arrêter l'application

```bash
# Dans le terminal où l'app démarre
CTRL + C
```

### Redémarrer après modification

```bash
# Recompiler
mvn clean package -DskipTests

# Relancer
java -jar target/gestion-stock-pme-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### Consulter les logs détaillés

Dans le terminal, cherchez les messages avec:
- `INFO` - Informations générales
- `DEBUG` - Détails techniques
- `WARN` - Avertissements
- `ERROR` - Erreurs (problèmes à résoudre)

---

## 🆘 Aide Rapide

| Problème | Solution |
|----------|----------|
| **Erreur de connexion MySQL** | Vérifiez que WAMP/XAMPP est lancé |
| **Port 8080 déjà utilisé** | Changez le port dans `application.yml` ou arrêtez l'autre application |
| **Erreur 401 Unauthorized** | Assurez-vous d'avoir l'`accessToken` dans le header |
| **Erreur 404 Not Found** | Vérifiez l'URL exacte de l'endpoint |
| **Erreur 500 Internal Server** | Lisez les logs et [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) |

---

## 🎓 Ressources Supplémentaires

- **Swagger UI**: http://localhost:8080/swagger-ui.html (Documentation interactive)
- **Postman Collection**: Importez depuis [API_TESTING.md](./API_TESTING.md)
- **GitHub Docs**: Lisez le README du projet
- **Spring Boot Docs**: https://spring.io/projects/spring-boot

---

## ✨ Prochaines Étapes Recommandées

1. **Créez 3-4 clients** avec des données réalistes
2. **Créez des commandes** avec différents produits
3. **Testez les transitions de statut** (BROUILLON → CONFIRMEE → etc.)
4. **Explorez les endpoints** de recherche et filtrage
5. **Modifiez et supprimez** des données

---

**Bon développement! 🚀**

---

*Besoin d'aide? Consultez les fichiers README dans le répertoire racine du projet.*

