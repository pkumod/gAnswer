package application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler; 
   
public class HelloWorld  {  
    public static void main(String[] args) throws Exception {  
        Server server = new Server(8888);  
        ContextHandler context = new ContextHandler();
        context.setContextPath("/hello");
        context.setHandler(new HelloHandler());
        server.setHandler(context);
        server.start();  
        server.join();  
    }  
}
