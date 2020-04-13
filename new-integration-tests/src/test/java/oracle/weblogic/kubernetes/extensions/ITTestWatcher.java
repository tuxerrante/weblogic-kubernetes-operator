// Copyright (c) 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.weblogic.kubernetes.extensions;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.nio.charset.StandardCharsets;

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
import oracle.weblogic.domain.DomainList;
import oracle.weblogic.kubernetes.actions.impl.primitive.Kubernetes;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestWatcher;

import static io.kubernetes.client.util.Yaml.dump;
import static oracle.weblogic.kubernetes.extensions.LoggedTest.logger;

public class ITTestWatcher implements
    TestWatcher,
    BeforeAllCallback,
    AfterAllCallback,
    AfterEachCallback,
    AfterTestExecutionCallback,
    InvocationInterceptor,
    TestExecutionExceptionHandler {

  private Namespace methodNamespace;
  private Store globalStore;
  private Store testStore;
  private String className;
  private String methodName;

  @Override
  public void beforeAll(ExtensionContext context) {
    logger.info("beforeAll");
    className = context.getTestClass().get().getName();
    globalStore = context.getStore(Namespace.GLOBAL);
    globalStore.put("BEFOREALL", Boolean.FALSE);
    logger.info(getHeader("Starting Test Suite   ", className, "+"));
    logger.info(getHeader("Starting beforeAll for ", className, "-"));
  }

  @Override
  public void interceptBeforeEachMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext context) throws Throwable {
    logger.info("interceptBeforeEachMethod");
    methodName = context.getRequiredTestMethod().getName();
    methodNamespace = Namespace.create(methodName);
    testStore = context.getStore(methodNamespace);

    // if execution reaches here the beforeAll is succeeded.
    globalStore.put("BEFOREALL", Boolean.TRUE);
    // assume beforeEach is failed and set it to true if execution reaches test
    testStore.put("BEFOREEACH", Boolean.FALSE);
    logger.info(getHeader("Starting beforeEach for ", className + "." + methodName, "-"));
    invocation.proceed();
  }

  @Override
  public void interceptTestMethod​(Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext context) throws Throwable {
    logger.info("interceptTestMethod​");
    logger.info(getHeader("Ending beforeEach for ", className + "." + methodName, "-"));
    // if execution reaches here the beforeEach is succeeded.
    testStore.put("BEFOREEACH", Boolean.TRUE);
    // assume the test is passed and set it to false in handleTestExecutionException​ if it fails
    testStore.put("TEST", Boolean.TRUE);
    logger.info(getHeader("Starting Test   ", className + "." + methodName, "-"));
    invocation.proceed();
  }

  @Override
  public void handleTestExecutionException​(ExtensionContext context, Throwable throwable) throws Throwable {
    logger.info("handleTestExecutionException​");
    testStore.put("TEST", Boolean.FALSE);
    logger.info(getHeader("Test failed   ", className + "." + methodName, "!"));
    logger.info("Collect logs...");
    collectLogs(context, "test");
    throw throwable;
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    logger.info(getHeader("Ending Test   ", className + "." + methodName, "-"));
  }

  @Override
  public void interceptAfterEachMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext context) throws Throwable {
    logger.info("interceptAfterEachMethod");
    // if BEFOREEACH is false then beforeEach failed, test was not run
    if (!(Boolean) testStore.get("BEFOREEACH")) {
      logger.info("beforeEach failed for test " + className + "." + methodName);
      logger.info("skipped test " + className + "." + methodName);
      logger.info("Collecting logs...");
      collectLogs(context, "beforeEach");
    }
    logger.info(getHeader("Starting afterEach for ", className + "." + methodName, "-"));
    invocation.proceed();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    logger.info(getHeader("Ending afterEach for ", className + "." + methodName, "-"));
  }

  @Override
  public void interceptAfterAllMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext context) throws Throwable {
    logger.info("interceptAfterAllMethod");
    logger.info(getHeader("Starting afterAll for  ", className, "-"));
    if (!(Boolean) globalStore.get("BEFOREALL")) {
      logger.info("beforeAll failed for class " + className);
      logger.info("Collecting logs...");
      collectLogs(context, "beforeAll");
    }
    invocation.proceed();
  }

  @Override
  public void afterAll(ExtensionContext context) {
    logger.info("afterAll");
    if (context.getExecutionException().isPresent()) {
      logger.info("Exception thrown, collecting logs...");
      collectLogs(context, "afterAll");
    }
    logger.info(getHeader("Ending Test Suite  ", className, "+"));
  }

  @Override
  public void testSuccessful(ExtensionContext extensionContext) {
    logger.info("testSuccessful");
    logger.info(getHeader("Test passed  ", className + "." + methodName, "-"));
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable throwable) {
    logger.info("testFailed");
    // if both beforeEach and Test is passed then execution must have failed in afterEach
    if ((Boolean) testStore.get("BEFOREEACH") && (Boolean) testStore.get("TEST")) {
      logger.info("afterEach failed for test " + methodName);
      logger.info("Collecting logs...");
      collectLogs(context, "afterEach");
    }
    logger.info(getHeader("Test failed  ", className + "." + methodName, "-"));
  }

  public void collectLogs(ExtensionContext extensionContext, String failedStage) {
    String[] namespaceFields = {"domainns", "opns"};
    final String DIAG_LOGS_DIR = System.getProperty("java.io.tmpdir");
    String itClassName = extensionContext.getRequiredTestClass().getSimpleName();
    String testName = methodName;
    Object testInstance = extensionContext.getRequiredTestInstance();

    Path path = null;
    for (String namespace : namespaceFields) {
      try {
        switch (failedStage) {
          case "beforeAll":
          case "afterAll":
            logger.info("beforeAll");
            path = Paths.get(DIAG_LOGS_DIR, itClassName, failedStage);
            break;
          case "beforeEach":
          case "afterEach":
            logger.info("beforeEach");
            path = Paths.get(DIAG_LOGS_DIR, itClassName, testName + "_" + failedStage);
            break;
          case "test":
            logger.info("test");
            path = Paths.get(DIAG_LOGS_DIR, itClassName, testName);
            break;
          default:
            logger.info("");
        }
        logger.info("creating directory " + path);
        Files.createDirectories(path);
        generateLog((String) testInstance.getClass().getField(namespace).get(testInstance), path);
      } catch (NoSuchFieldException
          | SecurityException
          | IllegalArgumentException
          | IllegalAccessException
          | IOException ex) {
        Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public void generateLog(String namespace, Path path) {
    try {
      // get service accounts
      V1ServiceAccountList listServiceAccounts = Kubernetes.listServiceAccounts(namespace);
      Files.write(
          Paths.get(path.toString(), namespace + "_sa.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get namespaces
      V1NamespaceList listNamespaces = Kubernetes.listNamespacesAsObjects();
      Files.write(
          Paths.get(path.toString(), namespace + "_ns.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get pv
      V1PersistentVolumeList listPersistenVolumes = Kubernetes.listPersistenVolumes();
      Files.write(
          Paths.get(path.toString(), namespace + "_pv.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get pvc
      V1PersistentVolumeClaimList listPersistenVolumeClaims = Kubernetes.listPersistenVolumeClaims();
      Files.write(
          Paths.get(path.toString(), namespace + "_pvc.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get secrets
      V1SecretList listSecrets = Kubernetes.listSecrets(namespace);
      Files.write(
          Paths.get(path.toString(), namespace + "_secrets.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get configmaps
      V1ConfigMapList listConfigMaps = Kubernetes.listConfigMaps(namespace);
      Files.write(
          Paths.get(path.toString(), namespace + "_cm.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get jobs
      V1JobList listJobs = Kubernetes.listJobs(namespace);
      Files.write(
          Paths.get(path.toString(), namespace + "_jobs.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get deployments
      V1DeploymentList listDeployments = Kubernetes.listDeployments(namespace);
      Files.write(
          Paths.get(path.toString(), namespace + "_deploy.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get replicasets
      V1ReplicaSetList listReplicaSets = Kubernetes.listReplicaSets(namespace);
      Files.write(
          Paths.get(path.toString(), namespace + "_rs.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get Domain
      DomainList listDomains = Kubernetes.listDomains(namespace);
      Files.write(
          Paths.get(path.toString(), namespace + "_domain.log"),
          dump(listServiceAccounts).getBytes(StandardCharsets.UTF_8)
      );
      // get domain pods
      V1PodList listPods = Kubernetes.listPods(namespace, null);
      List<V1Pod> domainPods = listPods.getItems();
      for (V1Pod pod : domainPods) {
        String podName = pod.getMetadata().getName();
        String podLog = Kubernetes.getPodLog(podName, namespace);
        Files.write(
          Paths.get(path.toString(), namespace + podName + ".log"),
          dump(podLog).getBytes(StandardCharsets.UTF_8)
      );
      }
    } catch (ApiException | IOException ex) {
      Logger.getLogger(ITTestWatcher.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private String getHeader(String header, String name, String rc) {
    String line = header + "   " + name;
    return "\n" + rc.repeat(line.length()) + "\n" + line + "\n" + rc.repeat(line.length()) + "\n";
  }
}
