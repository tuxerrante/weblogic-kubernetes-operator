// Copyright (c) 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.weblogic.kubernetes;

import java.util.ArrayList;

import oracle.weblogic.kubernetes.actions.TestActions;
import oracle.weblogic.kubernetes.extensions.LoggedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static oracle.weblogic.kubernetes.actions.TestActions.withWITParams;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Simple validation of basic WIT functions")
class ItWITValidation implements LoggedTest {
  private static final String TEST_MODEL_DIR =
      System.getProperty("user.dir") + "/src/test/resources/models/";
  
  private static final String WDT_MODEL_FILE = "model1-wls.yaml";
  private static final String WDT_PROPERIES_FILE = "model1-10.properties"
		  
  @Test
  @DisplayName("Create a MII image")
  public void testCreatingMIIImage() {

    // install WIT using the default version and location
    boolean downloadWIT = TestActions.installWIT(
        null /* version */, 
        null /* location */,
        false /* do not redirect output */);
   
    assertEquals(true, downloadWIT, "Failed to download or unzip WebLogic Image Tool");
    
    // install WDT using the default version and location
    boolean downloadWDT = TestActions.installWDT(
        null /* version */, 
        null /* location */,
        false /* do not redirect output */);

    assertEquals(true, downloadWDT, "Failed to download WebLogic Deploy Tool");

    // create the MII image
    // TODO add model files and other contents to the image once we have those resources

    logger.info("Model dir = " + TEST_MODEL_DIR);
    ArrayList<String> modelVariableList = new ArrayList();
    modelVariableList.add(TEST_MODEL_DIR + WDT_PROPERIES_FILE);
     
    ArrayList<String> modelList = new ArrayList();
    modelList.add(TEST_MODEL_DIR + WDT_MODEL_FILE);

    boolean success = TestActions.createMIIImage(
        withWITParams()
            .modelFiles(modelList)
            .modelVariableFiles(modelVariableList));

    assertEquals(true, success, "Failed to create the image using WebLogic Deploy Tool");
  } 
}

