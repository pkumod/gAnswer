package rdf;

import qa.Globals;

public class TypeMapping implements Comparable<TypeMapping> 
{
	public Integer typeID = null;
	public String typeName = null;
	public double score = 0;
	
	/*
	 * 1, For standard type (DBO type in DBpedia), relation = typePredicateID (rdf:type)
	 * 2, For nonstandard type, typeID = -1
	 * 3, If add type into triples, need relation | eg, Which professional surfers were born in Australia? (?uri dbo:occupation res:Surfing) relation = dbo:occupation
	 * 4, If needn't add type, relation = -1 | eg, Who was the father of [Queen] Elizabeth II
	 * */
	public int prefferdRelation = Globals.pd.typePredicateID; 
	
	public TypeMapping(Integer tid, String type, double sco) 
	{
		typeID = tid;
		typeName = type;
		score = sco;
	}
	
	public TypeMapping(Integer tid, String type, Integer relation, double sco) 
	{
		typeID = tid;
		typeName = type.replace("_", "");
		score = sco;
		prefferdRelation = relation;
	}
	
	// In descending order: big --> small
	public int compareTo(TypeMapping o) 
	{
		double diff = this.score - o.score;
		if (diff > 0) return -1;
		else if (diff < 0) return 1;
		else return 0;
	}
	
	public int hashCode()
	{
		return typeID.hashCode();
	}
	
	public String toString() 
	{
		StringBuilder res = new StringBuilder(typeName+"("+score+")");
		return res.toString();
	}
}