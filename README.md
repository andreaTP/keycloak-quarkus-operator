## Keycloak operator based on Quarkus

The idea is that you create a `JokeRequest` custom resource that you apply to your cluster. The
operator will do its best to comply and create a `Joke` custom resource on your behalf if everything
went well. Jokes are retrieved from the https://v2.jokeapi.dev/joke API endpoint. The request can be
customized to your taste by specifying which category of jokes you'd like or the amount of
explicitness / topics you can tolerate. You can also request a "safe" joke which should be
appropriate in most settings.

### Quick start on Minikube

Compile the project generating the Docker image with JIB:

```bash
mvn clean package -Dquarkus.container-image.build=true
```

Load the Image in minikube:

```bash
minikube image load keycloak/keycloak-quarkus-operator:0.0.1-SNAPSHOT
```

Copy the produced Kubernetes resources in the `kustomize/base` directory:

```bash
cp -f target/kubernetes/* ./kustomize/base
```

And finally install the operator:

```bash
kubectl apply -k ./kustomize/overlays/dev
```
