package cc.notsoclever.tools;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VersionsUtils {
    static Logger LOG = Logger.getLogger(VersionsUtils.class.getName());

    public static List<String> extractVersions(String source, String name) {
        List<String> versionTags = new ArrayList<String>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Branch> allTags = mapper.readValue(source, mapper.getTypeFactory().constructCollectionType(
                  List.class, Branch.class));
            final String cname = name + "-";
            final String cname2 = name + ".";
            for (Branch b : allTags) {
                versionTags.add(b.name);//.substring(name.length() + 1));
            }
        } catch (IOException e) {
            LOG.error("Error converting JSON collection to List<MyType>.", e);
        }
        return versionTags;
    }

    public static List<String> extractVersionso(String source, String name) {
        List<String> versionTags = new ArrayList<String>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> allTags = mapper.readValue(source, new TypeReference<List<String>>() {
            });
            final String cname = name + "-";
            final String cname2 = name + ".";
            for (String v : allTags) {
                if (v.startsWith(cname) || v.startsWith(cname2)) {
                    versionTags.add(v.substring(name.length() + 1));
                }
            }
        } catch (IOException e) {
            LOG.error("Error converting JSON collection to List<MyType>.", e);
        }
        return versionTags;
    }
}

class Branch {
    String name;
    String zipball_url;
    String tarball_url;
    Commit commit;

    public static class Commit {
        private String sha, url;

        public String getSha() {
            return sha;
        }

        public void setSha(String sha) {
            this.sha = sha;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZipball_url() {
        return zipball_url;
    }

    public void setZipball_url(String zipball_url) {
        this.zipball_url = zipball_url;
    }

    public String getTarball_url() {
        return tarball_url;
    }

    public void setTarball_url(String tarball_url) {
        this.tarball_url = tarball_url;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }
}
