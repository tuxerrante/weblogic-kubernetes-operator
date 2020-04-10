// Copyright (c) 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.weblogic.kubernetes.extensions;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1JobList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.openapi.models.V1PersistentVolumeList;
import io.kubernetes.client.openapi.models.V1ReplicaSetList;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.openapi.models.V1ServiceAccountList;
import oracle.weblogic.kubernetes.actions.impl.primitive.Kubernetes;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import static oracle.weblogic.kubernetes.extensions.LoggedTest.logger;

public class ITTestWatcher implements TestWatcher, AfterEachCallback, BeforeEachCallback, BeforeAllCallback, AfterAllCallback {

  @Override
  public void beforeAll(ExtensionContext context) {
    logger.info("beforeAll");
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    logger.info("beforeEach");
  }

  @Override
  public void afterEach(ExtensionContext context) {
    logger.info("afterEach");
  }

  @Override
  public void afterAll(ExtensionContext context) {
    logger.info("afterAll");
  }

  @Override
  public void testAborted(ExtensionContext extensionContext, Throwable throwable) {
    logger.info("testAborted");
  }

  @Override
  public void testDisabled(ExtensionContext extensionContext, Optional<String> optional) {
    logger.info("testDisabled");
  }

  @Override
  public void testFailed(ExtensionContext extensionContext, Throwable throwable) {
    logger.info("testFailed");
  }

  // @Override
  public void testFailed1(ExtensionContext extensionContext, Throwable throwable) {
    logger.info("testFailed");
    logger.info("Test Class :" + extensionContext.getTestClass().get().getName());
    logger.info("Test Method :" + extensionContext.getTestMethod().get().getName());

    try {
      String namespace = "my-ns";
      // get service accounts
      V1ServiceAccountList listServiceAccounts = Kubernetes.listServiceAccounts(namespace);
      // get namespaces
      V1NamespaceList listNamespaces = Kubernetes.listNamespacesAsObjects();
      // get pv
      V1PersistentVolumeList listPersistenVolumes = Kubernetes.listPersistenVolumes();
      // get pvc
      V1PersistentVolumeClaimList listPersistenVolumeClaims = Kubernetes.listPersistenVolumeClaims();
      // get secrets
      V1SecretList listSecrets = Kubernetes.listSecrets(namespace);
      // get configmaps
      V1ConfigMapList listConfigMaps = Kubernetes.listConfigMaps(namespace);
      // get jobs
      V1JobList listJobs = Kubernetes.listJobs(namespace);
      // get deployments
      V1DeploymentList listDeployments = Kubernetes.listDeployments(namespace);
      // get replicasets
      V1ReplicaSetList listReplicaSets = Kubernetes.listReplicaSets(namespace);
      // get Domain CRD
      // get Domain
      // get pods
      // get logs
    } catch (ApiException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void testSuccessful(ExtensionContext extensionContext) {
    logger.info("testSuccessful");
  }
}
