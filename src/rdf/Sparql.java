package rdf;

import java.util.ArrayList;
import java.util.Collections;
//import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import log.QueryLogger;
import nlp.ds.Sentence;
import nlp.ds.Sentence.SentenceType;
import qa.Globals;

public class Sparql implements Comparable<Sparql> 
{
	public ArrayList<Triple> tripleList = new ArrayList<Triple>();
	public boolean countTarget = false;
	public String mostStr = null;
	public String moreThanStr = null;
	public double score = 0;
	
	public String questionFocus = null;	// The answer variable
	public HashSet<String> variables = new HashSet<String>();
	
	public enum QueryType {Select,Ask}
	public QueryType queryType = QueryType.Select;
	
	public HashMap<Integer, SemanticRelation> semanticRelations = null;

	public void addTriple(Triple t) 
	{
		if(!tripleList.contains(t))
		{
			tripleList.add(t);
			score += t.score;
		}
	}
	
	public void delTriple(Triple t)
	{
		if(tripleList.contains(t))
		{
			tripleList.remove(t);
			score -= t.score;
		}
	}

	@Override
	public String toString() 
	{
		String ret = "";
		for (Triple t : tripleList) {
			ret += t.toString();
			ret += '\n';
		}
		return ret;
	}
	
	public void deduplicate()
	{
		HashSet<String> set = new HashSet<String>();
		ArrayList<Triple> list = new ArrayList<Triple>();
		for(Triple t: tripleList)
		{
			String st = t.toStringWithoutScore();
			if(set.contains(st))
				list.add(t);
			set.add(st);
		}
		for(Triple t: list)
			this.delTriple(t);
	}
	
	// Is it a Basic Graph Pattern without filter and aggregation?
	public boolean isBGP()
	{
		if(moreThanStr != null || mostStr != null || countTarget)
			return false;
		return true;
	}
	
	//Use to display (can not be executed)
	public String toStringForGStore() 
	{
		String ret = "";
		for (Triple t : tripleList) 
		{
			// !Omit obvious LITERAL
			if(t.object.equals("literal_HRZ"))
				continue;
			
			// !Omit some bad TYPEs
			if(t.predicateID==Globals.pd.typePredicateID && Globals.pd.bannedTypes.contains(t.object))
				continue;
			
			ret += t.toStringForGStore();
			ret += '\n';
		}
		return ret;
	}
	
	/**
	* @description:
	* 1. Select all variables for BGP queries to display specific information.
	* 2. DO NOT select all variables when Aggregation like "HAVING" "COUNT" ... 
	* (It may involves too many results, e.g. "which countries have more than 1000 caves?", caves is no need to display) 
	* @param: NULL.
	* @return: A SPARQL query can be executed by GStore (NO prefix of entities/predicates).
	*/
	public String toStringForGStore2()
	{
		String ret = "";
		variables.clear();
		for(Triple t: tripleList)
		{
			if (!t.isSubjConstant()) variables.add(t.subject.replaceAll(" ", "_"));
			if (!t.isObjConstant()) variables.add(t.object.replaceAll(" ", "_"));		
		}
		if(variables.size() == 0)
			queryType = QueryType.Ask;
		
		// part1: select / ask ...
		if (queryType==QueryType.Ask)
			ret += "ask";
		else if(countTarget)
			ret += ("select COUNT(DISTINCT " + questionFocus + ")");
		else
		{
			if(!isBGP())	// AGG: select question focus
				ret += ("select DISTINCT " + questionFocus);
			else	// BGP: select all variables
			{				
				ret += "select DISTINCT ";
				for (String v : variables)
					ret += v + " ";
			}
		}					
		
		// part2: triples
		ret += " where\n{\n";
		for(Triple t : tripleList) 
		{
			if (!t.object.equals("literal_HRZ")) {	// need not display literal
				ret += t.toStringForGStore();
				ret += " .\n";
			}
		}
		ret += "}\n";
		
		// part3: order by / group by ...
		if(moreThanStr != null)
			ret += moreThanStr+"\n";
		if(mostStr != null)
			ret += mostStr+"\n";
		
		// part4: limit
		if(queryType != QueryType.Ask && (mostStr == null || !mostStr.contains("LIMIT")))
			ret += "LIMIT " + Globals.MaxAnswerNum; 
		
		return ret;
	}
	
	//Use to execute (select all variables; format 'aggregation' and 'ask')
	public String toStringForVirtuoso()
	{
		String ret = "";
		HashSet<String> variables = new HashSet<String>();
		
		// prefix
		if (queryType==QueryType.Ask)
			ret += "ask where";
		else if(countTarget)
			ret += ("select COUNT(DISTINCT " + questionFocus + ") where");
		else
		{
			// AGG: select question focus
			if(moreThanStr != null || mostStr != null)
				ret += ("select DISTINCT " + questionFocus + " where");
			// BGP: select all variables
			else
			{
				for (Triple t: tripleList)
				{
					if (!t.isSubjConstant()) variables.add(t.subject.replaceAll(" ", "_"));
					if (!t.isObjConstant()) variables.add(t.object.replaceAll(" ", "_"));		
				}
				
				ret += "select ";
				for (String v : variables)
					ret += v + " ";
				ret += "where";
			}
		}					
		ret += "\n{\n";
		if(variables.size() == 0)
			variables.add(questionFocus);
		
		// triples
		for (Triple t : tripleList) 
		{
			if (!t.object.equals("literal_HRZ")) {
				ret += t.toStringForGStore();
				ret += " .\n";
			}
		}
		ret += "}\n";
		
		// suffix
		if(moreThanStr != null)
		{
			ret += moreThanStr+"\n";
		}
		if(mostStr != null)
		{
			ret += mostStr+"\n";
		}
	
		return ret;
	}
		
	public int getVariableNumber()
	{
		int res = 0;
		for (Triple t: tripleList)
		{
			if (!t.isSubjConstant()) res++;
			if (!t.isObjConstant()) res++;			
		}
		return res;
	}

	public void adjustTriplesOrder() 
	{
		Collections.sort(this.tripleList);
	}

	public int compareTo(Sparql o) 
	{
		double diff = this.score - o.score;
		if (diff > 0) 
			return -1;
		else if (diff < 0)
			return 1;
		else
			return 0;
	}
	
	@Override 
	public int hashCode() 
    { 
		int key = 0;
		for(Triple t: this.tripleList)
			key ^= t.hashCode();
        return key; 
    } 
	
	@Override 
	public boolean equals(Object spq) 
	{ 
	    Sparql tempSparql= (Sparql) spq; 
	    String s1 = this.toStringForGStore2(), s2 = tempSparql.toStringForGStore2();
	    if(this.toStringForGStore2().equals(tempSparql.toStringForGStore2()))
	    	return true; 
	    else 
	    	return false; 
	} 
	
	public Sparql(){}
	public Sparql(HashMap<Integer, SemanticRelation> semanticRelations) 
	{
		this.semanticRelations = semanticRelations;
	}
	
	public Sparql copy() 
	{
		Sparql spq = new Sparql(this.semanticRelations);
		for (Triple t : this.tripleList)
			spq.addTriple(t);
		return spq;
	}
	
	public void removeLastTriple() 
	{
		int idx = tripleList.size()-1;
		score -= tripleList.get(idx).score;
		tripleList.remove(idx);
	}
	
	public Sparql removeAllTypeInfo () 
	{
		score = 0;
		ArrayList<Triple> newTripleList = new ArrayList<Triple>();
		for (Triple t : tripleList) 
		{	
			if (t.predicateID != Globals.pd.typePredicateID) 
			{
				newTripleList.add(t);
				score += t.score;
			}
		}
		tripleList = newTripleList;
		return this;
	}

};
