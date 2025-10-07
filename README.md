# 🏦 Bank Management System - API REST

[![Java](https://img.shields.io/badge/Java-17-red.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Une API REST complète pour la gestion d'un système bancaire moderne avec authentification JWT, gestion des comptes et des transactions.

## 📋 Table des matières

- [Fonctionnalités](#-fonctionnalités)
- [Technologies utilisées](#️-technologies-utilisées)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#️-configuration)
- [Lancement de l'application](#-lancement-de-lapplication)
- [Documentation API](#-documentation-api)
- [Endpoints](#-endpoints)
- [Exemples d'utilisation](#-exemples-dutilisation)
- [Tests](#-tests)
- [Structure du projet](#-structure-du-projet)
- [Docker](#-docker)
- [CI/CD](#-cicd)
- [Contribuer](#-contribuer)
- [Licence](#-licence)

## ✨ Fonctionnalités

### 🔐 Authentification et Sécurité
- ✅ Enregistrement d'utilisateurs avec validation
- ✅ Connexion avec JWT (JSON Web Tokens)
- ✅ Rafraîchissement automatique des tokens
- ✅ Protection des routes avec Spring Security
- ✅ Gestion des rôles et permissions (USER, ADMIN, MANAGER)
- ✅ Hachage sécurisé des mots de passe avec BCrypt

### 💳 Gestion des Comptes Bancaires
- ✅ Création de comptes (Courant, Épargne, Professionnel, Étudiant)
- ✅ Consultation des détails d'un compte
- ✅ Liste de tous les comptes d'un utilisateur
- ✅ Calcul du solde total
- ✅ Activation/Désactivation/Suspension de comptes
- ✅ Génération automatique de numéros de compte uniques

### 💸 Transactions Bancaires
- ✅ **Dépôts** : Ajout d'argent sur un compte
- ✅ **Retraits** : Retrait d'argent avec vérification du solde
- ✅ **Virements** : Transfert entre comptes avec validation
- ✅ Historique complet des transactions
- ✅ Filtrage par date, type et statut
- ✅ Pagination et tri des résultats
- ✅ Numéros de référence uniques pour chaque transaction
- ✅ Statistiques détaillées des transactions

### 📊 Fonctionnalités Avancées
- ✅ Validation complète des données avec Bean Validation
- ✅ Gestion centralisée des exceptions
- ✅ Logs détaillés pour le debugging
- ✅ Documentation Swagger/OpenAPI interactive
- ✅ Support multi-devises
- ✅ Transactions ACID avec isolation
- ✅ Audit automatique (createdAt, updatedAt)

## 🛠️ Technologies utilisées

### Backend
- **Java 17** - Langage de programmation
- **Spring Boot 3.2.0** - Framework principal
- **Spring Security** - Authentification et autorisation
- **Spring Data JPA** - Persistance des données
- **Hibernate** - ORM
- **PostgreSQL 16** - Base de données relationnelle

### Sécurité
- **JWT (JSON Web Tokens)** - Authentification stateless
- **BCrypt** - Hachage des mots de passe
- **JJWT 0.12.3** - Gestion des tokens JWT

### Documentation
- **Springdoc OpenAPI 3** - Documentation API
- **Swagger UI** - Interface interactive de test

### Outils & DevOps
- **Maven** - Gestion des dépendances
- **Docker** - Conteneurisation
- **Docker Compose** - Orchestration des services
- **Lombok** - Réduction du code boilerplate
- **SLF4J & Logback** - Logging

## 📦 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Java JDK 17** ou supérieur
- **Maven 3.6+**
- **PostgreSQL 12+** (ou utilisez Docker)
- **Docker** et **Docker Compose** (optionnel mais recommandé)
- **Git**

### Vérifier les installations

```bash
java -version    # Devrait afficher Java 17+
mvn -version     # Devrait afficher Maven 3.6+
docker --version # Devrait afficher Docker 20+
psql --version   # Devrait afficher PostgreSQL 12+
```

## 🚀 Installation

### 1. Cloner le projet

```bash
git clone https://github.com/votre-username/bank-system.git
cd bank-system
```

### 2. Configurer la base de données PostgreSQL

#### Option A : Installation locale

```bash
# Créer la base de données
psql -U postgres

CREATE DATABASE bankdb;
CREATE USER bankuser WITH PASSWORD 'bankpass';
GRANT ALL PRIVILEGES ON DATABASE bankdb TO bankuser;
\q
```

#### Option B : Utiliser Docker (recommandé)

```bash
docker run --name bank-postgres \
  -e POSTGRES_DB=bankdb \
  -e POSTGRES_USER=bankuser \
  -e POSTGRES_PASSWORD=bankpass \
  -p 5432:5432 \
  -d postgres:16-alpine
```

### 3. Configurer les variables d'environnement

Créez un fichier `.env` à la racine du projet (optionnel) :

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bankdb
DB_USERNAME=bankuser
DB_PASSWORD=bankpass
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
SERVER_PORT=8080
```

### 4. Compiler le projet

```bash
mvn clean install
```

## ⚙️ Configuration

Les configurations se trouvent dans `src/main/resources/application.yml`.

### Configurations importantes

```yaml
# Base de données
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankdb
    username: bankuser
    password: bankpass

# JWT
jwt:
  secret: votre-secret-key-base64
  expiration: 86400000  # 24 heures
  refresh-expiration: 604800000  # 7 jours

# Serveur
server:
  port: 8080
```

## 🎯 Lancement de l'application

### Méthode 1 : Avec Maven

```bash
mvn spring-boot:run
```

### Méthode 2 : Avec le JAR compilé

```bash
mvn clean package -DskipTests
java -jar target/bank-system-1.0.0.jar
```

### Méthode 3 : Avec Docker Compose (recommandé)

```bash
# Lancer tous les services (PostgreSQL + Application)
docker-compose up -d

# Voir les logs
docker-compose logs -f bank-app

# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

### Méthode 4 : Avec pgAdmin (pour administration)

```bash
# Lancer avec pgAdmin
docker-compose --profile admin up -d

# Accéder à pgAdmin : http://localhost:5050
# Email: admin@banksystem.com
# Mot de passe: admin123
```

L'application sera accessible sur : **http://localhost:8080**

## 📚 Documentation API

Une fois l'application lancée, accédez à la documentation Swagger :

- **Swagger UI (Interface interactive)** : http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** : http://localhost:8080/api-docs
- **OpenAPI YAML** : http://localhost:8080/api-docs.yaml

### Utiliser Swagger UI

1. Ouvrez http://localhost:8080/swagger-ui.html
2. Créez un compte via `POST /api/auth/register`
3. Connectez-vous via `POST /api/auth/login`
4. Copiez le token JWT reçu
5. Cliquez sur le bouton **"Authorize"** en haut à droite
6. Entrez : `Bearer {votre-token}`
7. Testez tous les endpoints protégés !

## 🔌 Endpoints

### 🔐 Authentification (`/api/auth`)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/api/auth/register` | Créer un compte utilisateur | Non |
| POST | `/api/auth/login` | Se connecter | Non |
| POST | `/api/auth/refresh` | Rafraîchir le token | Non |
| POST | `/api/auth/logout` | Se déconnecter | Oui |
| GET | `/api/auth/me` | Obtenir l'utilisateur actuel | Oui |
| GET | `/api/auth/status` | Vérifier le statut d'authentification | Non |

### 💳 Comptes (`/api/accounts`)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/api/accounts` | Créer un nouveau compte | Oui |
| GET | `/api/accounts` | Lister tous ses comptes | Oui |
| GET | `/api/accounts/{id}` | Obtenir un compte par ID | Oui |
| GET | `/api/accounts/number/{accountNumber}` | Obtenir un compte par numéro | Oui |
| GET | `/api/accounts/active` | Lister les comptes actifs | Oui |
| GET | `/api/accounts/balance/total` | Obtenir le solde total | Oui |
| GET | `/api/accounts/statistics` | Obtenir les statistiques | Oui |
| PATCH | `/api/accounts/{id}/deactivate` | Désactiver un compte | Oui |
| PATCH | `/api/accounts/{id}/suspend` | Suspendre un compte | Oui |
| PATCH | `/api/accounts/{id}/reactivate` | Réactiver un compte | Oui |

### 💸 Transactions (`/api/transactions`)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/api/transactions/accounts/{id}/deposit` | Effectuer un dépôt | Oui |
| POST | `/api/transactions/accounts/{id}/withdraw` | Effectuer un retrait | Oui |
| POST | `/api/transactions/accounts/{id}/transfer` | Effectuer un virement | Oui |
| GET | `/api/transactions/history` | Historique de l'utilisateur | Oui |
| GET | `/api/transactions/history/paginated` | Historique paginé | Oui |
| GET | `/api/transactions/accounts/{id}/history` | Historique d'un compte | Oui |
| GET | `/api/transactions/accounts/{id}/history/paginated` | Historique paginé d'un compte | Oui |
| GET | `/api/transactions/accounts/{id}/history/period` | Transactions par période | Oui |
| GET | `/api/transactions/accounts/{id}/history/type/{type}` | Transactions par type | Oui |
| GET | `/api/transactions/accounts/{id}/statistics` | Statistiques des transactions | Oui |
| GET | `/api/transactions/reference/{referenceNumber}` | Obtenir une transaction par référence | Oui |

## 💡 Exemples d'utilisation

### 1. S'enregistrer

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+33612345678"
  }'
```

**Réponse :**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "userInfo": {
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_USER"]
  }
}
```

### 2. Se connecter

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john.doe",
    "password": "password123"
  }'
```

### 3. Créer un compte bancaire

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre-token}" \
  -d '{
    "accountType": "CHECKING",
    "initialBalance": 1000.00,
    "currency": "EUR"
  }'
```

**Réponse :**
```json
{
  "id": 1,
  "accountNumber": "FR7612345678901234567890",
  "accountType": "CHECKING",
  "balance": 1000.00,
  "currency": "EUR",
  "status": "ACTIVE",
  "owner": {
    "id": 1,
    "username": "john.doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com"
  },
  "createdAt": "2024-01-15T10:30:00"
}
```

### 4. Effectuer un dépôt

```bash
curl -X POST http://localhost:8080/api/transactions/accounts/1/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre-token}" \
  -d '{
    "amount": 500.00,
    "description": "Dépôt de salaire"
  }'
```

### 5. Effectuer un virement

```bash
curl -X POST http://localhost:8080/api/transactions/accounts/1/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre-token}" \
  -d '{
    "amount": 250.00,
    "destinationAccountNumber": "FR7698765432109876543210",
    "description": "Remboursement"
  }'
```

### 6. Consulter l'historique des transactions

```bash
curl -X GET http://localhost:8080/api/transactions/history/paginated?page=0&size=10 \
  -H "Authorization: Bearer {votre-token}"
```

## 🧪 Tests

### Exécuter tous les tests

```bash
mvn test
```

### Exécuter les tests avec couverture

```bash
mvn test jacoco:report
```

Le rapport de couverture sera généré dans `target/site/jacoco/index.html`.

### Tests d'intégration

```bash
mvn verify
```

## 📁 Structure du projet

```
bank-system/
├── src/
│   ├── main/
│   │   ├── java/com/bank/
│   │   │   ├── config/              # Configurations (Security, OpenAPI)
│   │   │   ├── controller/          # Contrôleurs REST
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── exception/           # Exceptions personnalisées
│   │   │   ├── model/               # Entités JPA
│   │   │   ├── repository/          # Repositories Spring Data
│   │   │   ├── security/            # JWT, UserDetails, Filters
│   │   │   ├── service/             # Logique métier
│   │   │   └── BankSystemApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Configuration principale
│   │       └── logback-spring.xml   # Configuration des logs
│   └── test/
│       └── java/com/bank/           # Tests unitaires et d'intégration
├── target/                          # Fichiers compilés
├── logs/                            # Logs de l'application
├── .dockerignore                    # Fichiers exclus de Docker
├── Dockerfile                       # Image Docker de l'app
├── docker-compose.yml               # Orchestration des services
├── pom.xml                          # Dépendances Maven
└── README.md                        # Documentation
```

## 🐳 Docker

### Construire l'image Docker

```bash
docker build -t bank-system:1.0.0 .
```

### Lancer l'application avec Docker

```bash
# Lancer uniquement la base de données
docker-compose up -d postgres

# Lancer l'application et la base de données
docker-compose up -d

# Voir les logs en temps réel
docker-compose logs -f

# Arrêter tous les services
docker-compose down

# Supprimer également les volumes (données)
docker-compose down -v
```

### Commandes Docker utiles

```bash
# Lister les conteneurs en cours
docker ps

# Accéder au shell de l'application
docker exec -it bank-app sh

# Accéder à PostgreSQL
docker exec -it bank-postgres psql -U bankuser -d bankdb

# Voir les logs d'un service
docker-compose logs -f bank-app

# Reconstruire l'image après modification
docker-compose up -d --build
```

---

## 🗄️ Accéder à la Base de Données PostgreSQL

### **Méthode 1 : Via pgAdmin (Interface graphique - RECOMMANDÉ)**

pgAdmin est une interface graphique qui facilite la gestion de PostgreSQL. Plutôt que de taper des commandes SQL, vous utilisez une interface visuelle avec des clics de souris.

#### **Étape 1 : Démarrer pgAdmin**

```bash
docker-compose --profile admin up -d
```

**Résultat attendu :**
```
✔ Container bank-pgadmin  Started
```

#### **Étape 2 : Accéder à pgAdmin**

1. **Ouvrez votre navigateur** : http://localhost:5050
2. **Connectez-vous avec** :
   - **Email** : `admin@banksystem.com`
   - **Mot de passe** : `admin123`

#### **Étape 3 : Ajouter le serveur PostgreSQL**

Une fois connecté, vous devez enregistrer votre serveur PostgreSQL :

1. **Clic droit** sur **"Servers"** dans le menu de gauche
2. Sélectionnez **"Register"** → **"Server..."**

**Configuration du serveur :**

**Onglet "General"** :
- **Name** : `Bank System DB` (vous pouvez choisir n'importe quel nom)

**Onglet "Connection"** :
| Champ | Valeur | Note |
|-------|--------|------|
| **Host name/address** | `postgres` | ⚠️ Important : utilisez `postgres` (nom du conteneur), pas `localhost` |
| **Port** | `5432` | Port par défaut PostgreSQL |
| **Maintenance database** | `bankdb` | Nom de votre base de données |
| **Username** | `bankuser` | Utilisateur de la base |
| **Password** | `bankpass` | Mot de passe |
| **Save password?** | ✅ Cochez | Pour ne pas retaper à chaque fois |

3. **Cliquez sur "Save"**

✅ Vous devriez maintenant voir votre serveur dans la liste !

#### **Étape 4 : Explorer vos données**

Dans le menu de gauche, dépliez l'arborescence (cliquez sur les flèches ▶) :

```
📁 Servers (1)
   └─ 📊 Bank System DB
       └─ 📁 Databases (1)
           └─ 📊 bankdb
               └─ 📁 Schemas (1)
                   └─ 📁 public
                       └─ 📁 Tables (5)
                           ├─ 📋 accounts        ← Comptes bancaires
                           ├─ 📋 transactions    ← Transactions
                           ├─ 📋 users           ← Utilisateurs
                           └─ 📋 user_roles      ← Rôles
```

#### **Étape 5 : Visualiser vos données**

Pour voir les données d'une table :

1. **Naviguez** vers : `Tables` dans l'arborescence
2. **Clic droit** sur une table (ex: `users`)
3. Choisissez **"View/Edit Data"** → **"All Rows"**

🎉 Vous voyez maintenant toutes vos données !

#### **Actions utiles dans pgAdmin**

**📊 Exécuter une requête SQL personnalisée :**
1. Clic droit sur **`bankdb`**
2. Choisissez **"Query Tool"**
3. Tapez votre requête SQL :

```sql
-- Voir tous vos comptes avec les soldes
SELECT 
    u.username,
    a.account_number,
    a.account_type,
    a.balance,
    a.currency,
    a.status
FROM users u
JOIN accounts a ON u.id = a.user_id
ORDER BY a.created_at DESC;
```

4. Cliquez sur ▶️ **Execute** (ou appuyez sur F5)

**✏️ Modifier une donnée :**
1. Ouvrez une table en mode "View/Edit Data"
2. Double-cliquez sur une cellule
3. Modifiez la valeur
4. Cliquez sur la **disquette** 💾 en haut pour sauvegarder

#### **Requêtes SQL utiles**

**Voir toutes les transactions :**
```sql
SELECT 
    t.id,
    t.transaction_type,
    t.amount,
    t.description,
    t.status,
    t.transaction_date,
    sa.account_number AS from_account,
    da.account_number AS to_account
FROM transactions t
LEFT JOIN accounts sa ON t.source_account_id = sa.id
LEFT JOIN accounts da ON t.destination_account_id = da.id
ORDER BY t.transaction_date DESC;
```

**Statistiques par type de transaction :**
```sql
SELECT 
    transaction_type,
    COUNT(*) as nombre,
    SUM(amount) as montant_total
FROM transactions
WHERE status = 'COMPLETED'
GROUP BY transaction_type;
```

**Voir les comptes avec leur utilisateur :**
```sql
SELECT 
    u.username,
    u.email,
    a.account_number,
    a.balance,
    a.status
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id;
```

#### **⚠️ Résolution des problèmes courants**

**❌ Erreur "Could not connect to server"**
- Vérifiez que PostgreSQL tourne : `docker ps | grep postgres`
- Vérifiez que vous avez bien utilisé `postgres` comme Host (pas `localhost`)
- Redémarrez pgAdmin : `docker-compose restart pgadmin`

**❌ Erreur "password authentication failed"**
- Vérifiez Username : `bankuser` (pas `postgres`)
- Vérifiez Password : `bankpass`

**❌ La page http://localhost:5050 ne charge pas**
```bash
# Vérifier que pgAdmin tourne
docker ps | grep pgadmin

# Voir les logs
docker logs bank-pgadmin

# Redémarrer
docker-compose --profile admin restart
```

---

### **Méthode 2 : Via ligne de commande (Terminal)**

Pour les utilisateurs avancés qui préfèrent le terminal :

#### **Accéder à PostgreSQL en ligne de commande**

```bash
docker exec -it bank-postgres psql -U bankuser -d bankdb
```

#### **Commandes SQL utiles dans psql**

```sql
-- Voir toutes les tables
\dt

-- Voir les utilisateurs
SELECT id, username, email, first_name, last_name, created_at FROM users;

-- Voir les comptes bancaires
SELECT id, account_number, account_type, balance, currency, status FROM accounts;

-- Voir les transactions
SELECT id, transaction_type, amount, description, status, transaction_date FROM transactions;

-- Voir les détails d'un utilisateur avec ses comptes
SELECT u.username, a.account_number, a.balance, a.currency 
FROM users u 
LEFT JOIN accounts a ON u.id = a.user_id 
WHERE u.username = 'votre_username';

-- Quitter psql
\q
```

#### **Commandes psql utiles**

| Commande | Description |
|----------|-------------|
| `\dt` | Lister toutes les tables |
| `\d nom_table` | Voir la structure d'une table |
| `\du` | Lister les utilisateurs PostgreSQL |
| `\l` | Lister toutes les bases de données |
| `\c nom_db` | Se connecter à une autre base |
| `\q` | Quitter psql |
| `\h` | Aide sur les commandes SQL |
| `\?` | Aide sur les commandes psql |

---

### **Méthode 3 : SSH Tunnel + pgAdmin LOCAL (Production - RECOMMANDÉ) 🌟**

⚠️ **Important** : En production, pgAdmin n'est **JAMAIS** déployé sur le serveur pour des raisons de sécurité. Vous utilisez **pgAdmin sur VOTRE machine** en créant un tunnel sécurisé vers le serveur !

#### **Comment ça marche ?**

```
┌─────────────┐                      ┌─────────────┐
│  Votre PC   │  Tunnel SSH chiffré  │  Serveur    │
│             │◄─────────────────────┤             │
│  pgAdmin    │                      │ PostgreSQL  │
│  (local)    │                      │  (privé)    │
└─────────────┘                      └─────────────┘
```

Le tunnel SSH crée une connexion sécurisée et chiffrée entre votre machine locale et le serveur de production, vous permettant d'accéder à PostgreSQL comme s'il était sur votre machine.

#### **Étapes pratiques :**

**1️⃣ Créer le tunnel SSH** (depuis votre PC)

```bash
# Syntaxe générale
ssh -L local_port:remote_host:remote_port user@server

# Exemple concret pour votre application
ssh -L 5433:localhost:5432 root@votre-serveur.com

# Ou si vous utilisez une clé SSH
ssh -i ~/.ssh/id_rsa -L 5433:localhost:5432 root@votre-serveur.com
```

**Explication des paramètres :**
- `5433` : Port LOCAL sur votre PC (on utilise 5433 pour éviter les conflits avec un PostgreSQL local éventuel)
- `localhost:5432` : PostgreSQL sur le serveur (localhost du POINT DE VUE du serveur)
- `root@votre-serveur.com` : Votre serveur de production

⚠️ **IMPORTANT** : Laissez ce terminal **OUVERT** - le tunnel reste actif tant que la connexion SSH est active. Si vous fermez le terminal, le tunnel se ferme.

---

**2️⃣ Ouvrir pgAdmin sur VOTRE PC**

Si vous n'avez pas pgAdmin installé localement :

```bash
# Sur Linux (Debian/Ubuntu)
sudo apt install pgadmin4

# Sur Linux (Fedora/RHEL)
sudo dnf install pgadmin4

# Sur macOS
brew install --cask pgadmin4

# Sur Windows
# Téléchargez depuis https://www.pgadmin.org/download/
```

**Alternative : Utiliser la version Docker de pgAdmin en local**
```bash
docker run -d \
  --name pgadmin-local \
  -p 5050:80 \
  -e PGADMIN_DEFAULT_EMAIL=admin@local.com \
  -e PGADMIN_DEFAULT_PASSWORD=admin \
  dpage/pgadmin4

# Accédez à http://localhost:5050
```

---

**3️⃣ Configurer la connexion dans pgAdmin**

Dans pgAdmin sur votre machine :

1. Clic droit sur **"Servers"** → **"Register"** → **"Server..."**

2. **Onglet "General"** :
   - **Name** : `Bank System Production`

3. **Onglet "Connection"** :

| Champ | Valeur | Note |
|-------|--------|------|
| **Host name/address** | `localhost` ou `127.0.0.1` | Via le tunnel SSH |
| **Port** | `5433` | Le port LOCAL du tunnel |
| **Maintenance database** | `bankdb` | Nom de votre base |
| **Username** | `bankuser` | Utilisateur de production |
| **Password** | `bankpass` | Mot de passe de production |
| **Save password?** | ✅ Cochez | Pour éviter de retaper |

4. **Cliquez sur "Save"**

✅ **C'est fait !** Vous voyez maintenant vos données de production en toute sécurité via le tunnel chiffré !

#### **💡 Astuce : Script pour automatiser le tunnel**

Créez un fichier `connect-prod-db.sh` dans votre projet :

```bash
#!/bin/bash

echo "🔐 Création du tunnel SSH vers la base de production..."
echo "📊 PostgreSQL sera accessible sur localhost:5433"
echo ""
echo "⚠️  Gardez ce terminal ouvert tant que vous utilisez pgAdmin"
echo "⌨️  Appuyez sur Ctrl+C pour fermer le tunnel"
echo ""

# Remplacez par vos vraies valeurs
SERVER_USER="root"
SERVER_HOST="votre-serveur.com"
SSH_KEY="~/.ssh/id_rsa"

ssh -i $SSH_KEY -L 5433:localhost:5432 $SERVER_USER@$SERVER_HOST

echo ""
echo "✅ Tunnel fermé proprement"
```

Rendez-le exécutable et utilisez-le :
```bash
chmod +x connect-prod-db.sh
./connect-prod-db.sh
```

#### **🔍 Vérifier que le tunnel fonctionne**

Dans un autre terminal (pendant que le tunnel est actif) :

```bash
# Test de connexion via le tunnel
psql -h localhost -p 5433 -U bankuser -d bankdb

# Si ça se connecte, votre tunnel fonctionne ! 🎉
```

#### **⚠️ Bonnes pratiques de sécurité**

- ✅ **N'exposez JAMAIS** PostgreSQL directement sur Internet (pas de `0.0.0.0:5432`)
- ✅ **Utilisez des clés SSH** plutôt que des mots de passe
- ✅ **Fermez le tunnel** quand vous ne l'utilisez plus
- ✅ **Utilisez des mots de passe forts** pour PostgreSQL
- ✅ **Activez l'authentification à deux facteurs** sur votre serveur si possible
- ✅ **Loggez tous les accès** à la base de données

---

### **Méthode 4 : Avec un client SQL (DBeaver, DataGrip, etc.)**

Vous pouvez également utiliser votre client SQL préféré avec le tunnel SSH :

**Pour le développement local (sans tunnel) :**
- **Type** : PostgreSQL
- **Host** : `localhost`
- **Port** : `5432`
- **Database** : `bankdb`
- **Username** : `bankuser`
- **Password** : `bankpass`

**Pour la production (avec tunnel SSH intégré) :**

La plupart des clients SQL modernes supportent SSH nativement. Exemple avec **DBeaver** :

1. **Nouvelle connexion PostgreSQL**
2. **Onglet "Main"** :
   - Host: `localhost`
   - Port: `5432` (port sur le serveur)
   - Database: `bankdb`
   - Username: `bankuser`
   - Password: `bankpass`

3. **Onglet "SSH"** :
   - ✅ Cochez **"Use SSH Tunnel"**
   - Host/IP: `votre-serveur.com`
   - Port: `22`
   - Username: `root`
   - Authentication: **Public Key** (recommandé) ou **Password**
   - Private key: `/home/vous/.ssh/id_rsa`

4. **Test Connection** → ✅ Connected !

DBeaver créera automatiquement le tunnel SSH pour vous.

---

### **🎯 Résumé : Accès à la base de données**

| Environnement | Méthode | URL/Commande | Identifiants |
|---------------|---------|--------------|--------------|
| **Développement** | pgAdmin (Web UI) | http://localhost:5050 | admin@banksystem.com / admin123 |
| **Développement** | PostgreSQL Direct | `docker exec -it bank-postgres psql -U bankuser -d bankdb` | bankuser / bankpass |
| **Développement** | Client SQL | localhost:5432 | bankuser / bankpass |
| **Production** | SSH Tunnel + pgAdmin | `ssh -L 5433:localhost:5432 user@serveur` puis localhost:5433 | bankuser / bankpass |
| **Production** | SSH + psql | `ssh user@serveur` puis `docker exec -it bank-postgres-prod psql ...` | bankuser / bankpass |
| **Production** | Client SQL + SSH | Tunnel SSH intégré dans le client | bankuser / bankpass |

**⚠️ Important** : En production, pgAdmin n'est **JAMAIS** exposé sur Internet. Utilisez toujours un tunnel SSH sécurisé.

## 🔄 CI/CD

### GitHub Actions (exemple)

Créez `.github/workflows/ci.yml` :

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: bankdb
          POSTGRES_USER: bankuser
          POSTGRES_PASSWORD: bankpass
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    
    - name: Build with Maven
      run: mvn clean install -DskipTests
    
    - name: Run tests
      run: mvn test
    
    - name: Build Docker image
      run: docker build -t bank-system:${{ github.sha }} .
```

## 🤝 Contribuer

Les contributions sont les bienvenues ! Voici comment contribuer :

1. **Fork** le projet
2. Créez votre branche de fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Pushez vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une **Pull Request**

### Guidelines

- Respecter les conventions de code existantes
- Ajouter des tests pour les nouvelles fonctionnalités
- Mettre à jour la documentation si nécessaire
- Écrire des messages de commit clairs et descriptifs

## 📝 Licence

Ce projet est sous licence Apache 2.0 - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Auteurs

- **Votre Nom** - *Travail initial* - [VotreGitHub](https://github.com/votre-username)

## 🙏 Remerciements

- Spring Boot pour le framework excellent
- PostgreSQL pour la base de données robuste
- La communauté open source pour les outils et bibliothèques

## 📞 Support

Pour toute question ou problème :

- 📧 Email : support@banksystem.com
- 🐛 Issues : [GitHub Issues](https://github.com/votre-username/bank-system/issues)
- 📖 Documentation : [Wiki](https://github.com/votre-username/bank-system/wiki)

---

⭐ **N'oubliez pas de donner une étoile si ce projet vous a aidé !** ⭐