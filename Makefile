SHELL := /bin/bash

APP_VERSION ?= 0.0.1
NAMESPACE ?= demo
KIND_CLUSTER ?= order-platform

# images
IMG_GATEWAY := demo/gateway:$(APP_VERSION)
IMG_ORDER := demo/order-service:$(APP_VERSION)
IMG_PAYMENT := demo/payment-service:$(APP_VERSION)
IMG_INVENTORY := demo/inventory-service:$(APP_VERSION)
IMG_NOTIFICATION := demo/notification-service:$(APP_VERSION)

.PHONY: help
help:
	@echo ""
	@echo "Targets:"
	@echo "make up					-> docker compose up (rabbitmq + 3 postgres"
	@echo "make dow					-> docker compose dow"
	@echo "make build				-> gradle build"
	@echo "make run-<svc>			-> run service locally (gateway/order/payment/inventory/notification)"
	@echo "make build-images		-> build Docker images"
	@echo "make kind-up				-> create kind cluster"
	@echo "make kind-load			-> load images into kind"
	@echo "make k8s-apply			-> kubectl apply manifests"
	@echo "make metrics-install		-> install metrics-server (kind)"
	@echo "make hpa-apply			-> apply HPA manifests"
	@echo "make demo				-> create sample order via gateway"
	@echo ""

.PHONY: up down
up:
	docker compose up -d

down:
	docker compose down

.PHONY: build
build:
	./gradlew clean build

.PHONY: run-gateway run-order run-payment run-inventory run-notification
run-gateway:
	./gradlew :apps:gateway:bootrun

run-order:
	./gradlew :apps:order-service:bootrun

run-payment:
	./gradlew :apps:payment-service:bootrun

run-inventory:
	./gradlew :apps:inventory-service:bootrun

run-notification:
	./gradlew :apps:notification-service:bootrun

#-------- Docker images (uses per-service Dockerfile) -------
.PHONY: build-images
build-images:
	docker built -t $(IMG_GATEWAY) -f apps:gateway/Dockerfile .
	docker built -t $(IMG_ORDER) -f apps:order-service/Dockerfile .
	docker built -t $(IMG_PAYMENT) -f apps:payment-service/Dockerfile .
	docker built -t $(IMG_INVENTORY) -f apps:inventory-service/Dockerfile .
	docker built -t $(IMG_NOTIFICATION) -f apps:notification-service/Dockerfile .

#-------- kind / k8s -----
.PHONY: kind-up kind-delete kind-load
kind-up:
	kind create cluster --$(KIND_CLUSTER) --config kind-config.yaml || true

kind-delete:
	kind delete cluster --name $(KIND_CLUSTER)

kind-load:
	kind load docker-image $(IMG_GATEWAY) --name $(KIND_CLUSTER)
	kind load docker-image $(IMG_ORDER) --name $(KIND_CLUSTER)
	kind load docker-image $(IMG_PAYMENT) --name $(KIND_CLUSTER)
	kind load docker-image $(IMG_INVENTORY) --name $(KIND_CLUSTER)
	kind load docker-image $(IMG_NOTIFICATION) --name $(KIND_CLUSTER)

.PHONY: k8s-apply k8s-status
k8s-apply:
	kubectl apply -f k8s/all.yaml

k8s-status:
	kubectl get pods -n $(NAMESPACE)
	kubectl get svc -n $(NAMESPACE)

# metrics-server for HPA
.PHONY: metrics-install
metrics-install:
	kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml \
    kubectl -n kube-system patch deployment metrics-server --type='json' -p='[  \
    {"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'

.PHONY: hpa-apply
hpa-apply:
	kubectl apply -f k8s/hpa.yaml

# quick demo
.PHONY: demo
demo:
	@echo "Post /order via gateway ..."
	curl -s -X POST http://localhost:8080/api/v1/orders \
	-H "Content-Type: application/json" \
	-H "X-Correlation-Id: demo-123" \
	-d '{"CustomerId":"c123", "amount":120.50}' | jq .
	@echo ""
	@echo "Check payment:"
	@echo "	curl http:localhost:8080/api/v1/payments/orders/<ORDER_ID>"
	@echo "Check inventory:"
	@echo "	curl http:localhost:8080/api/v1/inventory/orders/ORDER_ID>"



