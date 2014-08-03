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

public class MyRouteBuilder extends RouteBuilder {

   public void configure() {

      from("restlet:///tags/{orgName}/{projectName}?restletMethods=get")
            .to("log:bar")
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

       from("restlet:///foo/{orgName}/{projectName}?restletMethods=get")
             .to("log:foo")
             .process(new Processor() {
                 @Override
                 public void process(Exchange exchange) throws Exception {

                     Path currentRelativePath = Paths.get("");
                     String s = currentRelativePath.toAbsolutePath().toString();
                     System.out.println("Current relative path is: " + s);
                     System.out.println(System.getProperty("user.dir"));

                     File folder = new File(System.getProperty("user.dir") + "/target/version-diff-1.0-SNAPSHOT");
                     File[] listOfFiles = folder.listFiles();

                     for (int i = 0; i < listOfFiles.length; i++) {
                         if (listOfFiles[i].isFile()) {
                             s = s + "\nFile " + listOfFiles[i].getName();
                         } else if (listOfFiles[i].isDirectory()) {
                             s = s + "Directory " + listOfFiles[i].getName();
                         }
                     }

                     Message out = exchange.getOut();

                     String pathStr = System.getProperty("user.dir") + "/target/version-diff-1.0-SNAPSHOT/index.html";
                     Path path = FileSystems.getDefault().getPath(pathStr);


                     out.setBody(Files.readAllBytes(path));

                     /*
                     InputStream input = getClass().getClassLoader().getResourceAsStream("index.html");
                     BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                     StringBuffer sb = new StringBuffer();
                     //String s;
                     while ((s = reader.readLine()) != null) {
                         sb.append(s);
                     }
                     out.setBody(sb.toString());
                     */
                 }
             });
   }
}
