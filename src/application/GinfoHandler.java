package application;
import java.io.IOException;  

import javax.servlet.ServletException;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  

import log.QueryLogger;

import org.json.*;
import org.eclipse.jetty.server.Request;  
import org.eclipse.jetty.server.handler.AbstractHandler;

import qa.Globals;

public class GinfoHandler  extends AbstractHandler{

	public static String errorHandle(String status,String message,String question,QueryLogger qlog){
		JSONObject exobj = new JSONObject();
		try {
			exobj.put("status", status);
			exobj.put("message", message);
			exobj.put("query", question);
			if(qlog!=null&&qlog.rankedSparqls!=null&&qlog.rankedSparqls.size()>0){
				exobj.put("sparql", qlog.rankedSparqls.get(0).toStringForGStore2());
			}
		} catch (Exception e1) {
		}
		return exobj.toString();
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)  
            throws IOException, ServletException {
		try{
			response.setContentType("text/html;charset=utf-8");  
	        response.setStatus(HttpServletResponse.SC_OK);
	        JSONObject infoobj = new JSONObject();
	        
			infoobj.put("version", Globals.Version);
		    infoobj.put("dataset", Globals.Dataset);
		    infoobj.put("GDB system", Globals.GDBsystem);

	        //TODO add more info
	        baseRequest.setHandled(true);  
	        response.getWriter().println(infoobj.toString());  
		}
		catch(Exception e){
			if(e instanceof IOException){
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","IOException","",null));
				} catch (Exception e1) {
				}
			}
			else if(e instanceof JSONException){
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","JSONException","",null));
				} catch (Exception e1) {
				}
			}
			else if(e instanceof ServletException){
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","ServletException","",null));
				} catch (Exception e1) {
				}
			}
			else {
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","Unkown Exception","",null));
				} catch (Exception e1) {
				}
			}
		}
	}

}
