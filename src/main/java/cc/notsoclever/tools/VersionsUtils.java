package cc.notsoclever.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class VersionsUtils {
  static Logger LOG = Logger.getLogger(VersionsUtils.class.getName());
  
  public static List<String> extractVersions(String source, String name) {
    List<String> versionTags = new ArrayList<String>();
    try {
      ObjectMapper mapper = new ObjectMapper();
      List<String> allTags = mapper.readValue(source, new TypeReference<List<String>>() {});
      final String cname = name + "-";
      for (String v : allTags) {
        if (v.startsWith(cname)) {
          versionTags.add(v.substring(name.length() + 1));
        }
      }
    } catch (IOException e) {
      LOG.error("Error converting JSON collection to List<MyType>.", e);
    }
    return versionTags;
  }
}
