package application;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler; 
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.Handler;

import qa.Globals;

public class GanswerHttp {
	public static void main(String[] args) throws Exception {  
		//step 1: initialize the server with a given port
        Server server = new Server(9999);  
        
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
        server.join();  
        System.out.println("Server ready!");
    }  
}
