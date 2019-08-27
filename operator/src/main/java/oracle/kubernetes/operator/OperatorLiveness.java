// Copyright 2017, 2019, Oracle Corporation and/or its affiliates.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at
// http://oss.oracle.com/licenses/upl.

package oracle.kubernetes.operator;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import oracle.kubernetes.operator.logging.LoggingFacade;
import oracle.kubernetes.operator.logging.LoggingFactory;
import oracle.kubernetes.operator.logging.MessageKeys;

/**
 * This task maintains the "liveness" indicator so that Kubernetes knows the Operator is still
 * alive.
 */
public class OperatorLiveness implements Runnable {

  private static final LoggingFacade LOGGER = LoggingFactory.getLogger("Operator", "Operator");
  private static final File livenessFile = new File("/operator/.alive");
  private static final File restartRequiredFile = new File("/operator/.restart");
  private static final File shutdownFile = new File("/operator/.shutdown");
  private static final File generateHeapFile = new File("/operator/.generateHeap");

  public static void causeRestart() {
    try {
      restartRequiredFile.createNewFile();
    } catch (IOException io) {
      LOGGER.severe(MessageKeys.EXCEPTION, io);
    }
  }

  public static void causeShutdown() {
    causeShutdown(false);
  }

  public static void causeShutdown(boolean isWithHeapDump) {
    try {
      if (isWithHeapDump) {
        generateHeapFile.createNewFile();
      }
      shutdownFile.createNewFile();
    } catch (IOException io) {
      LOGGER.severe(MessageKeys.EXCEPTION, io);
    }
  }

  @Override
  public void run() {
    // If the restart required file exists then stop updating the liveness marker.  Soon, k8s will restart
    // the operator's pod.
    if (!restartRequiredFile.exists()) {
      if (!livenessFile.exists()) {
        try {
          livenessFile.createNewFile();
        } catch (IOException ioe) {
          LOGGER.info(MessageKeys.COULD_NOT_CREATE_LIVENESS_FILE);
        }
      }
      livenessFile.setLastModified(new Date().getTime());
    }
  }
}
