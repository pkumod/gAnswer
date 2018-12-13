package lcn;

import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import fgmt.TypeFragment;
import qa.Globals;
import rdf.TypeMapping;

public class SearchInTypeShortName {
	// get id and score -- husen
	public ArrayList<TypeMapping> searchTypeScore(String s, double thres1, double thres2, int k) throws Exception
	{		
		Hits hits = null;
		String queryString = s;
		Query query = null;
		
		IndexSearcher searcher = new IndexSearcher(Globals.localPath+"data/DBpedia2016/lucene/type_fragment_index");

		ArrayList<TypeMapping> tmList = new ArrayList<TypeMapping>();

		Analyzer analyzer = new StandardAnalyzer();
		try {
			QueryParser qp = new QueryParser("SplittedTypeShortName", analyzer);
			query = qp.parse(queryString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (searcher != null) {
			hits = searcher.search(query);
			
			//System.out.println("find " + hits.length() + " matched type.");
			if (hits.length() > 0) {
				for (int i=0; i<hits.length(); i++) {
					if (i < k) {
						//System.out.println("<<<<---" + hits.doc(i).get("TypeShortName") + " : " + hits.score(i));
					    if(hits.score(i) >= thres1)
					    {
					    	//System.out.println("Score>=thres1("+thres1+") ---" + hits.doc(i).get("TypeShortName") + " : " + hits.score(i));
					    	String type = hits.doc(i).get("TypeShortName");
					    	System.out.println("Matched type: " + type + " : " + hits.score(i));
					    	
					    	ArrayList<Integer> ret_in = TypeFragment.typeShortName2IdList.get(type);
					    	if(ret_in!=null)
					    	{
						    	for(Integer tid: ret_in)
						    	{
						    		TypeMapping typeMapping = new TypeMapping(tid, hits.doc(i).get("TypeShortName"), hits.score(i));
						    		tmList.add(typeMapping);
						    	}
					    	}
					    }
					    else {
					    	break;
					    }
					}
					else {
					    if(hits.score(i) >= thres2)
					    {
					    	System.out.println("<<<<---" + hits.doc(i).get("TypeShortName") + " : " + hits.score(i));

					    	ArrayList<Integer> ret_in = TypeFragment.typeShortName2IdList.get(s);
					    	if(ret_in!=null)
					    	{
						    	for(Integer tid: ret_in)
						    	{
						    		TypeMapping typeMapping = new TypeMapping(tid, hits.doc(i).get("TypeShortName"), hits.score(i));
						    		tmList.add(typeMapping);
						    	}
					    	}
					    }
					    else {
					    	break;
					    }						
					}
				}				
			}
		}		
		return tmList;	
	}
	
	public  ArrayList<String> searchType(String s, double thres1, double thres2, int k) throws Exception
	{		
		Hits hits = null;
		String queryString = null;
		Query query = null;
		
		IndexSearcher searcher = new IndexSearcher(Globals.localPath+"data/DBpedia2016/lucene/type_fragment_index");
		
		ArrayList<String> typeNames = new ArrayList<String>(); 
		
		//String[] array = s.split(" ");
		//queryString = array[array.length-1];
		queryString = s;

		Analyzer analyzer = new StandardAnalyzer();
		try {
			QueryParser qp = new QueryParser("SplittedTypeShortName", analyzer);
			query = qp.parse(queryString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (searcher != null) {
			hits = searcher.search(query);
			
			System.out.println("find " + hits.length() + " answars!");
			if (hits.length() > 0) {
				for (int i=0; i<hits.length(); i++) {
					if (i < k) {
						System.out.println("<<<<---" + hits.doc(i).get("TypeShortName") + " : " + hits.score(i));
					    if(hits.score(i) >= thres1){
					    	System.out.println("Score>=thres1("+thres1+") ---" + hits.doc(i).get("TypeShortName") + " : " + hits.score(i));
					    	typeNames.add(hits.doc(i).get("TypeShortName"));
					    	//if (satisfiedStrictly(hits.doc(i).get("SplittedTypeShortName"), queryString)) typeNames.add(hits.doc(i).get("TypeShortName"));
					    }
					    else {
					    	//break;
					    }
					}
					else {
					    if(hits.score(i) >= thres2){
					    	System.out.println("<<<<---" + hits.doc(i).get("TypeShortName") + " : " + hits.score(i));
					    	typeNames.add(hits.doc(i).get("TypeShortName"));
					    	//if (satisfiedStrictly(hits.doc(i).get("SplittedTypeShortName"), queryString)) typeNames.add(hits.doc(i).get("TypeShortName"));
					    }
					    else {
					    	break;
					    }						
					}
				}				
			}
		}		
		return typeNames;	
	}
	
	private boolean satisfiedStrictly (String splittedTypeShortName, String queryString) 
	{
		String[] tnames = splittedTypeShortName.toLowerCase().split(" ");
		String[] qnames = queryString.toLowerCase().split(" ");
		for (int i = 0; i < tnames.length; i ++) {
			if (tnames[i].length() == 0) continue;
			boolean matched = false;
			for (int j = 0; j < qnames.length; j ++) {
				if (tnames[i].equals(qnames[j])) {
					matched = true;
					break;
				}
			}
			if (!matched && !Globals.stopWordsList.isStopWord(tnames[i])) {
				return false;
			}
		}
		String qlast = qnames[qnames.length-1];
		boolean flag = false;
		for (int i = 0; i < tnames.length; i ++) {
			if (tnames[i].length() == 0) continue;
			if (tnames[i].equals(qlast)) {
				flag = true;
				break;
			}
		}
		
		if (flag) return true;
		else return false;
	}

}
