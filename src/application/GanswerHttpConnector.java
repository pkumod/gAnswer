package application;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public class GanswerHttpConnector {
	public static final String defaultServerIP = "127.0.0.1";
    public static final int defaultServerPort = 9999;
    
    private String serverIP;
    private int serverPort;
    
    public GanswerHttpConnector() {
        this.serverIP = GanswerHttpConnector.defaultServerIP;
        this.serverPort = GanswerHttpConnector.defaultServerPort;
    }

    public GanswerHttpConnector(int _port) {
        this.serverIP = GanswerHttpConnector.defaultServerIP;
        this.serverPort = _port;
    }

    public GanswerHttpConnector(String _ip, int _port) {
        this.serverIP = _ip;
        this.serverPort = _port;
    }
    
    public String sendGet(String param,String context) {
		String url = "http://" + this.serverIP + ":" + this.serverPort+context;
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
		System.out.println("parameter: "+param);

		try {
			param = URLEncoder.encode(param, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Broken VM does not support UTF-8");
		}

        try {
        	//careful: if you encode the "?data=" part, jetty may not accept such a encoding
            String urlNameString = url + "/?data=" + param;
            System.out.println("request: "+urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
			//set agent to avoid: speed limited by server if server think the client not a browser
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {

				result.append(line+"\n");

            }

        } catch (Exception e) {
            System.out.println("error in get request: " + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result.toString();
    }
    
    public String gSolve(String data){
    	String param = data;
    	String rst = sendGet(param,"/gSolve");
    	System.out.println(rst);
    	return rst;
    }
    
    public String gInfo(){
    	String param = "";
    	String rst = sendGet(param,"/gInfo");
    	System.out.println(rst);
    	return rst;
    }
    
    public static void main(String[] args){
    	GanswerHttpConnector ghc = new GanswerHttpConnector();
    	String data = "{\"maxAnswerNum\":\"3\",\"needSparql\":\"2\",\"question\":\"Who is the president of China?\"}";
    	ghc.gInfo();
    	ghc.gSolve(data);
    }
}
