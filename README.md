# Projet E-Commerce - Architecture Microservices avec Domain-Driven Design (DDD)

##  Objectif du Projet

Ce projet académique a pour objectif principal de démontrer l'application pratique de **Domain-Driven Design (DDD)** dans un contexte d'architecture microservices. Il illustre comment les concepts et patterns DDD peuvent être utilisés pour concevoir et implémenter un système e-commerce complexe, en mettant l'accent sur la modélisation du domaine métier et la séparation des responsabilités.

---

##  Qu'est-ce que Domain-Driven Design (DDD) ?

**Domain-Driven Design** est une approche de conception logicielle introduite par Eric Evans dans son livre éponyme. DDD se concentre sur la modélisation du logiciel pour refléter fidèlement le domaine métier, en plaçant la logique métier au centre de l'architecture.

### Principes Fondamentaux de DDD

1. **Ubiquitous Language (Langage Universel)** : Un vocabulaire partagé entre développeurs et experts métier
2. **Bounded Context (Contexte Délimité)** : Délimitation claire des modèles de domaine
3. **Strategic Design** : Organisation des contextes délimités et de leurs relations
4. **Tactical Design** : Patterns de modélisation au sein d'un contexte délimité

---

##  Architecture DDD dans ce Projet

Ce projet applique DDD à travers une architecture microservices où chaque service représente un **Bounded Context** distinct :

```
┌─────────────────────────────────────────────────────────────┐
│                    E-Commerce Platform                       │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Order        │  │ Product      │  │ Stock        │      │
│  │ Context      │  │ Context      │  │ Context      │      │
│  │ (DDD)        │  │ (DDD)        │  │ (CQRS/ES)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Auth         │  │ Payment     │  │ Shipping     │      │
│  │ Context      │  │ Context     │  │ Context      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

##  Étapes d'Implémentation DDD

### Phase 1 : Découverte du Domaine (Domain Discovery)

#### 1.1 Event Storming
- Identification des événements métier
- Cartographie des flux de processus
- Identification des agrégats et entités

#### 1.2 Définition du Langage Universel
- Création d'un glossaire métier partagé
- Exemples dans ce projet :
  - **Order** (Commande) : Agrégat racine représentant une commande client
  - **OrderLine** : Entité représentant une ligne de commande
  - **OrderStatus** : Objet valeur représentant l'état d'une commande
  - **StockItem** : Agrégat représentant un item de stock

### Phase 2 : Identification des Bounded Contexts

Chaque microservice représente un contexte délimité avec son propre modèle de domaine :

| Service | Bounded Context | Responsabilité |
|---------|----------------|----------------|
| **Order Service** | Gestion des Commandes | Création, suivi et gestion du cycle de vie des commandes |
| **Product Service** | Catalogue Produits | Gestion du catalogue, catégories, collections |
| **Stock Service** | Gestion des Stocks | Suivi des quantités, réservations, alertes |
| **Auth Service** | Authentification | Gestion des utilisateurs et authentification |
| **Payment Service** | Paiements | Traitement des transactions de paiement |
| **Shipping Service** | Expédition | Gestion des livraisons |

### Phase 3 : Modélisation Tactique (Tactical Patterns)

#### 3.1 Agrégats (Aggregates)

**Définition** : Un agrégat est un cluster d'objets de domaine traités comme une unité pour les changements de données. Il a une racine d'agrégat (Aggregate Root) qui est le seul point d'entrée.

**Exemple : Order (Order Service)**

```java
public class Order {
    // Racine d'agrégat
    private Long id;
    private String orderNumber;
    private OrderStatus status;  // Value Object
    private DeliveryAddress deliveryAddress;  // Value Object
    private List<OrderLine> orderLines;  // Entités enfants
    
    // Logique métier encapsulée
    public void confirmOrder(String confirmedBy) {
        if (!status.canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new IllegalStateException("Transition invalide");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }
}
```

**Caractéristiques** :
-  Encapsule la logique métier
-  Garantit l'intégrité des invariants
-  Point d'entrée unique pour les modifications
-  Gère les transitions d'état

#### 3.2 Entités (Entities)

**Définition** : Objets identifiés par leur identité plutôt que par leurs attributs.

**Exemple : OrderLine**

```java
public class OrderLine {
    private Long id;  // Identité
    private String productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    
    // Logique métier
    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

#### 3.3 Objets Valeur (Value Objects)

**Définition** : Objets définis uniquement par leurs attributs, immutables et sans identité.

**Exemple : OrderStatus**

```java
public enum OrderStatus {
    PENDING, CONFIRMED, PAID, SHIPPED, DELIVERED, CANCELLED;
    
    // Logique métier : règles de transition
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == PAID || newStatus == CANCELLED;
            // ... autres transitions valides
        };
    }
}
```

**Autres Value Objects dans le projet** :
- `DeliveryAddress` : Adresse de livraison
- `BillingAddress` : Adresse de facturation
- `PaymentSummary` : Résumé de paiement
- `StockThreshold` : Seuils de stock (Stock Service)

#### 3.4 Services du Domaine (Domain Services)

**Définition** : Services contenant de la logique métier qui ne peut pas être placée naturellement dans une entité ou un objet valeur.

**Exemples dans Order Service** :

```java
@Service
public class OrderCreationService {
    // Logique métier complexe nécessitant plusieurs agrégats
    public Order createOrder(Long customerId, 
                           DeliveryAddress deliveryAddress,
                           List<OrderLine> orderLines) {
        // Validation
        orderValidationService.validateOrderCreation(...);
        
        // Génération du numéro unique
        String orderNumber = generateUniqueOrderNumber();
        
        // Création
        return new Order(orderNumber, customerId, ...);
    }
}
```

**Services du domaine identifiés** :
- `OrderCreationService` : Création de commandes
- `OrderValidationService` : Validation des règles métier
- `OrderLifecycleService` : Gestion du cycle de vie
- `OrderDiscountService` : Calcul des remises
- `ProductDomainService` : Logique métier produits

#### 3.5 Repositories (Domain Repositories)

**Définition** : Abstractions pour la persistance, définies dans le domaine, implémentées dans l'infrastructure.

**Exemple** :

```java
// Interface dans le domaine
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerId(Long customerId);
}

// Implémentation dans l'infrastructure
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final JpaOrderRepository jpaRepository;
    private final OrderMapper mapper;
    // ...
}
```

#### 3.6 Événements du Domaine (Domain Events)

**Définition** : Événements qui représentent quelque chose d'important qui s'est produit dans le domaine.

**Exemples** :

```java
public class OrderPlaced {
    private String orderId;
    private Long customerId;
    private LocalDateTime occurredAt;
    private List<OrderLineDTO> orderLines;
}

public class OrderConfirmed {
    private String orderId;
    private LocalDateTime confirmedAt;
}
```

**Événements publiés** :
- `OrderPlaced` : Commande créée
- `OrderConfirmed` : Commande confirmée
- `OrderStatusChanged` : Changement de statut
- `OrderCancelled` : Commande annulée
- `StockReserved` : Stock réservé (Stock Service)
- `StockReleased` : Stock libéré (Stock Service)

### Phase 4 : Architecture en Couches (Layered Architecture)

Chaque service suit une architecture en couches respectant les principes DDD :

```
┌─────────────────────────────────────────┐
│      Infrastructure Layer                │
│  - Controllers (REST)                    │
│  - Repositories (JPA)                    │
│  - Messaging (Kafka)                     │
│  - Security                              │
└─────────────────────────────────────────┘
           ↕
┌─────────────────────────────────────────┐
│      Application Layer                    │
│  - Application Services                  │
│  - DTOs                                  │
│  - Event Handlers                        │
└─────────────────────────────────────────┘
           ↕
┌─────────────────────────────────────────┐
│      Domain Layer                        │
│  - Aggregates                            │
│  - Entities                              │
│  - Value Objects                         │
│  - Domain Services                       │
│  - Domain Events                         │
│  - Repository Interfaces                 │
└─────────────────────────────────────────┘
```

**Structure dans Order Service** :

```
order-service/
├── domain/
│   ├── model/
│   │   ├── aggregate/          # Agrégats
│   │   │   └── Order.java
│   │   ├── entity/              # Entités
│   │   │   ├── OrderLine.java
│   │   │   └── OrderModificationHistory.java
│   │   └── valueobject/         # Objets valeur
│   │       ├── OrderStatus.java
│   │       ├── DeliveryAddress.java
│   │       └── PaymentSummary.java
│   ├── service/                  # Services du domaine
│   │   ├── OrderCreationService.java
│   │   ├── OrderValidationService.java
│   │   └── OrderLifecycleService.java
│   ├── repository/               # Interfaces repositories
│   │   └── OrderRepository.java
│   └── event/                    # Événements du domaine
│       ├── OrderPlaced.java
│       └── OrderConfirmed.java
├── application/
│   ├── service/                  # Services applicatifs
│   ├── dto/                      # DTOs
│   └── eventhandler/            # Gestionnaires d'événements
└── infrastructure/
    ├── controller/               # Contrôleurs REST
    ├── repository/              # Implémentations repositories
    ├── persistence/              # Entités JPA
    └── messaging/                 # Intégration Kafka
```

---

##  Patterns DDD Utilisés

### 1. Aggregate Root Pattern
- **Order** est la racine d'agrégat pour toutes les opérations sur les commandes
- **StockItem** est la racine d'agrégat pour la gestion des stocks

### 2. Repository Pattern
- Abstraction de la persistance
- Séparation entre interface (domaine) et implémentation (infrastructure)

### 3. Domain Events Pattern
- Découplage entre contextes délimités
- Communication asynchrone via Kafka

### 4. Specification Pattern
- Validation des règles métier complexes
- Exemple : `OrderValidationService`

### 5. Factory Pattern
- Création d'agrégats complexes
- Exemple : `OrderCreationService`

### 6. CQRS (Command Query Responsibility Segregation)
- Implémenté dans **Stock Service** avec Axon Framework
- Séparation entre commandes (écriture) et requêtes (lecture)

### 7. Event Sourcing
- Implémenté dans **Stock Service**
- Historique complet des événements de domaine

---

##  Exemple Concret : Order Service

### Modélisation du Domaine

#### Agrégat : Order

```java
public class Order {
    // Identité
    private Long id;
    private String orderNumber;
    
    // Value Objects
    private OrderStatus status;
    private DeliveryAddress deliveryAddress;
    private BillingAddress billingAddress;
    private PaymentSummary paymentSummary;
    
    // Entités enfants
    private List<OrderLine> orderLines;
    private List<OrderModificationHistory> modificationHistory;
    
    // Logique métier encapsulée
    public void confirmOrder(String confirmedBy) {
        // Validation de la transition
        if (!status.canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new IllegalStateException("Transition invalide");
        }
        
        // Mise à jour de l'état
        OrderStatus previousStatus = this.status;
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        
        // Enregistrement de l'historique
        addModificationHistory(previousStatus, OrderStatus.CONFIRMED, 
                             "Commande confirmée", confirmedBy);
    }
    
    // Calcul métier
    private PaymentSummary calculatePaymentSummary() {
        BigDecimal subtotal = orderLines.stream()
            .map(OrderLine::calculateSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        // ... calcul taxes, remises, total
    }
}
```

#### Value Object : OrderStatus

```java
public enum OrderStatus {
    PENDING, CONFIRMED, PAID, PREPARING, 
    READY_FOR_SHIPMENT, SHIPPED, DELIVERED, 
    CANCELLED, REFUNDED;
    
    // Règles métier : transitions valides
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == PAID || newStatus == CANCELLED;
            case PAID -> newStatus == PREPARING || newStatus == CANCELLED;
            // ... autres règles
            case CANCELLED, REFUNDED -> false; // États finaux
        };
    }
}
```

#### Service du Domaine : OrderValidationService

```java
@Service
public class OrderValidationService {
    
    public void validateOrderCreation(Long customerId,
                                    DeliveryAddress deliveryAddress,
                                    List<OrderLine> orderLines) {
        // Règles métier
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID requis");
        }
        
        if (orderLines == null || orderLines.isEmpty()) {
            throw new IllegalArgumentException("Commande doit contenir au moins un produit");
        }
        
        // Validation des quantités
        orderLines.forEach(line -> {
            if (line.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantité invalide");
            }
        });
    }
}
```

---

##  Architecture Hexagonale (Ports & Adapters)

Le projet applique également l'architecture hexagonale pour une meilleure séparation des préoccupations :

```
                    ┌─────────────────┐
                    │   Domain Layer  │
                    │  (Business Core)│
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌───────▼────────┐  ┌───────▼────────┐
│  REST Adapter  │  │  Kafka Adapter │  │  JPA Adapter    │
│  (Port In)     │  │  (Port Out)    │  │  (Port Out)     │
└────────────────┘  └────────────────┘  └─────────────────┘
```

**Ports (Interfaces)** :
- **Ports In** : Controllers REST, Event Handlers
- **Ports Out** : Repository Interfaces, Event Publishers

**Adapters (Implémentations)** :
- **Adapters In** : REST Controllers, Kafka Consumers
- **Adapters Out** : JPA Repositories, Kafka Producers

---

##  Communication entre Contextes Délimités

### Pattern : Domain Events via Message Broker

Les contextes délimités communiquent via des événements de domaine publiés sur Kafka :

```
Order Service                    Stock Service
     │                                │
     │  OrderPlaced Event             │
     ├───────────────────────────────>│
     │                                │  StockReserved Event
     │                                ├─────────────────────>
     │                                │
     │  OrderConfirmed Event          │
     ├───────────────────────────────>│
```

**Exemple de flux** :
1. **Order Service** publie `OrderPlaced`
2. **Stock Service** écoute et réserve le stock
3. **Stock Service** publie `StockReserved`
4. **Order Service** peut confirmer la commande

---

##  Technologies et Outils

### Framework & Bibliothèques
- **Spring Boot 3.5.0** : Framework principal
- **Spring Cloud 2025.0.0** : Microservices
- **Java 21** : Langage de programmation
- **Axon Framework** : CQRS/Event Sourcing (Stock Service)
- **MapStruct** : Mapping objet-objet
- **Lombok** : Réduction du boilerplate

### Infrastructure
- **PostgreSQL** : Base de données relationnelle
- **Kafka** : Message broker pour événements
- **Elasticsearch** : Recherche de produits
- **Redis** : Cache (prévu)
- **Eureka** : Service discovery
- **Spring Cloud Config** : Configuration centralisée
- **Docker** : Containerisation
- **Kubernetes** : Orchestration (manifests préparés)

### Outils de Développement
- **Flyway** : Migrations de base de données
- **OpenAPI/Swagger** : Documentation API
- **Maven** : Gestion des dépendances

---

##  Configuration Centralisée (Config Repository)

### Concept et Architecture

Le projet utilise **Spring Cloud Config Server** pour centraliser la configuration de tous les microservices. Cette approche permet de :

- ✅ **Centraliser** toutes les configurations dans un seul dépôt Git
- ✅ **Séparer** les configurations par environnement (dev, prod, docker)
- ✅ **Sécuriser** les secrets via variables d'environnement
- ✅ **Versionner** les changements de configuration
- ✅ **Dynamiser** la configuration sans redéployer les services

### Architecture du Config Repository

```
┌─────────────────────────────────────────────────────────┐
│              Config Repository (GitHub)                   │
│  https://github.com/ImaneIdbali02/config-repo           │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  config-repo/                                            │
│  ├── global.yml              # Configuration globale     │
│  ├── application.yml         # Config par défaut        │
│  ├── product-service.yml     # Config product-service   │
│  ├── order-service.yml      # Config order-service     │
│  ├── stock-service.yml      # Config stock-service     │
│  ├── auth-service.yml       # Config auth-service      │
│  └── ... (autres services)                              │
│                                                           │
└─────────────────────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────────────────────┐
│         Config Server (Spring Cloud Config)             │
│         Port: 8888                                       │
├─────────────────────────────────────────────────────────┤
│  - Clone le repository Git au démarrage                 │
│  - Expose les configurations via REST API                │
│  - Support des profils (local, docker, prod)            │
└─────────────────────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────────────────────┐
│              Microservices (Clients)                     │
│  - product-service                                        │
│  - order-service                                         │
│  - stock-service                                         │
│  - auth-service                                          │
│  - ...                                                    │
└─────────────────────────────────────────────────────────┘
```

### Structure du Config Repository

Le repository `config-repo` contient :

```
config-repo/
├── global.yml                    # Configuration partagée
│   ├── JPA/Hibernate settings
│   ├── Kafka configuration
│   ├── Actuator endpoints
│   └── Logging configuration
│
├── product-service.yml          # Configuration spécifique
│   ├── Database (PostgreSQL)
│   ├── Elasticsearch
│   ├── AWS S3 (images)
│   └── Service-specific settings
│
├── order-service.yml
├── stock-service.yml
├── auth-service.yml
└── ... (autres services)
```

### Exemple de Configuration

**global.yml** (Configuration partagée) :
```yaml
# Configuration JPA globale
jpa:
  hibernate:
    ddl-auto: update
  properties:
    hibernate:
      dialect: org.hibernate.dialect.PostgreSQLDialect
      format_sql: true

# Configuration Kafka globale
kafka:
  bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
  consumer:
    group-id: ecommerce-group
    auto-offset-reset: earliest
  producer:
    key-serializer: org.apache.kafka.common.serialization.StringSerializer
    value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

# Configuration Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

**product-service.yml** (Configuration spécifique) :
```yaml
server:
  port: 8082

spring:
  application:
    name: product-service
  
  datasource:
    url: jdbc:postgresql://postgres-product:5432/product_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  elasticsearch:
    uris: http://localhost:9200
    username: ${ELASTIC_USERNAME}
    password: ${ELASTIC_PASSWORD}

# AWS S3 Configuration
aws:
  s3:
    access-key: ${ACCESS_KEY}
    secret-key: ${SECRET_KEY}
    region: eu-north-1
    bucket: enaya
```

### Configuration du Config Server

**config-service/application.yml** :
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/ImaneIdbali02/config-repo.git
          username: ${GIT_USERNAME}
          password: ${GIT_PASSWORD}
          default-label: master
          clone-on-start: true
          force-pull: true
```

### Utilisation par les Microservices

Chaque microservice se connecte au Config Server via `bootstrap.yml` :

```yaml
spring:
  application:
    name: product-service  # Nom du service (correspond au fichier YAML)
  cloud:
    config:
      uri: http://config-service:8888
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 6
```

### Profils de Configuration

Le Config Server supporte les profils pour différents environnements :

- **local** : `product-service-local.yml` - Développement local
- **docker** : `product-service-docker.yml` - Environnement Docker
- **prod** : `product-service-prod.yml` - Production

Les services peuvent spécifier le profil via :
```yaml
spring:
  profiles:
    active: docker  # ou local, prod
```

### Avantages de cette Approche

1. **Sécurité** : Les secrets ne sont pas dans le code, mais dans des variables d'environnement
2. **Centralisation** : Une seule source de vérité pour toutes les configurations
3. **Versionnement** : Historique complet des changements via Git
4. **Flexibilité** : Changements de configuration sans redéploiement (avec refresh)
5. **Séparation** : Configuration par service et par environnement

### Repository GitHub

Le config-repo est un **repository GitHub séparé** :
- **URL** : `https://github.com/ImaneIdbali02/config-repo`
- **Structure** : Un fichier YAML par service
- **Sécurité** : Secrets gérés via variables d'environnement (`.env`)

---

##  Structure du Projet

```
bijouterie-ecommerce/
├── api-gateway/              # API Gateway (Spring Cloud Gateway)
├── src/
│   ├── auth-service/        # Service d'authentification
│   ├── product-service/     # Service produits (DDD)
│   ├── order-service/       # Service commandes (DDD) 
│   ├── stock_service/       # Service stock (CQRS/ES) 
│   ├── payment-service/     # Service paiement
│   ├── shipping-service/    # Service expédition
│   ├── config-service/      # Configuration centralisée
│   └── eureka-server/       # Service discovery
├── k8s/                     # Manifests Kubernetes
├── docs/                    # Documentation
│   ├── api-spec/            # Spécifications OpenAPI
│   └── architecture/        # Diagrammes d'architecture
├── scripts/                 # Scripts utilitaires
└── docker-compose.yml       # Orchestration locale
```

---

##  Apprentissages et Concepts DDD Démontrés

### Concepts Stratégiques
-  **Bounded Contexts** : Chaque service = un contexte délimité
-  **Ubiquitous Language** : Vocabulaire métier cohérent
-  **Context Mapping** : Relations entre contextes

### Concepts Tactiques
-  **Aggregates** : Order, StockItem
-  **Entities** : OrderLine, OrderModificationHistory
-  **Value Objects** : OrderStatus, DeliveryAddress, PaymentSummary
-  **Domain Services** : Services métier complexes
-  **Repositories** : Abstraction de la persistance
-  **Domain Events** : Communication asynchrone

### Patterns Avancés
-  **CQRS** : Séparation commande/requête (Stock Service)
-  **Event Sourcing** : Historique complet (Stock Service)
-  **Hexagonal Architecture** : Ports & Adapters
-  **Outbox Pattern** : Cohérence transactionnelle

---

##  Démarrage Rapide

### Prérequis
- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+
- Kafka (via Docker)

### Installation

1. **Cloner le projet**
```bash
git clone <repository-url>
cd bijouterie-ecommerce
```

2. **Configurer les variables d'environnement**
```bash
# Créer un fichier .env avec vos configurations
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
# ... autres variables
```

3. **Démarrer l'infrastructure**
```bash
docker-compose up -d postgres-auth postgres-product postgres-stock kafka zookeeper elasticsearch
```

4. **Démarrer les services**
```bash
# Démarrer config-service et eureka-server d'abord
# Puis les autres services
```

---

##  Objectifs Pédagogiques Atteints

Ce projet démontre la maîtrise de :

1. **Conception** : Application des principes DDD
2. **Modélisation** : Identification et modélisation des agrégats, entités, objets valeur
3. **Architecture** : Architecture hexagonale et en couches
4. **Patterns** : Application de patterns DDD (Repository, Domain Events, CQRS, ES)
5. **Microservices** : Découpage en contextes délimités
6. **Communication** : Communication asynchrone via événements

---

##  Notes Importantes

- Ce projet est **académique** et met l'accent sur la **conception DDD**
- Certains services sont en cours de développement
- L'objectif principal est de démontrer la compréhension et l'application de DDD
- Les patterns DDD sont appliqués de manière explicite et documentée

---

##  Auteur

Projet académique réalisé dans le cadre de l'apprentissage de **Domain-Driven Design** et des architectures microservices.

---

##  Références

- **Domain-Driven Design** - Eric Evans (2003)
- **Implementing Domain-Driven Design** - Vaughn Vernon (2013)
- **Domain-Driven Design Distilled** - Vaughn Vernon (2016)
- **Microservices Patterns** - Chris Richardson

---

##  Licence

Ce projet est à des fins éducatives et académiques.

