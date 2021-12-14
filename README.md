## Keycloak operator POC based on Quarkus

### Quick start on Minikube

Enable the Minikube Docker daemon:
```bash
eval $(minikube -p minikube docker-env)
```

Compile the project generating the Docker image with JIB:

```bash
mvn clean package -Dquarkus.container-image.build=true -Dquarkus.kubernetes.deployment-target=minikube
```

Install the CRD definition:

```bash
kubectl apply -f target/kubernetes/keycloaks.keycloak.org-v1.yml
```

And finally install the operator:

```bash
cp -f target/kubernetes/* kustomize/base
kubectl apply -k kustomize/base
```
