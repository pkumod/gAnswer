package log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import qa.Matches;
import qa.Query;
import rdf.EntityMapping;
import rdf.SemanticRelation;
import rdf.Sparql;
import rdf.MergedWord;
import rdf.SemanticUnit;
import qa.Answer;
import nlp.ds.Sentence;
import nlp.ds.Word;

public class QueryLogger {
	public Sentence s = null;
	public String ipAdress = null;
	
	public Word target = null;
	public Sparql sparql = null;
	public Matches match = null;
	public ArrayList<Answer> answers = null;	
	
	public boolean MODE_debug = false;
	public boolean MODE_log = true;
	public boolean MODE_fragment = true;
	public boolean isMaltParserUsed = true;	// Notice, we utilize Malt Parser as default parser, which is different from the older version. TODO: some coref rules need changed to fit Malt Parser.
	
	public HashMap<String, Integer> timeTable = null;
	public ArrayList<MergedWord> mWordList = null;
	public ArrayList<SemanticUnit> semanticUnitList = null;
	public HashMap<Integer, SemanticRelation> semanticRelations = null;
	public HashMap<Integer, SemanticRelation> potentialSemanticRelations = null;
	public HashMap<Word, ArrayList<EntityMapping>> entityDictionary = null;
	public ArrayList<Sparql> rankedSparqls = null;
		
	public String NRlog = "";
	public String SQGlog = "";
	public int gStoreCallTimes = 0;
	
	public QueryLogger (Query query) 
	{
		timeTable = new HashMap<String, Integer>();
		rankedSparqls = new ArrayList<Sparql>();
		mWordList = query.mWordList;
	}
	
	public void reloadSentence(Sentence sentence)
	{
		this.s = sentence;
		if(this.semanticUnitList != null)
			this.semanticUnitList.clear();
		if(this.semanticRelations != null)
			this.semanticRelations.clear();
		if(this.rankedSparqls != null)
			this.rankedSparqls.clear();
	}
		
	// Source code: http://edu.21cn.com/java/g_189_755584-1.htm
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		
		int idx;
		if((idx = ip.indexOf(',')) != -1) {
			ip = ip.substring(0, idx);
		}
		return ip;
	}
	
	public void reviseAnswers()
	{	
		System.out.println("Revise Answers:");
		answers = new ArrayList<Answer>();
		if (match == null || sparql == null || match.answers == null || sparql.questionFocus == null)
			return;
		
		HashSet<Answer> answerSet = new HashSet<Answer>();
		String questionFocus = sparql.questionFocus;
		String sparqlString = sparql.toStringForGStore();		
		//System.out.println("mal="+match.answers.length);
		for (int i=0;i<match.answers.length;i++)
		{
			Answer ans = new Answer(questionFocus, match.answers[i]);
			if (!sparqlString.contains(ans.questionFocusValue))
				answerSet.add(ans);
		}
		
		for (Answer ans : answerSet)
			answers.add(ans);	
		
		Collections.sort(answers);
	}
	
	
}
