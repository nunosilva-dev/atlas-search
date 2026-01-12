CLUSTER_NAME=atlas-cluster
IMAGE_NAME=atlas-search:v1

down:
	kind delete cluster --name $(CLUSTER_NAME)

create-cluster:
	kind create cluster --config k8s/kind/cluster-config.yml --name $(CLUSTER_NAME) || true

install-platform:
	kubectl apply -f k8s/platform/

build-app:
	docker run --rm -v "$(PWD)":/usr/src/app -w /usr/src/app maven:3.9.6-eclipse-temurin-21 mvn clean package -DskipTests
	docker build -t $(IMAGE_NAME) .
	kind load docker-image $(IMAGE_NAME) --name $(CLUSTER_NAME)

install-app:
	kubectl apply -f k8s/app/

install-monitoring:
	kubectl create namespace monitoring || true
	helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
	helm repo update
	helm upgrade --install atlas-monitoring prometheus-community/kube-prometheus-stack -n monitoring --wait

up: create-cluster install-platform install-monitoring build-app install-app