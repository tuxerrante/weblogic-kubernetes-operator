// Copyright (c) 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.kubernetes.operator;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import oracle.kubernetes.operator.utils.Domain;
import oracle.kubernetes.operator.utils.DomainCrd;
import oracle.kubernetes.operator.utils.ExecResult;
import oracle.kubernetes.operator.utils.LoggerHelper;
import oracle.kubernetes.operator.utils.TestUtils;

public class MiiBaseTest extends BaseTest {
  /**
   * Creates a map with customized domain input attributes using suffixCount and prefix
   * to make the namespaces and ports unique for model in image
   *
   * @param suffixCount unique numeric value
   * @param prefix      prefix for the artifact names
   * @return map with domain input attributes
   */
  public Map<String, Object> createModelInImageMap(
      int suffixCount, String prefix) {
    Map<String, Object> domainMap = createDomainMap(suffixCount, prefix);
    domainMap.put("domainHomeSourceType", "FromModel");
    domainMap.put("domainHomeImageBase",
        getWeblogicImageName() + ":" + getWeblogicImageTag());
    domainMap.put("logHomeOnPV", "true");
    //domainMap.put("wdtDomainType", "WLS");

    if (prefix != null && !prefix.trim().equals("")) {
      domainMap.put("image", prefix.toLowerCase() + "-modelinimage-" + suffixCount + ":latest");
    } else {
      domainMap.put("image", "modelinimage-" + suffixCount + ":latest");
    }
    return domainMap;
  }

  /**
   * Create domain using model in image.
   * @param domainUIDPrefix
   * @param domainNS
   * @param wdtModelFile - file should be under test/resouces/model-in-image dir,
   *                     value can be ./model.wls.yaml
   * @param wdtModelPropertiesFile - file should be under test/resouces/model-in-image dir,
   *                               value can be ./model.empty.properties
   * @param cmFile - creates configmap from this file or dir
   */
  public Domain createMIIDomainWithConfigMap(String domainUIDPrefix,
        String domainNS, String wdtModelFile, String wdtModelPropertiesFile,
        String cmFile, String wdtDomainType) throws Exception {
    Map<String, Object> domainMap =
        createModelInImageMap(getNewSuffixCount(), domainUIDPrefix);
    // config map before deploying domain crd
    String cmName = domainMap.get("domainUID") + "-mii-config-map";

    domainMap.put("namespace", domainNS);
    domainMap.put("wdtModelFile", wdtModelFile);
    domainMap.put("wdtModelPropertiesFile", wdtModelPropertiesFile);
    domainMap.put("wdtDomainType", wdtDomainType);

    domainMap.put("miiConfigMap", cmName);
    domainMap.put("miiConfigMapFileOrDir", cmFile);

    Domain domain = TestUtils.createDomain(domainMap);
    // domain = new Domain(domainMap, true, false);
    domain.verifyDomainCreated();
    return domain;
  }

  /**
   *
   * @param cmName
   * @param domain
   * @throws Exception
   */
  public void modifyDomainYamlWithNewConfigMapAndDomainRestartVersion(
      String cmName, Domain domain)
      throws Exception {
    String originalYaml =
        getUserProjectsDir()
            + "/weblogic-domains/"
            + domain.getDomainUid()
            + "/domain.yaml";

    // Modify the original domain yaml to include restartVersion in admin server node
    DomainCrd crd = new DomainCrd(originalYaml);
    Map<String, String> objectNode = new HashMap();
    objectNode.put("restartVersion", "v1.1");
    crd.addObjectNodeToDomain(objectNode);
    String modYaml = crd.getYamlTree();
    LoggerHelper.getLocal().log(Level.INFO, modYaml);

    //change config map name to new config map
    modYaml.replaceAll((String)domain.getDomainMap().get("miiConfigMap"), cmName);

    // Write the modified yaml to a new file
    Path path = Paths.get(getUserProjectsDir()
        + "/weblogic-domains/"
        + domain.getDomainUid(), "modified.domain.yaml");
    LoggerHelper.getLocal().log(Level.INFO, "Path of the modified domain.yaml :{0}", path.toString());
    Charset charset = StandardCharsets.UTF_8;
    Files.write(path, modYaml.getBytes(charset));

    // Apply the new yaml to update the domain crd
    LoggerHelper.getLocal().log(Level.INFO, "kubectl apply -f {0}", path.toString());
    ExecResult exec = TestUtils.exec("kubectl apply -f " + path.toString());
    LoggerHelper.getLocal().log(Level.INFO, exec.stdout());

  }

  /**
   * Modify the domain yaml to change domain-level restart version.
   * @param domainNS the domain namespace
   * @param domainUid the domain UID
   * @param versionNo version number of domain
   *
   * @throws Exception if patching domain fails
   */
  protected void modifyDomainYamlWithRestartVersion(
      Domain domain, String domainNS) throws Exception {
    String versionNo = getRestartVersion(domainNS, domain.getDomainUid());
    StringBuffer patchDomainCmd = new StringBuffer("kubectl -n ");
    patchDomainCmd
        .append(domainNS)
        .append(" patch domain ")
        .append(domain.getDomainUid())
        .append(" --type='json' ")
        .append(" -p='[{\"op\": \"replace\", \"path\": \"/spec/restartVersion\", \"value\": \"'")
        .append(versionNo)
        .append("'\" }]'");

    // patching the domain
    LoggerHelper.getLocal().log(Level.INFO, "Command to patch domain: " + patchDomainCmd);
    ExecResult result = TestUtils.exec(patchDomainCmd.toString());
    LoggerHelper.getLocal().log(Level.INFO, "Domain patch result: " + result.stdout());

    // verify the domain restarted
    domain.verifyAdminServerRestarted();
    domain.verifyManagedServersRestarted();
  }

  private String getRestartVersion(String domainNS, String domainUid) throws Exception {
    String versionNo = "1";
    StringBuffer getVersionCmd = new StringBuffer("kubectl -n ");
    getVersionCmd
        .append(domainNS)
        .append(" get domain ")
        .append(domainUid)
        .append("-o=jsonpath='{.spec.restartVersion}'");

    LoggerHelper.getLocal().log(Level.INFO, "Command to get restartVersion: " + getVersionCmd);
    try {
      ExecResult result = TestUtils.exec(getVersionCmd.toString());
      String existinVersion = result.stdout();
      LoggerHelper.getLocal().log(Level.INFO, "Existing restartVersion is: " + existinVersion);

      // check restartVersion number is digit
      if (existinVersion.matches("-?(0|[1-9]\\d*)")) {
        int number = Integer.parseInt(existinVersion);
        // if restartVersion is a digit, increase it by 1
        versionNo = String.valueOf(Integer.parseInt(versionNo) + number);
      } else {
        // if restartVersion is not a digit, append 1 to it
        versionNo = existinVersion + versionNo;
      }
    } catch (Exception ex) {
      if (ex.getMessage().contains("not found")) {
        LoggerHelper.getLocal().log(Level.INFO, "Not Version num found. Set the restartVersion the first time");
      }
    }

    LoggerHelper.getLocal().log(Level.INFO, "New restartVersion is: " + versionNo);

    return versionNo;
  }

  enum WdtDomainType {
    WLS("WLS"), JRF("JRF"), RestrictedJRF("RestrictedJRF");

    private String type;

    WdtDomainType(String type) {
      this.type = type;
    }

    public String geWdtDomainType() {
      return type;
    }
  }
}
