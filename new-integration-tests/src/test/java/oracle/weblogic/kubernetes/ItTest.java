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

import static org.junit.jupiter.api.Assertions.fail;



@DisplayName("Simple validation of junit5 functions")
@IntegrationTest
@ExtendWith(ITTestWatcher.class)
public class ItTest implements LoggedTest {

  public String domnamespace;
  public String opnamespace;

  @BeforeAll
  public void beforeAll() {
    logger.info("Running beforeAll");
    //fail("Failing test");
  }

  @BeforeEach
  public void beforeEach() {
    logger.info("Running beforeEach");
    //fail("Failing test");
  }

  @AfterEach
  public void afterEach() {
    logger.info("Running afterEach");
    //fail("Failing test");
  }

  @AfterAll
  public void afterAll() {
    logger.info("Running afterAll");
    //fail("Failing test");
  }

  @Test
  public void test1() {
    domnamespace = "dom-test1";
    opnamespace = "op-test1";
    logger.info("Running test1");
    //assertNotNull(null);
    fail("Failling test1");
  }

  @Test
  @DisplayName("Sample JUnit5 test fail")
  public void test2() {
    domnamespace = "dom-test2";
    opnamespace = "op-test2";
    logger.info("Running test2");
    //fail("Failing test");
  }

}
