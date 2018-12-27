package qa.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import lcn.EntityFragmentFields;
import log.QueryLogger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import fgmt.EntityFragment;
import rdf.EntityMapping;

public class DBpediaLookup {
	//There are two websites of the DBpediaLookup online service.
	//public static final String baseURL = "http://en.wikipedia.org/w/api.php?action=opensearch&format=xml&limit=10&search=";
	public static final String baseURL = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?MaxHits=5&QueryString=";
	
	public HttpClient ctripHttpClient = null;
	
	//public static final String begin = "<Text xml:space=\"preserve\">";
	//public static final String begin = "<Result>\n        <Label>";
	public static final String begin = "<Result>\n      <Label>";
	public static final int begin_length = begin.length();
	//public static final String end = "</Text>";
	public static final String end = "</Label>";
	public static final int end_length = end.length();
	
	public static HashMap<String, String>entMentionDict = null;	// TODO: base on redirect data & wikipedia click data to build mention2ent's dictionary, now just manually
	
	public DBpediaLookup() 
	{
		ctripHttpClient = new HttpClient();		
		ctripHttpClient.setTimeout(3000);
		
		entMentionDict = new HashMap<String, String>();
		entMentionDict.put("Prince_Charles", "Charles,_Prince_of_Wales");
	}
	
	public ArrayList<EntityMapping> getEntityMappings(String searchString, QueryLogger qlog) 
	{
		ArrayList<String> slist = new ArrayList<String>();
		if(entMentionDict.containsKey(searchString))
			slist.add(entMentionDict.get(searchString));
		else
			slist = lookForEntityNames(searchString, qlog);
		
		if (slist.size() == 0 && searchString.contains(". "))		
			slist.addAll(lookForEntityNames(searchString.replaceAll(". ", "."), qlog));		
		
		ArrayList<EntityMapping> emlist = new ArrayList<EntityMapping>();
		
		// Now string use "_" as delimiter (original)
		String[] sa = searchString.split("_");
		int UpperCnt = 0;
		for(String str: sa)
		{
			if( (str.charAt(0)>='A'&&str.charAt(0)<='Z') || (str.charAt(0)>='0'&&str.charAt(0)<='9') )
				UpperCnt ++;
		}
		
		System.out.print("DBpediaLookup find: " + slist + ", ");
		
		int count = 40;
		for (String s : slist) 
		{
			//consider ABBR only when all UPPER; drop when too long edit distance
			if(UpperCnt < sa.length && EntityFragment.calEditDistance(s, searchString.replace("_", ""))>searchString.length()/2)
				continue;
			
			int eid = -1;
			s = s.replace(" ", "_");
			if(EntityFragmentFields.entityName2Id.containsKey(s))
			{
				eid = EntityFragmentFields.entityName2Id.get(s);
				emlist.add(new EntityMapping(eid, s, count));
				count -=2 ;
			}
			else
			{
				System.out.print("Drop "+s+" because it not in Entity Dictionary. ");
			}
		}
		System.out.println("DBpediaLookup select: " + emlist);
		
		return emlist;
	}
	
	public ArrayList<String> lookForEntityNames (String searchString, QueryLogger qlog) {
		// URL transition: " " -> %20
		GetMethod getMethod = new GetMethod((baseURL+searchString).replaceAll(" ", "%20"));
		ArrayList<String> ret = new ArrayList<String>();
		int statusCode;
		
		try {
			statusCode = ctripHttpClient.executeMethod(getMethod);
		} catch (HttpException e) {
			e.printStackTrace();
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			return ret;
		}
		
		if (statusCode!=200) return null;
		
		String response = getMethod.getResponseBodyAsString();
		if (qlog != null && qlog.MODE_debug) {
			System.out.println("searchString=" + searchString);
			System.out.println("statusCode=" + statusCode);
			System.out.println("response=" + getMethod.getResponseBodyAsString());
		}
		getMethod.releaseConnection();
		
		//System.out.println(response);
				
		if (response == null || response.isEmpty())
			return ret;
		int idx1  = response.indexOf(begin);
		while (idx1 != -1) {
			int idx2 = response.indexOf(end, idx1+begin_length);
			String ss = response.substring(idx1+begin_length, idx2);
			ret.add(ss);
			//System.out.println(ss);
			idx1 = response.indexOf(begin, idx2 + end_length);
		}		

		return ret;
	}
	
	public static void main(String argv[]){
		
		DBpediaLookup dbplook = new DBpediaLookup();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				System.out.println("Test DBpediaLookup.");
				System.out.print("Please input the search string: ");
				String searchString = br.readLine();
				try {
					long t1 = System.currentTimeMillis();
					ArrayList<String> res = dbplook.lookForEntityNames(searchString, null);
					long t2 = System.currentTimeMillis();
					System.out.println(res);
					System.out.println("time=" + (t2-t1) + "ms");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();	
		}

		
		return;
	}
}
