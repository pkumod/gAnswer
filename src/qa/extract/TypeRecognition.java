package qa.extract;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import nlp.ds.Word;
import nlp.tool.StopWordsList;
import fgmt.TypeFragment;
import lcn.SearchInTypeShortName;
import log.QueryLogger;
import qa.Globals;
import rdf.PredicateMapping;
import rdf.SemanticRelation;
import rdf.Triple;
import rdf.TypeMapping;

/*
 * 2016-6-17
 * 1. Recognize types (include YAGO type)
 * 2、Add some type mapping manually, eg, "US State"-"yago:StatesOfTheUnitedStates"
 * 3、Add some extend variable, (generalization of [variable with inherit type] -> [variable with inherit triples]) eg, ?canadian <birthPlace> <Canada>
 * */
public class TypeRecognition {
	
	public static ArrayList<Integer> type_Person = new ArrayList<Integer>();
	public static ArrayList<Integer> type_Place = new ArrayList<Integer>();
	public static ArrayList<Integer> type_Organisation = new ArrayList<Integer>();
	
	public static HashMap<String, String> extendTypeMap = null; 
	public static HashMap<String, Triple> extendVariableMap = null;
	
	SearchInTypeShortName st = new SearchInTypeShortName();
	
	static
	{
		extendTypeMap = new HashMap<String, String>();
		extendVariableMap = new HashMap<String, Triple>();
		
		//!Handwriting for convenience | TODO: approximate/semantic match of type
		extendTypeMap.put("NonprofitOrganizations", "dbo:Non-ProfitOrganisation");
		extendTypeMap.put("GivenNames", "dbo:GivenName");
		extendTypeMap.put("JamesBondMovies","yago:JamesBondFilms");
		extendTypeMap.put("TVShows", "dbo:TelevisionShow");
		extendTypeMap.put("USState", "yago:StatesOfTheUnitedStates");
		extendTypeMap.put("USStates", "yago:StatesOfTheUnitedStates");
		extendTypeMap.put("Europe", "yago:EuropeanCountries");
		extendTypeMap.put("Africa", "yago:AfricanCountries");
		
		//Types for wh-word
		if(TypeFragment.typeShortName2IdList != null)
		{
			if(TypeFragment.typeShortName2IdList.containsKey("Person"))
				type_Person.addAll(TypeFragment.typeShortName2IdList.get("Person"));
			if(TypeFragment.typeShortName2IdList.containsKey("NaturalPerson"))
				type_Person.addAll(TypeFragment.typeShortName2IdList.get("NaturalPerson"));
			if(TypeFragment.typeShortName2IdList.containsKey("Location"))
				type_Place.addAll(TypeFragment.typeShortName2IdList.get("Location"));
			if(TypeFragment.typeShortName2IdList.containsKey("Place"))
				type_Place.addAll(TypeFragment.typeShortName2IdList.get("Place"));
			if(TypeFragment.typeShortName2IdList.containsKey("Organisation"))
				type_Organisation.addAll(TypeFragment.typeShortName2IdList.get("Organisation"));
			if(TypeFragment.typeShortName2IdList.containsKey("Organization"))
				type_Organisation.addAll(TypeFragment.typeShortName2IdList.get("Organization"));
		}
	}
	
	public static void recognizeExtendVariable(Word w)
	{
		String key = w.baseForm;
		if(extendVariableMap.containsKey(key))
		{
			w.mayExtendVariable = true;
			Triple triple = extendVariableMap.get(key).copy();
			if(triple.subjId == Triple.VAR_ROLE_ID && triple.subject.equals(Triple.VAR_NAME))
				triple.subject = "?" + w.originalForm;
			if(triple.objId == Triple.VAR_ROLE_ID && triple.object.equals(Triple.VAR_NAME))
				triple.object = "?" + w.originalForm;
			w.embbededTriple = triple;
		}
	}
	
	public ArrayList<TypeMapping> getExtendTypeByStr(String allUpperFormWord)
	{
		ArrayList<TypeMapping> tmList = new ArrayList<TypeMapping>();
		
		//Do not consider SINGLE-word type (most are useless) | eg, Battle, War, Daughter
		if(allUpperFormWord.length() > 1 && allUpperFormWord.substring(1).equals(allUpperFormWord.substring(1).toLowerCase()))
			return null;
		
		//search in YAGO type
		if(TypeFragment.yagoTypeList.contains(allUpperFormWord))
		{
			//YAGO prefix
			String typeName = "yago:"+allUpperFormWord;
			TypeMapping tm = new TypeMapping(-1,typeName,Globals.pd.typePredicateID,1);
			tmList.add(tm);
		}
		else if(extendTypeMap.containsKey(allUpperFormWord))
		{
			String typeName = extendTypeMap.get(allUpperFormWord);
			TypeMapping tm = new TypeMapping(-1,typeName,Globals.pd.typePredicateID,1);
			tmList.add(tm);
		}
		if(tmList.size()>0)
			return tmList;
		else
			return null;
	}
	
	public ArrayList<TypeMapping> getTypeIDsAndNamesByStr (String baseform) 
	{
		ArrayList<TypeMapping> tmList = new ArrayList<TypeMapping>();
		
		try 
		{
			tmList = st.searchTypeScore(baseform, 0.4, 0.8, 10);
			Collections.sort(tmList);
			if (tmList.size()>0) 
				return tmList;
			else 
				return null;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
		
	public ArrayList<Integer> recognize (String baseform) {
		
		char c = baseform.charAt(baseform.length()-1);
		if (c >= '0' && c <= '9') {
			baseform = baseform.substring(0, baseform.length()-2);
		}
		
		try {
			ArrayList<String> ret = st.searchType(baseform, 0.4, 0.8, 10);
			ArrayList<Integer> ret_in = new ArrayList<Integer>();
			for (String s : ret) {
				System.out.println("["+s+"]");
				ret_in.addAll(TypeFragment.typeShortName2IdList.get(s));
			}
			if (ret_in.size()>0) return ret_in;
			else return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}

	public static void AddTypesOfWhwords (HashMap<Integer, SemanticRelation> semanticRelations) {
		ArrayList<TypeMapping> ret = null;
		for (Integer it : semanticRelations.keySet()) 
		{
			SemanticRelation sr = semanticRelations.get(it);
			if(!sr.arg1Word.mayType) 
			{
				ret = recognizeSpecial(sr.arg1Word.baseForm);
				if (ret != null) 
				{
					sr.arg1Word.tmList = ret;
				}
			}
			if(!sr.arg2Word.mayType) 
			{
				ret = recognizeSpecial(sr.arg2Word.baseForm);
				if (ret != null) 
				{
					sr.arg2Word.tmList = ret;
				}
			}
		}	
	}
	
	public static ArrayList<TypeMapping> recognizeSpecial (String wordSpecial) 
	{
		ArrayList<TypeMapping> tmList = new ArrayList<TypeMapping>();
		if (wordSpecial.toLowerCase().equals("who")) 
		{
			for (Integer i : type_Person) 
			{
				tmList.add(new TypeMapping(i,"Person",1));
			}
			//"who" can also means organization
			for (Integer i : type_Organisation) 
			{
				tmList.add(new TypeMapping(i,"Organization",1));
			}
			return tmList;
		}
		else if (wordSpecial.toLowerCase().equals("where")) 
		{
			for (Integer i : type_Place) 
			{
				tmList.add(new TypeMapping(i,"Place",1));
			}
			for (Integer i : type_Organisation) 
			{
				tmList.add(new TypeMapping(i,"Organization",1));
			}
			return tmList;
		}
		//TODO: When ...
		return null;
	}
	
	/*
	 * 1. Priority: mayEnt(Uppercase)>mayType>mayEnt
	 * 2. mayEnt=1: Constant
	 * 3. mayType=1:
	 * (1)Variable, a triple will be added when evaluation. | eg, Which [books] by Kerouac were published by Viking Press?
	 * (2)Constant, it modify other words. | eg, Are tree frogs a type of [amphibian]?
	 * 4、extend variable (a variable embedded triples)
	 * */
	public static void constantVariableRecognition(HashMap<Integer, SemanticRelation> semanticRelations, QueryLogger qlog) 
	{
		Word[] words = qlog.s.words;
		//NOTICE: modifiers(implicit relation) have not been considered.
		for (Integer it : semanticRelations.keySet()) 
		{
			SemanticRelation sr = semanticRelations.get(it);
			int arg1WordPos = sr.arg1Word.position - 1;
			int arg2WordPos = sr.arg2Word.position - 1;
			
			// extend variable recognition
			recognizeExtendVariable(sr.arg1Word);
			recognizeExtendVariable(sr.arg2Word);
			
			// constant or variable
			if(sr.arg1Word.mayExtendVariable)
			{
				//eg, ?canadian <birthPlace> <Canada> (both extendVariable & type)
				if(sr.arg1Word.mayType)
					sr.arg1Word.mayType = false;
				
				if(sr.arg1Word.mayEnt)
				{
					//rule: [extendVaraible & ent] + noun -> ent |eg, Canadian movies -> ent:Canada
					if(arg1WordPos+1 < words.length && words[arg1WordPos+1].posTag.startsWith("N"))
					{
						sr.arg1Word.mayExtendVariable = false;
						sr.isArg1Constant = true;
					}
					else
					{
						sr.arg1Word.mayEnt = false;
					}
				}
			}
			// type
			else if(sr.arg1Word.mayType)
			{
				//rule in/of [type] -> constant  |eg, How many [countries] are there in [exT:Europe] -> ?uri rdf:type yago:EuropeanCountries
				if(arg1WordPos >= 2 && (words[arg1WordPos-1].baseForm.equals("in") || words[arg1WordPos-1].baseForm.equals("of"))  
						&& !words[arg1WordPos-2].posTag.startsWith("V"))
				{
					sr.isArg1Constant = true;
					double largerScore = 1000;
					if(sr.predicateMappings!=null && sr.predicateMappings.size()>0)
						largerScore = sr.predicateMappings.get(0).score * 2;
					PredicateMapping nPredicate = new PredicateMapping(Globals.pd.typePredicateID, largerScore, "[type]");
					sr.predicateMappings.add(0,nPredicate);
					
					//constant type should be object
					sr.preferredSubj = sr.arg2Word;
				}
			}
			//ent: constant
			else if(sr.arg1Word.mayEnt)
			{
				sr.isArg1Constant = true;
			}
			
			// constant or variable
			if(sr.arg2Word.mayExtendVariable)
			{
				if(sr.arg2Word.mayType)
					sr.arg2Word.mayType = false;
				
				if(sr.arg2Word.mayEnt)
				{
					if(arg2WordPos+1 < words.length && words[arg2WordPos+1].posTag.startsWith("N"))
					{
						sr.arg2Word.mayExtendVariable = false;
						sr.isArg2Constant = true;
					}
					else
					{
						sr.arg2Word.mayEnt = false;
					}
				}
			}
			// type
			else if(sr.arg2Word.mayType)
			{
				//rule in/of [type] -> constant  |eg, How many [countries] are there in [exT:Europe] -> ?uri rdf:type yago:EuropeanCountries
				if(arg2WordPos >= 2 && (words[arg2WordPos-1].baseForm.equals("in") || words[arg2WordPos-1].baseForm.equals("of")) 
						&& !words[arg2WordPos-2].posTag.startsWith("V") )
				{
					sr.isArg2Constant = true;
					double largerScore = 1000;
					if(sr.predicateMappings!=null && sr.predicateMappings.size()>0)
						largerScore = sr.predicateMappings.get(0).score * 2;
					PredicateMapping nPredicate = new PredicateMapping(Globals.pd.typePredicateID, largerScore, "[type]");
					sr.predicateMappings.add(0,nPredicate);
					
					sr.preferredSubj = sr.arg1Word;
				}
				//rule: Be ... a type?
				if(words[0].baseForm.equals("be") && arg2WordPos >=3 && words[arg2WordPos-1].baseForm.equals("a"))
				{
					sr.isArg2Constant = true;
					double largerScore = 1000;
					if(sr.predicateMappings!=null && sr.predicateMappings.size()>0)
						largerScore = sr.predicateMappings.get(0).score * 2;
					PredicateMapping nPredicate = new PredicateMapping(Globals.pd.typePredicateID, largerScore, "[type]");
					sr.predicateMappings.add(0,nPredicate);
					
					sr.preferredSubj = sr.arg1Word;
				}
			}
			else if(sr.arg2Word.mayEnt)
			{
				sr.isArg2Constant = true;
			}
			
			if(sr.arg1Word != sr.preferredSubj)
				sr.swapArg1Arg2();
		}
	}
	
	public static void main (String[] args) 
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String type = "space mission";
		try 
		{
			TypeFragment.load();
			Globals.stopWordsList = new StopWordsList();
			TypeRecognition tr = new TypeRecognition();
			while(true)
			{
				System.out.print("Input query type: ");
				type = br.readLine();
				tr.recognize(type);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
