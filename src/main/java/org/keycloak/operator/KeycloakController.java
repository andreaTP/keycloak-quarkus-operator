/**
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.keycloak.operator;

import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.keycloak.operator.crds.Keycloak;
import org.keycloak.operator.crds.KeycloakStatus;

import java.util.logging.Logger;

import static org.keycloak.operator.crds.KeycloakStatus.State.*;

@Controller(namespaces = Controller.WATCH_CURRENT_NAMESPACE, finalizerName = Controller.NO_FINALIZER)
public class KeycloakController implements ResourceController<Keycloak> {

    Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Inject
    KubernetesClient client;

    @Override
    public DeleteControl deleteResource(Keycloak joke, Context<Keycloak> context) {
        return DeleteControl.DEFAULT_DELETE;
    }

    @Override
    public UpdateControl<Keycloak> createOrUpdateResource(Keycloak kc, Context<Keycloak> context) {
        final var spec = kc.getSpec();

        KeycloakStatus status = kc.getStatus();
        var deployment = new KeycloakDeployment(client);

        try {
            var kcDeployment = deployment.getKeycloakDeployment(kc);

            if (kcDeployment == null) {
                // Need to create the deployment
                deployment.createKeycloakDeployment(kc);
            }

            var nextStatus = deployment.getNextStatus(spec, status, kcDeployment);

            if (nextStatus != null) {
                kc.setStatus(nextStatus);
                return UpdateControl.updateStatusSubResource(kc);
            } else {
                return UpdateControl.noUpdate();
            }
        } catch (Exception e) {
            status = new KeycloakStatus();
            status.setMessage("Error performing operations:\n" + e.getMessage());
            status.setState(ERROR);
            status.setError(true);

            kc.setStatus(status);
            return UpdateControl.updateStatusSubResource(kc);
        }
    }
}
