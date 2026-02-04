Architecture – Idempotence, Messaging, Observabilité & Kubernetes
1. Idempotence DB (Payment & Inventory)
   
🎯 Objectif

Garantir qu’une même commande ne crée qu’une seule transaction et qu’une seule réservation, même si RabbitMQ redélivre plusieurs fois le même event (at-least-once delivery).

🧠 Pourquoi

RabbitMQ garantit at-least-once, donc un event peut être livré plusieurs fois → risque de doublons.

🛠️ Comment

On utilise une contrainte unique sur order_id dans chaque service (source de vérité).

    payment-service  
    Table payment_transactions  
    Contrainte : uk_payment_order_id(order_id)

    inventory-service  
    Table reservations  
    Contrainte : uk_inventory_order_id(order_id)

✅ Résultat

    1 commande → 1 transaction payment

    1 commande → 1 réservation inventory
    Même si l’event est redélivré plusieurs fois.

2. Replay Outcome (Idempotence observable)

🎯 Objectif:

Si un duplicate arrive, on ne fait pas “rien” :
➡️ On rejoue le même outcome pour garder un workflow cohérent.

🧠 Pourquoi:

Le workflow doit rester déterministe même en cas de redelivery.

🛠️ Comment

Si orderId existe déjà en DB :
→ Republier l’event outcome déjà produit.

🔍 Preuve via headers RabbitMQ

    x-idempotent-replay=true → indique un replay (duplicate détecté)

    x-idempotency-key=<orderId> → traçabilité

    x-event-type=<EventClassName>

    x-event-version=v1

3. Correlation ID (HTTP + Events)
   
🎯 Objectif

Tracer tout le parcours d’une commande (gateway → services → events) avec un identifiant unique.

🛠️ Comment

    Gateway  
    Injecte X-Correlation-Id si absent.

    order-service  
    Lit le header HTTP et le copie dans les headers RabbitMQ.

    payment / inventory / notification  
    Lisent X-Correlation-Id depuis RabbitMQ et le republient sur leurs events.

✅ Résultat

Toutes les étapes d’une commande sont liées à un seul CID, facilitant le debugging et l’observabilité.

4. Kubernetes Readiness (Secrets, Probes, Resources, HPA)
   

🔐 Secrets: 

   Credentials DB + RabbitMQ stockés dans Secret (demo-secrets)

   Injectés via variables d’environnement

❤️ Health Probes

    readinessProbe → /actuator/health  
    Empêche d’envoyer du trafic à un pod non prêt

    livenessProbe → /actuator/health  
    Redémarre automatiquement en cas de blocage

⚙️ Resources

Définition de requests + limits CPU/memory
→ nécessaire pour la stabilité et l’autoscaling
📈 HPA (Horizontal Pod Autoscaler)

    Autoscaling activé (target CPU)

    metrics-server requis sur kind pour kubectl top et HPA

    hpa.yaml → définit les HPA pour chaque service

## Demo in 2 minutes (local or kind)

## Prerequisites
- Docker + Docker Compose
- Java 21 + Gradle
- (Optional) kind + kubectl
- (Optional) k6 + jq

### 1) Start infra (RabbitMQ + 3 Postgres)
```bash
make up

RabbitMQ UI: http://localhost:15672 (guest/guest)
```
### 2) Run services (5 terminals)
```bash
make run-order
make run-payment
make run-inventory
make run-notification
make run-gateway 
```
### 3) Create order via gateway (publishes event)
```bash
curl -X POST http://localhost:8080/api/v1/orders  \
    -H "Content-Type:application/json" \
    -H "X-Correlation-Id: demo-123" \
    -d '{"costumerId":"c123", "amount":4500.00}' | jq .
```
Copy the rerned orderId.

### 4) Verify payment + inventory (idempotence DB outcomes)
```bash
curl -s http://localhost:8080/api/v1/payments/orders/<ORDER_ID> | jq .
curl -s http://localhost:8080/api/v1/inventory/orders/<ORDER_ID> | jq .
```

### 5) Prove idempotent replay (publish duplicate event)

   1. Open RabbitMQ UI → Exchanges → order.events

   2. Publish with routing key order.created

   3. Reuse the same orderId in the JSON payload

Expected:

- No duplicate rows in DB (unique constraint on order_id)

- Outcome event is republished with header x-idempotent-replay=true

### 6) Swagger UI

- Order service: http://localhost:8081/swagger-ui.html

- Payment service: http://localhost:8082/swagger-ui.html

- Inventory service: http://localhost:8083/swagger-ui.html