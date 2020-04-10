// Copyright (c) 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.weblogic.kubernetes;

import oracle.weblogic.kubernetes.annotations.IntegrationTest;
import oracle.weblogic.kubernetes.extensions.ITTestWatcher;
import oracle.weblogic.kubernetes.extensions.LoggedTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("Simple validation of junit5 functions")
@IntegrationTest
@ExtendWith(ITTestWatcher.class)
class ItTest implements LoggedTest {

  @BeforeAll
  public void beforeAll() {
    logger.info("beforeAll");
  }

  @BeforeEach
  public void beforeEach() {
    logger.info("beforeEach");
  }

  @AfterEach
  public void afterEach() {
    logger.info("afterEach");
  }

  @AfterAll
  public void afterAll() {
    logger.info("afterAll");
  }

  @Test
  @DisplayName("Sample JUnit5 test pass ")
  public void testPass() {
    String name = "name";
    String namespace = "namespace";
    Object obj = new Object();

    logger.info(name);

  }

  @Test
  @DisplayName("Sample JUnit5 test fail")
  public void testFail() {
    String name = "name";
    String namespace = "namespace";
    Object obj = new Object();

    logger.info(name);

  }

}
