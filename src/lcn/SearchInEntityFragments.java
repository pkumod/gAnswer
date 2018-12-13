package lcn;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import qa.Globals;


public class SearchInEntityFragments {

	/*
	 * Search entity in Lucene
	 * */
	public ArrayList<EntityNameAndScore> searchName(String literal, double thres1, double thres2, int k) throws IOException {
		Hits hits = null;
		String queryString = null;
		Query query = null;
	
		IndexSearcher searcher = new IndexSearcher(Globals.localPath+"data/DBpedia2016/lucene/entity_fragment_index");
		
		ArrayList<EntityNameAndScore> result = new ArrayList<EntityNameAndScore>(); 

		queryString = literal;
		
		Analyzer analyzer = new StandardAnalyzer();
		try
		{
			QueryParser qp = new QueryParser("EntityName", analyzer);
			query = qp.parse(queryString);
		} catch (ParseException e)
		{
			e.printStackTrace();
		}
		
		if (searcher != null)
		{
			hits = searcher.search(query);
			//System.out.println("search for entity fragment hits.length=" + hits.length());
			if (hits.length() > 0) 
			{
				//System.out.println("find " + hits.length() + " result!");
				for (int i=0; i<hits.length(); i++) {
				    //System.out.println(i+": <"+hits.doc(i).get("EntityName") +">;"
				    //		  +hits.doc(i).get("EntityFragment")
				    //		  + "; Score: " + hits.score(i)
				    //		  + "; Score2: " + hits.score(i)*(literalLength/hits.doc(i).get("EntityName").length()));    
				    if(i<k) {
				    	if (hits.score(i) >= thres1) {
					    	String en = hits.doc(i).get("EntityName");
					    	int id = Integer.parseInt(hits.doc(i).get("EntityId"));
					    	result.add(new EntityNameAndScore(id, en, hits.score(i)));
				    	}
				    	else {
				    		break;
				    	}
				    }
				    else {
				    	if (hits.score(i) >= thres2) {
					    	String en = hits.doc(i).get("EntityName");
					    	int id = Integer.parseInt(hits.doc(i).get("EntityId"));
					    	result.add(new EntityNameAndScore(id, en, hits.score(i)));
				    	}
				    	else {
				    		break;
				    	}
				    }
				}				    	  
			}				
		}
		
		//Collections.sort(result);
		return result;

	}

}
