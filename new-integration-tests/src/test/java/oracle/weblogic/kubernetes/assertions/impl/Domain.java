// Copyright 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.weblogic.kubernetes.assertions.impl;

import java.util.Random;
import java.util.concurrent.Callable;

import static oracle.weblogic.kubernetes.extensions.LoggedTest.logger;

import java.util.ArrayList;
import java.util.List;

public class Domain {

  public static Callable<Boolean> exists(String domainUID, String namespace) {
    List<String> list = new ArrayList<>();
    // add admin server in pod list
    list.add(domainUID + "-" + adminServerName, namespace);
    if (!serverStartPolicy.equals("ADMIN_ONLY")) {
      for (int i = 1; i <= initialManagedServerReplicas; i++) {
        list.add(domainUID + "-" + managedServerNameBase + i, namespace);
      }
    }
    return podExists(namespace, domainUID, namespace)
    return () -> {
      if (!podExists(podName, domainUID, namespace)) {
        return false;
      }
      if (!podReady(domainUID, namespace)) {
        return false;
      }
      if (!serviceCreated(domainUID, namespace)) {
        return false;
      }
      return true;
    };
  }

  public static Callable<Boolean> podExists(String podName, String domainUID, String namespace) {

    return () -> {
      StringBuffer cmd = new StringBuffer();
      cmd.append("kubectl get pod ").append(podName).append(" -n ").append(namespace);
      // check for pod to be running
      checkCmdInLoop(cmd.toString(), "Running", podName);
      int outcome = new Random(System.currentTimeMillis()).nextInt(3);
      if (outcome == 1) {
        logger.info(String.format("Domain %s exists in namespace %s", domainUID, namespace));
        return true;
      } else {
        logger.info(String.format("Domain %s does not exist in namespace %s", domainUID, namespace));
        return false;
      }
    };
  }

  public static Callable<Boolean> podReady(String domainUID, String namespace) {
    return () -> {
      int outcome = new Random(System.currentTimeMillis()).nextInt(3);
      if (outcome == 1) {
        logger.info(String.format("Domain %s exists in namespace %s", domainUID, namespace));
        return true;
      } else {
        logger.info(String.format("Domain %s does not exist in namespace %s", domainUID, namespace));
        return false;
      }
    };
  }

  public static Callable<Boolean> serviceCreated(String domainUID, String namespace) {
    return () -> {
      int outcome = new Random(System.currentTimeMillis()).nextInt(3);
      if (outcome == 1) {
        logger.info(String.format("Domain %s exists in namespace %s", domainUID, namespace));
        return true;
      } else {
        logger.info(String.format("Domain %s does not exist in namespace %s", domainUID, namespace));
        return false;
      }
    };
  }

}
