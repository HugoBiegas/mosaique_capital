# 🏦 Mosaïque Capital

## 📋 Description du Projet
Mosaïque Capital est une application de gestion patrimoniale complète permettant aux utilisateurs de suivre, d'analyser et d'optimiser leur patrimoine à travers différents types d'actifs.

## 🛠️ Dépendances du Projet

### 🔧 Infrastructure
- **Java 21** - Version LTS avec fonctionnalités modernes
- **Spring Boot 3.4.5** - Framework d'application
- **Maven** - Gestion des dépendances

### 🔒 Sécurité
- **Spring Security** - Framework de sécurité
- **OAuth2 Resource Server** - Authentification standardisée
- **Spring Security Messaging** - Sécurité pour communications

### 💾 Persistance
- **Spring Data JPA** - ORM et persistence
- **H2 Database** - Base de données embarquée
- **Spring Integration JPA** - Intégration JPA

### 🌐 API et Web
- **Spring WebFlux** - APIs réactives
- **Spring HATEOAS** - API RESTful hypermedia
- **Spring Validation** - Validation de données

### 📨 Messagerie
- **Spring AMQP** - Messaging avec RabbitMQ
- **Spring Integration** - Framework d'intégration
- **Spring Integration AMQP** - Support AMQP
- **Spring Mail** - Envoi d'emails

### 🔍 Monitoring
- **Spring Boot Actuator** - Points de monitoring

### ⚙️ Développement
- **Lombok** - Réduction de boilerplate
- **Spring Boot DevTools** - Outils développement

### 🧪 Tests
- **Spring Boot Test** - Framework de test
- **Testcontainers** - Tests avec Docker
- **JUnit Jupiter** - Tests unitaires
- **Spring REST Docs** - Documentation API
- **Reactor Test** - Tests réactifs

## 📝 Fonctionnalités de l'API

### 🚀 Priorité 0 - MVP Essentiel

#### 👤 Authentification et Gestion Utilisateurs
- Création/gestion des comptes
- Stockage sécurisé des mots de passe
- Authentification à deux facteurs
- Gestion des sessions

#### 📊 Base de Données Patrimoniale
- Modèle pour différents types d'actifs
- Stockage des valorisations
- Historique des valeurs
- API CRUD pour les actifs

#### 🧮 Moteur de Calcul Basique
- Calcul du patrimoine net total
- Agrégation par catégories
- Calcul d'évolution temporelle
- Répartition patrimoniale

#### 🔐 Sécurité de Base
- Chiffrement des données sensibles
- Protection contre CSRF, XSS
- Journalisation des accès
- Limitation contre attaques force brute

### 🌟 Priorité 1 - Fonctionnalités Essentielles

#### 🏛️ Agrégation Bancaire
- Intégration Budget Insight
- Synchronisation comptes bancaires
- Stockage transactions
- Rafraîchissement données

#### 📈 Analyse Patrimoniale
- Calcul de performance
- Évaluation diversification
- Détection tendances
- Catégorisation transactions

#### 🔔 Système Notifications
- Notifications push
- Préférences utilisateur
- Déclenchement alertes
- Journalisation envois

#### 📱 API Mobile
- Endpoints optimisés
- Compression/pagination
- Auth par token
- Synchro hors ligne

### 💎 Priorité 2 - Fonctionnalités Avancées

#### 💹 Intégration Courtiers
- API Bourse Direct, Degiro
- Positions et transactions
- Synchro PEA/assurance-vie
- Mise à jour valorisations

#### 🪙 Intégration Crypto
- API Binance, Coinbase
- Transactions crypto
- Cours temps réel
- Sécurisation clés API

#### 💰 Moteur Fiscal
- Calcul IFI
- Plus-values par actif
- Données fiscales
- Rapports fiscaux

#### ⚙️ API Personnalisation
- Préférences utilisateur
- Config tableaux de bord
- Gestion widgets
- Vues personnalisées

### 🏆 Priorité 3 - Fonctionnalités Complexes

#### 🌍 Extension Agrégation
- Salt Edge
- Banques internationales
- Open Banking/PSD2
- Multi-devises

#### 🏠 Évaluation Immobilière
- Valorisation automatique
- Bases prix immobiliers
- Estimation par comparables
- Rentabilité locative

#### ⚠️ Analyse Risques
- Évaluation risques
- Simulation scénarios
- Corrélations actifs
- Suggestions diversification

#### 👨‍👩‍👧‍👦 Multi-comptes
- Gestion familiale
- Permissions/partage
- Agrégation multi-utilisateurs
- Journalisation accès

### 🔮 Priorité 4 - Fonctionnalités Premium

#### 📊 Optimisation Fiscale
- Détection opportunités
- Simulation stratégies
- Recommandations personnalisées
- Comparaison structures

#### 🤖 Intelligence Artificielle
- Prédiction flux financiers
- Recommandations investissement
- Catégorisation avancée
- Détection anomalies

#### 🔌 API Publique
- Infrastructure sécurisée
- Gestion clés API
- Webhooks
- Quotas et limites

#### 📜 Planification Successorale
- Simulation succession
- Transmission patrimoine
- Structures juridiques
- Documentation

### 🚁 Priorité 5 - Extensions Futures

#### 🌐 Infrastructure Internationale
- Multi-devises avancé
- Fiscalité internationale
- Institutions internationales
- Localisation/traduction

#### 🎓 Plateforme Éducative
- Contenu éducatif
- Suivi progression
- Recommandation contenu
- Intégration tiers

#### 🛒 Marketplace
- Produits financiers
- Comparaison automatisée
- Transactions sécurisées
- Suivi offres

#### 💼 Architecture B2B
- Multi-clients
- API conseillers
- Analyse portefeuille
- Rapports personnalisés