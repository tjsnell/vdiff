package cc.notsoclever.tools;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class MyRouteBuilder extends RouteBuilder {

   public void configure() {

      from("restlet:http:/tags?restletMethods=get")
            .process(new Processor() {
               @Override
               public void process(Exchange exchange) throws Exception {
                  Versions versions = new Versions();
                  ObjectMapper mapper = new ObjectMapper();
                  String tags = versions.getTags();
                  String json = mapper.writeValueAsString(convert(tags));
                  exchange.getOut().setBody(json);
                  exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
               }
            });

      from("restlet:http:/{v1}/{v2}?restletMethods=get")
            .process(new Processor() {
               @Override
               public void process(Exchange exchange) throws Exception {
                  String v1 = exchange.getIn().getHeader("v1", String.class);
                  String v2 = exchange.getIn().getHeader("v2", String.class);

                  Versions versions = new Versions();
                  versions.compare(v1, v2);

                  ObjectMapper mapper = new ObjectMapper();

                  exchange.getOut().setBody(mapper.writeValueAsString(versions));
                  exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
               }
            });
   }

   public List<String> convert(String source) {
      ObjectMapper mapper = new ObjectMapper();
      List<String> myTypeList = null;
      try {
         myTypeList = mapper.readValue(source, new TypeReference<List<String>>() {
         });
      } catch (Exception e) {
         if (log.isErrorEnabled()) {
            log.error("Error converting JSON collection to List<MyType>.", e);
         }
      }

      List<String> tags = new ArrayList<String>();

      for (String s : myTypeList) {
         if (s.startsWith("camel-")) {
            tags.add(s.substring(6));
         }
      }
      return tags;
   }
}
