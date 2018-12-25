package application;

import java.io.IOException;  

import javax.servlet.ServletException;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
  

import org.eclipse.jetty.server.Request;  
import org.eclipse.jetty.server.handler.AbstractHandler;  
  
public class HelloHandler extends AbstractHandler {  
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)  
            throws IOException, ServletException { 
    	
        response.setContentType("text/html;charset=utf-8");  
        response.setStatus(HttpServletResponse.SC_OK);  
        baseRequest.setHandled(true);  
        String data = request.getParameter("data");
        response.getWriter().println("<h1>Hello World</h1>");  
        response.getWriter().println("Request url: " + target); 
        response.getWriter().println(data);  
    }  
}  