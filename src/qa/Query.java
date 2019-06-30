package qa;

import java.util.ArrayList;
import java.util.List;

import nlp.ds.Sentence;
import nlp.ds.Word;
import qa.extract.EntityRecognitionCh;

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
	
	public List<Word> words = null;
	
	public Query(){}
	public Query(String _question)
	{
		NLQuestion = _question;
		NLQuestion = removeQueryId(NLQuestion);
				
		TransferedQuestion = getTransferedQuestion(NLQuestion);	
		
		// step1. NODE Recognition
//		MergedQuestionList = getMergedQuestionList(TransferedQuestion);
		words = EntityRecognitionCh.parseSentAndRecogEnt(TransferedQuestion);
		
		// build Sentence
		sList = new ArrayList<Sentence>();
		sList.add(new Sentence(words, TransferedQuestion)); // TODO: TransferedQuestion or _question
//		for(String mergedQuestion: MergedQuestionList)
//		{
//			Sentence sentence = new Sentence(this, mergedQuestion);
//			sList.add(sentence);
//		}
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
		//discard ? ! .
		if(question.endsWith("？") || question.endsWith("。") || question.endsWith("！"))
			question = question.substring(0, question.length()-1);
		
		//discard 《》 because stanford parser DO NOT recognize them. TODO: why?
		question = question.replace("《", "").replace("》", "");
		question = question.replace("“", "").replace("”", "");	// now just discard "" because they confuse the parser. 
		
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

		
		return ret;
	}
	
	public String removeQueryId(String question)
	{
		String ret = question;
		// case 1: 1\t
		int st = question.indexOf("\t");
		if(st!=-1 && question.length()>4 && isDigit(question.charAt(0)))
		{
			queryId = question.substring(0,st);
			ret = question.substring(st+1);
			System.out.println("Extract QueryId :"+queryId);
		}
		// case 2: q1: | 1:
		st = question.indexOf(":");
		if(st!=-1 && st<6  && question.length()>4 && (isDigit(question.charAt(0)) ||question.startsWith("q")))
		{
			queryId = question.substring(0,st).replace("q", "");
			ret = question.substring(st+1);
			System.out.println("Extract QueryId :"+queryId);
		}
		
		return ret;
	}
}
