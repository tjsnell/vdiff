package cc.notsoclever.tools;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.lang.model.element.Name;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


@JsonIgnoreProperties({"versions1", "versions2"})
public class Versions {
   static Logger LOG = Logger.getLogger(Versions.class.getName());

   public static String POM_URL = "https://raw.github.com/apache/camel/camel-{ver}/parent/pom.xml";
   public static String TAGS_URL = "https://api.github.com/repos/apache/camel/tags";
   private int totalDropped;
   private int totalAdded;
   private int totalChanged;
   private int total;

   private String v1Tag;
   private String v2Tag;

   private List<List<String>> changed = new ArrayList<List<String>>();
   private Map<String, String> unChanged = new HashMap<String, String>();
   private Map<String, String> dropped = new HashMap<String, String>();
   private Map<String, String> added = new HashMap<String, String>();

   private Map<String, String> versions2 = new HashMap<String, String>();
   private Map<String, String> versions1 = new HashMap<String, String>();

   public void compare(String versions1Tag, String versions2Tag) throws Exception {
      LOG.info("Comparing version " + versions1Tag + " and " + versions2Tag);
      unChanged = new HashMap<String, String>();
      changed = new ArrayList<List<String>>();

      this.v1Tag = versions1Tag;
      this.v2Tag = versions2Tag;

      versions1 = loadVersions(versions1Tag);
      versions2 = loadVersions(versions2Tag);
      diffVersions();

      Collections.sort(changed, new Comparator<Object>() {
         @Override
         public int compare(Object o1, Object o2) {
            return ((String) o1).compareToIgnoreCase((String) o2);
         }
      });
   }

   private void diffVersions() {

      Set<String> versions1Keys = versions1.keySet();
      Set<String> versions2Keys = versions2.keySet();

      Set<String> versions1Dropped = new TreeSet<String>(versions1Keys);
      versions1Dropped.removeAll(versions2Keys);
      totalDropped = versions1Dropped.size();

      for (String s : versions1Dropped) {
         dropped.put(s, versions1.get(s));
      }

      Set<String> versions2Added = new TreeSet<String>(versions2Keys);
      versions2Added.removeAll(versions1Keys);
      totalAdded = versions2Added.size();

      for (String s : versions2Added) {
         added.put(s, versions2.get(s));
      }

      total = versions2.size();

      getUpdateVersions();
      totalChanged = changed.size();
   }


   private void getUpdateVersions() {
      for (String k : versions1.keySet()) {
         if (versions2.containsKey(k)) {
            String ver1 = versions1.get(k);
            String ver2 = versions2.get(k);
            if (!ver1.equals(ver2)) {
               List<String> l = new ArrayList<String>();
               l.add(k);
               l.add(ver1);
               l.add(ver2);
               changed.add(l);
            } else {
               unChanged.put(k, ver1);
            }
         }
      }
   }

   public String getTags() throws Exception {
      LOG.error("Get TAGS");
      URL url = new URL(TAGS_URL);
      HttpURLConnection c = (HttpURLConnection) url.openConnection();
      c.setRequestMethod("GET");
      c.setRequestProperty("Content-length", "0");
      c.setUseCaches(false);
      c.setAllowUserInteraction(false);
      c.connect();
      int status = c.getResponseCode();

      switch (status) {
         case 200:
         case 201:
            BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
               sb.append(line + "\n");
            }
            br.close();
            return sb.toString();
      }
      return null;
   }


   private Map<String, String> loadVersions(String version) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
      URL url = new URL(POM_URL.replace("{ver}", version));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(conn.getInputStream());

      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      XPathExpression expr = xpath.compile("//properties/*");

      Object result = expr.evaluate(doc, XPathConstants.NODESET);
      NodeList nodes = (NodeList) result;

      Map<String, String> versions = new HashMap<String, String>();

      for (int i = 0; i < nodes.getLength(); i++) {
         Node node = nodes.item(i);
         String nodeName = node.getNodeName();
         if (nodeName.startsWith("camel.osgi")) {
            break;
         }
         String nodeValue = node.getChildNodes().item(0).getNodeValue();
         versions.put(nodeName.replace("-version", ""), nodeValue);
      }
      return versions;
   }

   public int getTotalDropped() {
      return totalDropped;
   }

   public int getTotalAdded() {
      return totalAdded;
   }

   public int getTotal() {
      return total;
   }

   public List<List<String>> getChanged() {
      return changed;
   }

   public int getTotalChanged() {
      return totalChanged;
   }

   public String getV1Tag() {
      return v1Tag;
   }

   public String getV2Tag() {
      return v2Tag;
   }

   public Map<String, String> getUnChanged() {
      return unChanged;
   }

   public Map<String, String> getDropped() {
      return dropped;
   }

   public Map<String, String> getAdded() {
      return added;
   }


}
