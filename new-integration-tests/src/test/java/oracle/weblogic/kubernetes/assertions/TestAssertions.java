// Copyright 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.weblogic.kubernetes.assertions;

import oracle.weblogic.kubernetes.assertions.impl.Domain;
import oracle.weblogic.kubernetes.assertions.impl.Operator;

import java.util.concurrent.Callable;
import oracle.weblogic.kubernetes.assertions.impl.Kubernetes;

// as in the actions, it is intended tests only use these assertaions and do
// not go direct to the impl classes
public class TestAssertions {

  public static Callable<Boolean> operatorIsRunning(String namespace) {
    return Operator.isRunning(namespace);
  }

  public static boolean operatorRestServiceRunning(String namespace) {
    return Operator.isRestServiceCreated(namespace);
  }

  public static Callable<Boolean> domainExists(String domainUID, String namespace) {
    return Domain.exists(domainUID, namespace);
  }

  public static Callable<Boolean> podReady(String podName, String domainUID, String namespace) {
    return Kubernetes.podRunning(podName, domainUID, namespace);
  }

  public static Callable<Boolean> podTerminating(String podName, String domainUID, String namespace) {
    return Kubernetes.podTerminating(podName, domainUID, namespace);
  }

  public static boolean serviceReady(String serviceName, String namespace) {
    return Kubernetes.serviceCreated(serviceName, namespace);
  }

  public static boolean loadbalancerReady(String domainUID) {
    return Kubernetes.loadBalancerReady(domainUID);
  }

  public static boolean adminServerReady(String domainUID, String namespace) {
    return Kubernetes.adminServerReady(domainUID, namespace);
  }

  public static boolean adminT3ChannelAccessible(String domainUID, String namespace) {
    return Domain.adminT3ChannelAccessible(domainUID, namespace);
  }

  public static boolean adminNodePortAccessible(String domainUID, String namespace) {
    return Domain.adminNodePortAccessible(domainUID, namespace);
  }

}
