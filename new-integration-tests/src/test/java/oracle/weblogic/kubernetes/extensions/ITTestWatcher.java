// Copyright (c) 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.weblogic.kubernetes.extensions;

import java.lang.reflect.Field;
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
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ReplicaSetList;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.openapi.models.V1ServiceAccountList;
import java.util.List;
import oracle.weblogic.domain.DomainList;
import oracle.weblogic.kubernetes.actions.impl.primitive.Kubernetes;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import static oracle.weblogic.kubernetes.extensions.LoggedTest.logger;

public class ITTestWatcher implements
    TestWatcher,
    AfterEachCallback,
    BeforeEachCallback,
    BeforeAllCallback,
    AfterAllCallback {

  @Override
  public void beforeAll(ExtensionContext context) {
    logger.info("beforeAll");
    Object get = context.getTestInstance().get();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    logger.info("beforeEach");
  }

  @Override
  public void afterEach(ExtensionContext context) {
    logger.info("afterEach");
    ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
    store.put("name", "value");
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
    String name = "";
    logger.log(Level.INFO, "testDisabled{0}{1}", new Object[]{name, name});
  }

  @Override
  public void testFailed(ExtensionContext extensionContext, Throwable throwable) {
    try {
      logger.info("testFailed");
      Object testInstance = extensionContext.getRequiredTestInstance();
      Field field = testInstance.getClass().getField("p1");
      String name = field.getName();
      Object get = field.get(testInstance);
      logger.info("Instance field name : " + name + ", Instance field value :" + get);
      ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
      Object requiredTestInstance = extensionContext.getRequiredTestInstance();
      logger.info("STORE:" + (String) store.get("name"));
      logger.info(testInstance.getClass().getSimpleName());
    } catch (NoSuchFieldException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SecurityException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  // @Override
  public void testFailed1(ExtensionContext extensionContext, Throwable throwable) {
    logger.info("testFailed");
    logger.info("Test Class :" + extensionContext.getTestClass().get().getName());
    logger.info("Test Method :" + extensionContext.getTestMethod().get().getName());

    try {
      String namespace = "domain-ns";
      String opnamespace = "operator-ns";
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
      // get Domain
      DomainList listDomains = Kubernetes.listDomains(namespace);
      // get domain pods
      V1PodList listPods = Kubernetes.listPods(namespace, null);
      List<V1Pod> domainPods = listPods.getItems();
      for (V1Pod pod : domainPods) {
        String podLog = Kubernetes.getPodLog(pod.getMetadata().getName(), namespace);
      }
      // get operator pods
      V1PodList opPodsList = Kubernetes.listPods(opnamespace, null);
      List<V1Pod> opPods = opPodsList.getItems();
      for (V1Pod item : opPods) {
        String podLog = Kubernetes.getPodLog(item.getMetadata().getName(), namespace);
      }
    } catch (ApiException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void testSuccessful(ExtensionContext extensionContext) {
    logger.info("testSuccessful");
    try {
      Object testInstance = extensionContext.getRequiredTestInstance();
      Field field = testInstance.getClass().getField("p1");
      String name = field.getName();
      Object get = field.get(testInstance);
      logger.info("Instance field name : " + name + ", Instance field value :" + get);
      ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
      Object requiredTestInstance = extensionContext.getRequiredTestInstance();
      logger.info("STORE:" + (String) store.get("name"));
      logger.info(testInstance.getClass().getSimpleName());
    } catch (NoSuchFieldException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SecurityException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
