package org.keycloak.operator;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.keycloak.operator.crds.Keycloak;
import org.keycloak.operator.crds.KeycloakSpec;
import org.keycloak.operator.crds.KeycloakStatus;

import static org.keycloak.operator.crds.KeycloakStatus.State.*;

public class KeycloakDeployment {

    KubernetesClient client = null;

    KeycloakDeployment(KubernetesClient client) {
        this.client = client;
    }

    public Deployment getKeycloakDeployment(Keycloak keycloak) {
        // TODO this should be done through an informer to leverage caches
        return client
                .apps()
                .deployments()
                .inNamespace(keycloak.getMetadata().getNamespace())
                .withName(Constants.NAME)
                .get();
    }

    public void createKeycloakDeployment(Keycloak keycloak) {
        new DeploymentBuilder()
                .withNewMetadata()
                    .withName(Constants.NAME)
                    .withNamespace(keycloak.getMetadata().getNamespace())
                    .addToLabels(Constants.DEFAULT_LABELS)
                    .addToLabels(Constants.MANAGED_BY_LABEL, Constants.MANAGED_BY_VALUE)
                    .addNewOwnerReference()
                        .withApiVersion(Constants.CRDS_VERSION)
                        .withKind(keycloak.getKind())
                        .withName(keycloak.getMetadata().getName())
                        .withUid(keycloak.getMetadata().getUid())
                    .endOwnerReference()
                .endMetadata()

                .withNewSpec()
                .withReplicas(keycloak.getSpec().getInstances())
                .withNewSelector()
                    .addToMatchLabels(Constants.DEFAULT_LABELS)
                .endSelector()

                .withNewTemplate()
                .withNewSpec()

                .addNewInitContainer()
                .withName("init-container")
                .withImage(Constants.DEFAULT_KEYCLOAK_INIT_IMAGE)
                .endInitContainer()

                .addNewContainer()
                .withName(Constants.NAME)
                .withImage(Constants.DEFAULT_KEYCLOAK_IMAGE)
                .addNewPort()
                .withContainerPort(8443)
                .withProtocol("HTTP")
                .endPort()
                .endContainer()

                .endSpec()

                .endTemplate()
                .endSpec()
                .build();
    }

    public KeycloakStatus getNextStatus(KeycloakSpec desired, KeycloakStatus prev, Deployment current) {

        var isReady = (current != null &&
                current.getStatus() != null &&
                current.getStatus().getReadyReplicas() != null &&
                current.getStatus().getReadyReplicas() == desired.getInstances());

        switch (prev.getState()) {
            case READY:
                if (isReady) {
                    return null;
                } else {
                    var newStatus = prev.clone();
                    newStatus.setState(UNKNOWN);
                    newStatus.setMessage("Keycloak deployment is NOT ready");
                    return newStatus;
                }
            case ERROR:
            case UNKNOWN:
                if (isReady) {
                    var newStatus = prev.clone();
                    newStatus.setState(READY);
                    newStatus.setMessage("Keycloak deployment is ready!");
                    return newStatus;
                } else {
                    return null;
                }
        }

        throw new RuntimeException("unreachable");
    }

}
