package application;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler; 
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.Handler;

import qa.Globals;

public class GanswerHttp {
	static int maxAnswerNum = 100;
	static int maxSparqlNum = 3;
	static int defaultPort = 9999;
	public static void main(String[] args) throws Exception {  
	//step 1: initialize the server with a given port
	if(args.length>0){
		for(int k=0;k<args.length;k++){
			String[] paras = args[k].split("=");
			if(paras[0].startsWith("port")){
				try{
					defaultPort = Integer.parseInt(paras[1]);
				}
				catch(Exception e){
					System.out.println("Port should be an Integer. Using default port 9999 instead.");
					defaultPort = 9999;
				}
			}
			else if(paras[0].startsWith("maxAnswerNum")){
				try{
					maxAnswerNum = Integer.parseInt(paras[1]);
				}
				catch(Exception e){
					System.out.println("maxAnswerNum should be an Integer. Using default value 100 instead.");
					maxAnswerNum = 100;
				}
			}
			else if(paras[0].startsWith("maxSparqlNum")){
				try{
					maxSparqlNum = Integer.parseInt(paras[1]);
				}
				catch(Exception e){
					System.out.println("maxSparqlNum should be an Integer. Using default value 3 instead.");
					maxSparqlNum = 9999;
				}
			}
			else{
				System.out.println("Args "+k+" is not a valid parameter!");
			}
		}
	}
        Server server = new Server(defaultPort);  
        
        //step 2: attach gAnswer function handler to the server
        ContextHandler contextGS = new ContextHandler("/gSolve");
        contextGS.setHandler(new GanswerHandler());
        ContextHandler contextGI = new ContextHandler("/gInfo");
        contextGI.setHandler(new GinfoHandler());
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] {contextGS, contextGI});
        server.setHandler(contexts);
        
        //step 3: attach gAnswer error handler to the server
        //TODO: using default error handler currently. Should replace it with a custom one
        ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setShowStacks(false);
        server.addBean(errorHandler);
        
        //step 4: start the server and initialize gAnswer
        server.start();
        server.dumpStdErr();
        Globals.init();
        System.out.println("Server ready!");
        server.join();  
    }  
}
