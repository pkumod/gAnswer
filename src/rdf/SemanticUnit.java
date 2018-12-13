package rdf;

import java.util.ArrayList;
import java.util.HashMap;

import rdf.SemanticRelation;
import nlp.ds.DependencyTreeNode;
import nlp.ds.Word;

public class SemanticUnit 
{
	public Word centerWord = null;
	public ArrayList<DependencyTreeNode> describeNodeList = new ArrayList<DependencyTreeNode>();
	public ArrayList<SemanticUnit> neighborUnitList = new ArrayList<SemanticUnit>();
	public HashMap<Word, SemanticRelation> RelationList = new HashMap<Word, SemanticRelation>();
	
	public boolean isSubj = true;
	public Integer prefferdType = null;
	
	public SemanticUnit(Word center, boolean isSubJ)
	{
		centerWord = center;
		isSubj = isSubJ;
	}
	
	public SemanticUnit copy()
	{
		SemanticUnit su = new SemanticUnit(this.centerWord, this.isSubj);
		su.describeNodeList = (ArrayList<DependencyTreeNode>) this.describeNodeList.clone();
		su.neighborUnitList = (ArrayList<SemanticUnit>) this.neighborUnitList.clone();
		su.RelationList = (HashMap<Word, SemanticRelation>) this.RelationList.clone();
		return su;
	}
	
	@Override
	public int hashCode() {
		return centerWord.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SemanticUnit) {
			SemanticUnit su2 = (SemanticUnit) o;
			if(this.centerWord.equals(su2.centerWord))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() 
	{
		String ret = "<" + centerWord + ", {";
		for(SemanticUnit su: neighborUnitList)
			ret += su.centerWord + ", ";
		ret += "}>";
		
		return ret;
	}
	
}
