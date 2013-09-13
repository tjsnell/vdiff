package cc.notsoclever.tools;

import org.apache.camel.main.Main;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A Camel Application
 */
public class MainApp {
   public static void main(String... args) throws Exception {
      Main main = new Main();

      ApplicationContext context = new ClassPathXmlApplicationContext("application.xml");

//      main.bind("staticPageHandler", context.getBean("staticPageHandler"));


      main.enableHangupSupport();
      main.addRouteBuilder(new MyRouteBuilder());
      main.run(args);
   }



}


