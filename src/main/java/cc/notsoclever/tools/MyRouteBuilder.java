package cc.notsoclever.tools;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jackson.map.ObjectMapper;

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
}
