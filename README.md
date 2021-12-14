## Keycloak operator based on Quarkus

The idea is that you create a `JokeRequest` custom resource that you apply to your cluster. The
operator will do its best to comply and create a `Joke` custom resource on your behalf if everything
went well. Jokes are retrieved from the https://v2.jokeapi.dev/joke API endpoint. The request can be
customized to your taste by specifying which category of jokes you'd like or the amount of
explicitness / topics you can tolerate. You can also request a "safe" joke which should be
appropriate in most settings.

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
