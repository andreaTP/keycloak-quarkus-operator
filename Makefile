
redeploy:
	kubectl delete keycloak example-kc | true
	kubectl delete deployments/keycloak-quarkus-operator | true
	eval $(minikube -p minikube docker-env)
	mvn clean package -Dquarkus.container-image.build=true -Dquarkus.kubernetes.deployment-target=minikube
	kubectl apply -f target/kubernetes/keycloaks.keycloak.org-v1.yml | true
	cp -f target/kubernetes/* kustomize/base
	kubectl apply -k kustomize/base
