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