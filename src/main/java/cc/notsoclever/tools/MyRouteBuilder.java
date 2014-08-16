package cc.notsoclever.tools;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MyRouteBuilder extends RouteBuilder {

   public void configure() {

      from("restlet:///tags/{orgName}/{projectName}?restletMethods=get")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    String on = exchange.getIn().getHeader("orgName", String.class);
                    String cn = exchange.getIn().getHeader("projectName", String.class);
                    Versions versions = new Versions(on, cn);
                    ObjectMapper mapper = new ObjectMapper();
                    String tags = versions.getTags();
                    String json = mapper.writeValueAsString(VersionsUtils.extractVersions(tags, cn));
                    exchange.getOut().setBody(json);
                    exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
                }
            });

       from("restlet:///branches/{orgName}/{projectName}?restletMethods=get")
             .process(new Processor() {
                 @Override
                 public void process(Exchange exchange) throws Exception {
                     String on = exchange.getIn().getHeader("orgName", String.class);
                     String cn = exchange.getIn().getHeader("projectName", String.class);
                     Versions versions = new Versions(on, cn);
                     ObjectMapper mapper = new ObjectMapper();
                     String tags = versions.getBranches();
                     String json = mapper.writeValueAsString(VersionsUtils.extractVersions(tags, cn));
                     exchange.getOut().setBody(json);
                     exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
                 }
             });


      from("restlet:///compare/{orgName}/{projectName}/{v1}/{v2}?restletMethods=get")
            .process(new Processor() {
               @Override
               public void process(Exchange exchange) throws Exception {
                  String on = exchange.getIn().getHeader("orgName", String.class);
                  String cn = exchange.getIn().getHeader("projectName", String.class);
                  String v1 = exchange.getIn().getHeader("v1", String.class);
                  String v2 = exchange.getIn().getHeader("v2", String.class);

                  Versions versions = new Versions(on, cn);
                  versions.compare(v1, v2);

                  ObjectMapper mapper = new ObjectMapper();

                  exchange.getOut().setBody(mapper.writeValueAsString(versions));
                  exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
               }
            });
   }

    private String load(Message in) throws IOException {
        String org = in.getHeader("orgName", String.class);
        String proj = in.getHeader("projectName", String.class);

        String s = "";
        File folder = new File(System.getProperty("user.dir") + "/target/version-diff-1.0-SNAPSHOT");
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                s = s + "\nFile " + listOfFile.getName();
            } else if (listOfFile.isDirectory()) {
                s = s + "Directory " + listOfFile.getName();
            }
        }

        String pathStr = System.getProperty("user.dir") + "/target/version-diff-1.0-SNAPSHOT/index.html";
        Path path = FileSystems.getDefault().getPath(pathStr);

        in.setBody(Files.readAllBytes(path));
        in.setHeader(Exchange.CONTENT_TYPE, "text/html");
        in.setHeader(Exchange.HTTP_RESPONSE_CODE, "200");
        return "org=" + org + ", proj=" + proj;
    }
}
