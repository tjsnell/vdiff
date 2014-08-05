package cc.notsoclever.tools;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@JsonIgnoreProperties({"versions1", "versions2"})
public class Versions {
    static Logger LOG = Logger.getLogger(Versions.class.getName());

    public static final String POM_URL = "https://raw.github.com/{0}/{1}/{1}-{2}/parent/pom.xml";
    public static final String POM2_URL = "https://raw.github.com/{0}/{1}/{1}-{2}/pom.xml";
    public static final String POM_TRUNK_URL = "https://raw.github.com/{0}/{1}/{2}/parent/pom.xml";
    public static final String POM2_TRUNK_URL = "https://raw.github.com/{0}/{1}/{2}/pom.xml";
    public static final String TAGS_URL = "https://api.github.com/repos/{0}/{1}/tags";

    private static final Pattern VERSION_PROP_REGEX = Pattern.compile("^(.*).version$");
    private static final Pattern VERSION_VALUE_REGEX = Pattern.compile("^[\\(\\[]?\\d[0-9A-Za-z\\-\\.\\,\\s]*[\\)\\]]?$");
    private static final String DEFAULT_ORG = "apache";
    private static final String DEFAULT_NAME = "camel";

    private String org;
    private String name;

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

    public Versions() {
        this(DEFAULT_ORG, DEFAULT_NAME);
    }

    public Versions(String name) {
        this(DEFAULT_ORG, name);
    }

    public Versions(String org, String name) {
        this.org = org;
        this.name = name;
    }

    public void compare(String versions1Tag, String versions2Tag) throws Exception {
        LOG.info("Comparing version " + versions1Tag + " and " + versions2Tag);
        unChanged = new HashMap<String, String>();
        changed = new ArrayList<List<String>>();

        this.v1Tag = versions1Tag.startsWith(name) ? versions1Tag.substring(name.length() + 1) : versions1Tag;
        this.v2Tag = versions2Tag.startsWith(name) ? versions2Tag.substring(name.length() + 1) : versions2Tag;

        versions1 = loadVersions(v1Tag);
        versions2 = loadVersions(v2Tag);
        diffVersions();

        Collections.sort(changed, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                ArrayList<String> a1 = (ArrayList<String>) o1;
                ArrayList<String> a2 = (ArrayList<String>) o2;
                return (a1.get(0)).compareToIgnoreCase(a2.get(0));
            }
        });
    }

    public String getTags() throws Exception {
        LOG.info("Get TAGS");

        URL url = new URL(MessageFormat.format(TAGS_URL, new Object[]{org, name}));
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
                    sb.append(line).append("\n");
                }
                br.close();
                return sb.toString();
        }
        return null;
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


    private Map<String, String> loadVersions(String version) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        URL url = null;
        InputStream in = null;
        try {
            if (version.equals("trunk")) {
                url = new URL(MessageFormat.format(POM_TRUNK_URL, new Object[]{org, name, version}));
            } else {
                url = new URL(MessageFormat.format(POM_URL, new Object[]{org, name, version}));
            }
            in = url.openConnection().getInputStream();
        } catch (IOException e) {
            LOG.error("Unable to read from " + url);
            try {
                if (version.equals("trunk")) {
                    url = new URL(MessageFormat.format(POM2_TRUNK_URL, new Object[]{org, name, version}));
                } else {
                    url = new URL(MessageFormat.format(POM2_URL, new Object[]{org, name, version}));
                }
                in = url.openConnection().getInputStream();
            } catch (IOException e2) {
                LOG.error("Unable to read from " + url);
                throw e2;
            }
        }

        Document doc = builder.parse(in);

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//properties/*");

        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        Map<String, String> versions = new HashMap<String, String>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            Matcher mp = VERSION_PROP_REGEX.matcher(nodeName);
            if (mp.find()) {
                String nodeValue = node.getChildNodes().item(0).getNodeValue();
                Matcher mv = VERSION_VALUE_REGEX.matcher(nodeValue);
                if (mv.find()) {
                    versions.put(mp.group(1), nodeValue);
                }
            }
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
