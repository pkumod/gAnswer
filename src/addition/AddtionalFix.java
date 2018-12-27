package addition;

import java.util.ArrayList;
import java.util.HashMap;

import paradict.PredicateIDAndSupport;
import log.QueryLogger;
import nlp.ds.Word;
import nlp.ds.Sentence.SentenceType;
import qa.Globals;
import rdf.SemanticUnit;
import rdf.Sparql;
import rdf.Sparql.QueryType;
import rdf.Triple;


public class AddtionalFix 
{
	public HashMap<String, String> pattern2category = new HashMap<String, String>();
	
	public AddtionalFix()
	{
		// Some category mappings for DBpedia, try automatic linking methods later. | base form
		pattern2category.put("gangster_from_the_prohibition_era", "Prohibition-era_gangsters");
		pattern2category.put("seven_wonder_of_the_ancient_world", "Seven_Wonders_of_the_Ancient_World");
		pattern2category.put("three_ship_use_by_columbus", "Christopher_Columbus");
		pattern2category.put("13_british_colony", "Thirteen_Colonies");
	}
	
	public void process(QueryLogger qlog)
	{
		fixCategory(qlog);
		oneTriple(qlog);
		oneNode(qlog);
		
		//aggregation
		AggregationRecognition ar = new AggregationRecognition();
		ar.recognize(qlog);
	
		//query type
		decideQueryType(qlog);
	}
	
	public void decideQueryType(QueryLogger qlog)
	{
		for(Sparql spq: qlog.rankedSparqls)
			if(qlog.s.sentenceType == SentenceType.GeneralQuestion)
				spq.queryType = QueryType.Ask;
	}
	
	public void fixCategory(QueryLogger qlog)
	{
		if(qlog == null || qlog.semanticUnitList == null)
			return;
		
		String var = null, category = null;
		for(SemanticUnit su: qlog.semanticUnitList)
		{
			if(su.centerWord.mayCategory)
			{
				var = "?"+su.centerWord.originalForm;
				category = su.centerWord.category;
			}
		}
		
		if(category != null && var != null)
			for(Sparql spq: qlog.rankedSparqls)
			{
				boolean occured = false;
				for(Triple tri: spq.tripleList)
				{
					if(tri.subject.equals(var))
					{
						occured = true;
						break;
					}
				}
				String oName = category;
				String pName = "subject";
				int pid = Globals.pd.predicate_2_id.get(pName);
				Triple triple =	new Triple(Triple.VAR_ROLE_ID, var, pid, Triple.CAT_ROLE_ID, oName, null, 100);
				spq.addTriple(triple);
			}
	}
	
	/* recognize one-Node query 
	 * Two cases：1、Special question|Imperative sentence	2、General question
	 * 1-1：how many [], highest [] ...  | For single variable, add constraint (aggregation)
	 * 1-2: What is backgammon? | What is a bipolar syndrome? | Search an entity (return itself or its type/description ...)
	 * 1-3: Give me all Seven Wonders of the Ancient World. | Notice, "Seven Wonders of the Ancient World" should be recognized as ENT before. (in fact it is CATEGORY in DBpeida)
 	 * 2-1: Are there any [castles_in_the_United_States](yago:type)
 	 * 2-2：Was Sigmund Freud married? | Lack of variable node.
 	 * 2-3：Are penguins endangered? | No suitable relation matching, need transition.
	 */ 
	public void oneNode(QueryLogger qlog)
	{
		if(qlog == null || qlog.semanticUnitList == null || qlog.semanticUnitList.size()>1)
			return;
		
		Word target = qlog.target;
		Word[] words = qlog.s.words;
		if(qlog.s.sentenceType != SentenceType.GeneralQuestion)
		{
			//1-1: how many [type] are there | List all [type]
			if(target.mayType && target.tmList != null)
			{
				String subName = "?"+target.originalForm;
				String typeName = target.tmList.get(0).typeName;
				Triple triple =	new Triple(Triple.VAR_ROLE_ID, subName, Globals.pd.typePredicateID, Triple.TYPE_ROLE_ID, typeName, null, 100);
				Sparql sparql = new Sparql();
				sparql.addTriple(triple);
				qlog.rankedSparqls.add(sparql);
			}
			//1-2: What is [ent]?
			else if(target.mayEnt && target.emList != null)
			{
				if(words.length >= 3 && words[0].baseForm.equals("what") && words[1].baseForm.equals("be"))
				{
					int eid = target.emList.get(0).entityID;
					String subName = target.emList.get(0).entityName;
					Triple triple =	new Triple(eid, subName, Globals.pd.typePredicateID, Triple.VAR_ROLE_ID, "?"+target.originalForm, null, target.emList.get(0).score);
					Sparql sparql = new Sparql();
					sparql.addTriple(triple);
					qlog.rankedSparqls.add(sparql);
				}
			}
			//1-3: Give me all Seven Wonders of the Ancient World.
			else if(target.mayCategory && target.category != null)
			{
				String oName = target.category;
				String pName = "subject";
				int pid = Globals.pd.predicate_2_id.get(pName);
				Triple triple =	new Triple(Triple.VAR_ROLE_ID, "?"+target.originalForm, pid, Triple.CAT_ROLE_ID, oName, null, 100);
				Sparql sparql = new Sparql();
				sparql.addTriple(triple);
				qlog.rankedSparqls.add(sparql);
			}
		}
		else 
		{
			if(target.mayEnt && target.emList != null)
			{
				//2-2：Was Sigmund Freud married?
				String relMention = "";
				for(Word word: words)
					if(word != target && !word.baseForm.equals(".") && !word.baseForm.equals("?"))
						relMention += word.baseForm+" ";
				if(relMention.length() > 1)
					relMention = relMention.substring(0, relMention.length()-1);
				
				ArrayList<PredicateIDAndSupport> pmList = null;
				if(Globals.pd.nlPattern_2_predicateList.containsKey(relMention))
					pmList = Globals.pd.nlPattern_2_predicateList.get(relMention);
				
				if(pmList != null && pmList.size() > 0)
				{
					int pid = pmList.get(0).predicateID;
					int eid = target.emList.get(0).entityID;
					String subName = target.emList.get(0).entityName;
					Triple triple =	new Triple(eid, subName, pid, Triple.VAR_ROLE_ID, "?x", null, 100);
					Sparql sparql = new Sparql();
					sparql.addTriple(triple);
					qlog.rankedSparqls.add(sparql);
				}
		
				//2-3：Are penguins endangered?
				else
				{
					if(target.position < words.length && pattern2category.containsKey(words[target.position].baseForm))
					{
						String oName = pattern2category.get(words[target.position].baseForm);
						String pName = "subject";
						int pid = Globals.pd.predicate_2_id.get(pName);
						int eid = target.emList.get(0).entityID;
						String subName = target.emList.get(0).entityName;
						Triple triple =	new Triple(eid, subName, pid, Triple.CAT_ROLE_ID, oName, null, 100);
						Sparql sparql = new Sparql();
						sparql.addTriple(triple);
						qlog.rankedSparqls.add(sparql);
					}
				}
			}
			//2-1: Are there any [castles_in_the_United_States](yago:type)
			else if(target.mayType && target.tmList != null)
			{
				String typeName = target.tmList.get(0).typeName;
				String subName = "?" + target.originalForm;
				//System.out.println("typeName="+typeName+" subName="+subName);
				Triple triple =	new Triple(Triple.VAR_ROLE_ID, subName, Globals.pd.typePredicateID, Triple.TYPE_ROLE_ID, typeName, null, 100);
				Sparql sparql = new Sparql();
				sparql.addTriple(triple);
				qlog.rankedSparqls.add(sparql);
			}
		}
	}
	
	/*
	 * One triple recognized but no suitable relation.
	 * */ 
	public void oneTriple (QueryLogger qlog)
	{
		if(qlog == null || qlog.semanticUnitList == null)
			return;
		
		if(qlog.s.sentenceType == SentenceType.SpecialQuestion)
		{
			Word[] words = qlog.s.words;
			if(qlog.semanticUnitList.size() == 2)
			{
				Word entWord = null, whWord = null;
				for(int i=0;i<qlog.semanticUnitList.size();i++)
				{
					if(qlog.semanticUnitList.get(i).centerWord.baseForm.startsWith("wh"))
						whWord = qlog.semanticUnitList.get(i).centerWord;
					if(qlog.semanticUnitList.get(i).centerWord.mayEnt)
						entWord = qlog.semanticUnitList.get(i).centerWord;
				}
				// 1-1: (what) is [ent] | we guess users may want the type of ent.
				if(entWord!=null && whWord!= null && words.length >= 3 && words[0].baseForm.equals("what") && words[1].baseForm.equals("be"))
				{
					int eid = entWord.emList.get(0).entityID;
					String subName = entWord.emList.get(0).entityName;
					Triple triple =	new Triple(eid, subName, Globals.pd.typePredicateID, Triple.VAR_ROLE_ID, "?"+whWord.originalForm, null, entWord.emList.get(0).score);
					Sparql sparql = new Sparql();
					sparql.addTriple(triple);
					qlog.rankedSparqls.add(sparql);
				}
			}
		}
	}
}

