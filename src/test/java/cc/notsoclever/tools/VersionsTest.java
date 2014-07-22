package cc.notsoclever.tools;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class VersionsTest extends Assert {
  static Logger LOG = Logger.getLogger(VersionsTest.class.getName());
  private static boolean testable;
  
  @BeforeClass
  public static void checkConnection() {
    InputStream in = null;
    try {
      URL testTarget = new URL("https://api.github.com/repos/apache/camel/tags");
      in = testTarget.openStream();
      testable = true;
    } catch (Exception e) {
      // not connection
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }
  
  @Test
  public void testCamelTags() throws Exception {
    if (!testable) {
      LOG.error("No Internet connection available; Skip testing");
      return;
    }
        Versions versions = new Versions("camel");

        List<String> versionTags = VersionsUtils.extractVersions(versions.getTags(), "camel");
        for (String v : versionTags) {
          LOG.info("finding tag: " + v);  
        }
        // check some random valid version
        assertTrue(versionTags.contains("2.12.1"));
        assertTrue(versionTags.contains("2.12.2"));
        
  }
  
  @Test
  public void testCXFTags() throws Exception {
    if (!testable) {
      LOG.error("No Internet connection available; Skip testing");
      return;
    }
        Versions versions = new Versions("cxf");

        List<String> versionTags = VersionsUtils.extractVersions(versions.getTags(), "cxf");
        for (String v : versionTags) {
          LOG.info("finding tag: " + v);  
        }
        assertNotNull(versionTags);
        // check some random valid version
        assertTrue(versionTags.contains("2.7.8"));
  }
  
  @Test
  public void compareCamel() throws Exception {
    if (!testable) {
      LOG.error("No Internet connection available; Skip testing");
      return;
    }
    Versions versions = new Versions("camel");
    
    versions.compare("2.12.1", "2.12.2");
    logComparisonResult(versions);
    
    Map<String, String> added = versions.getAdded();
    assertNotNull(added);
    assertEquals(4, added.size());
    assertEquals(4, versions.getTotalAdded());

    Map<String, String> dropped = versions.getDropped();
    assertNotNull(dropped);
    assertEquals(0, dropped.size());
    assertEquals(0, versions.getTotalDropped());

    List<List<String>> changed = versions.getChanged();
    assertNotNull(changed);
    assertEquals(27, changed.size());
    assertEquals(27, versions.getTotalChanged());
    
    Map<String, String> unchanged = versions.getUnChanged();
    assertNotNull(unchanged);
    assertEquals(249, unchanged.size());
    assertEquals(249, versions.getTotal() - 4 - 27);
  }

  @Test
  public void compareCXF() throws Exception {
    if (!testable) {
      LOG.error("No Internet connection available; Skip testing");
      return;
    }
    Versions versions = new Versions("cxf");
    
    versions.compare("2.7.7", "2.7.8");
    logComparisonResult(versions);

    Map<String, String> added = versions.getAdded();
    assertNotNull(added);
    assertEquals(1, added.size());
    assertEquals(1, versions.getTotalAdded());

    Map<String, String> dropped = versions.getDropped();
    assertNotNull(dropped);
    assertEquals(0, dropped.size());
    assertEquals(0, versions.getTotalDropped());

    List<List<String>> changed = versions.getChanged();
    assertNotNull(changed);
    assertEquals(8, changed.size());
    assertEquals(8, versions.getTotalChanged());
    
    Map<String, String> unchanged = versions.getUnChanged();
    assertNotNull(unchanged);
    assertEquals(76, unchanged.size());
    assertEquals(76, versions.getTotal() - 1 - 8);
  }
  
  @Test
  public void compareAtmosphere() throws Exception {
    if (!testable) {
      LOG.error("No Internet connection available; Skip testing");
      return;
    }
    Versions versions = new Versions("Atmosphere", "atmosphere");
    
    versions.compare("project-2.1.7", "project-2.2.0");
    logComparisonResult(versions);

    Map<String, String> added = versions.getAdded();
    assertNotNull(added);
    assertEquals(0, added.size());
    assertEquals(0, versions.getTotalAdded());

    Map<String, String> dropped = versions.getDropped();
    assertNotNull(dropped);
    assertEquals(0, dropped.size());
    assertEquals(0, versions.getTotalDropped());

    List<List<String>> changed = versions.getChanged();
    assertNotNull(changed);
    assertEquals(3, changed.size());
    assertEquals(3, versions.getTotalChanged());
    
    Map<String, String> unchanged = versions.getUnChanged();
    assertNotNull(unchanged);
    assertEquals(32, unchanged.size());
    assertEquals(32, versions.getTotal() - 3);
  }
  
  @Test
  public void testAtmosphereTags() throws Exception {
    if (!testable) {
      LOG.error("No Internet connection available; Skip testing");
      return;
    }
        Versions versions = new Versions("Atmosphere", "atmosphere");

        List<String> versionTags = VersionsUtils.extractVersions(versions.getTags(), "atmosphere");
        for (String v : versionTags) {
          LOG.info("finding tag: " + v);  
        }
        // check some random valid version
        assertTrue(versionTags.contains("project-2.1.7"));
        assertTrue(versionTags.contains("project-2.2.0"));
  }
  
  private static void logComparisonResult(Versions versions) {
    LOG.info("### total added: " + versions.getTotalAdded());
    for (Map.Entry<String, String> c : versions.getAdded().entrySet()) {
      LOG.info(buildPairString("added", c));
    }
    LOG.info("### total dropped: " + versions.getTotalDropped());
    for (Map.Entry<String, String> c : versions.getDropped().entrySet()) {
      LOG.info(buildPairString("dropped", c));
    }
    LOG.info("### total changed: " + versions.getTotalChanged());
    for (List<String> c : versions.getChanged()) {
      LOG.info(buildChangedString("changed", c));
    }
    LOG.info("### total unchanged: " 
        + (versions.getTotal() - versions.getTotalChanged() - versions.getTotalAdded() - versions.getTotalDropped()));
    for (Map.Entry<String, String> c : versions.getUnChanged().entrySet()) {
      LOG.info(buildPairString("unchanged", c));
    }
    
  }
  private static String buildChangedString(String title, List<String> ss) {
    StringBuilder sb = new StringBuilder();
    sb.append(title).append(" ")
        .append(ss.get(0)).append(" ").append(ss.get(1)).append(" -> ").append(ss.get(2));
    return sb.toString();
  }
  
  private static String buildPairString(String title, Map.Entry<String, String> sp) {
    StringBuilder sb = new StringBuilder();
    sb.append(title).append(" ")
        .append(sp.getKey()).append(" ").append(sp.getValue());
    return sb.toString();
  }
  
  
}
