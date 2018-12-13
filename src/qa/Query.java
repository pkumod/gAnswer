package qa;

import java.util.ArrayList;

import nlp.ds.Sentence;
import qa.extract.EntityRecognition;
import rdf.MergedWord;

/**
 * 1. preprocessing of question
 * 2. Node Recognition
 * @author husen
 */
public class Query 
{
	public String NLQuestion = null;
	public String TransferedQuestion = null;
	public ArrayList<String> MergedQuestionList = null;
	public ArrayList<Sentence> sList  = null;
	
	public String queryId = null;
	public String preLog = "";
	
	public ArrayList<MergedWord> mWordList = null;
	
	public Query(){}
	public Query(String _question)
	{
		NLQuestion = _question;
		NLQuestion = removeQueryId(NLQuestion);
				
		TransferedQuestion = getTransferedQuestion(NLQuestion);	
		
		// step1. NODE Recognition
		MergedQuestionList = getMergedQuestionList(TransferedQuestion);
		
		// build Sentence
		sList = new ArrayList<Sentence>();
		for(String mergedQuestion: MergedQuestionList)
		{
			Sentence sentence = new Sentence(this, mergedQuestion);
			sList.add(sentence);
		}
	}
	
	public boolean isDigit(char ch)
	{
		if(ch>='0' && ch<='9')
			return true;
		return false;
	}
	
	public boolean isUpperWord(char ch)
	{
		if(ch>='A' && ch<='Z')
			return true;
		return false;
	}
	
	/**
	 * some words -> equivalent words
	 * 1、stanfordParser often parse incorrect.
	 * 2、Synonyms unify. eg, movie->film
	 * @param question
	 * @return transfered question
	 */
	public String getTransferedQuestion(String question)
	{
		//rule1: discard ".", because "." and "_" will be disconnected by parser. Discard word tail's "'", which may pollutes NER
		question = question.replace("' ", " ");
		String [] words = question.split(" ");
		String ret = "";
		for(String word: words)
		{
			String retWord = word;
			//TODO: now just check NUM in head/tail
			if(word.length()>=2 && !isDigit(word.charAt(0)) && !isDigit(word.charAt(word.length()-1)))
			{
				retWord = retWord.replace(".", "");
			}
			ret += retWord + " ";
		}
		if(ret.length()>1)
			ret = ret.substring(0,ret.length()-1);
		
		ret = ret.replace("-", " ");
		ret = ret.replace("in america", "in United States");
		
		//rule2: as well as -> and
		ret = ret.replace("as well as", "and");
		
		//rule3: movie -> film
		ret = ret.replace(" movie", " film");
		ret = ret.replace(" movies", " films");
		
		return ret;
	}
	
	/**
	 * Recognize entity & type & literal in KB and replace " " in Phrases with "_"
	 * @param question
	 * @return merged question list
	 */
	public ArrayList<String> getMergedQuestionList(String question)
	{
		ArrayList<String> mergedQuestionList = null;
		//entity & type recognize
		EntityRecognition er = new EntityRecognition(); 
		mergedQuestionList = er.process(question);
		preLog = er.preLog;
		mWordList = er.mWordList;

		return mergedQuestionList;
	}
	
	public String removeQueryId(String question)
	{
		String ret = question;
		int st = question.indexOf("\t");
		if(st!=-1 && question.length()>1 && question.charAt(0)>='0' && question.charAt(0)<='9')
		{
			queryId = question.substring(0,st);
			ret = question.substring(st+1);
			System.out.println("Extract QueryId :"+queryId);
		}
		return ret;
	}
}
