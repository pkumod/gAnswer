package rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import nlp.ds.Word;

public class SemanticQueryGraph implements Comparable<SemanticQueryGraph>
{
	public ArrayList<SemanticUnit> semanticUnitList = null;
	public HashMap<Integer, SemanticRelation> semanticRelations = new HashMap<>();
	public double score = 0;
	
	public SemanticQueryGraph(ArrayList<SemanticUnit> suList) 
	{
		semanticUnitList = suList;	//TODO: need copy?
		// Calculate Score by a reward function (TODO: using SVM-Rank)
	}

	public SemanticQueryGraph(SemanticQueryGraph head) 
	{
		semanticUnitList = new ArrayList<>();
		for(SemanticUnit su: head.semanticUnitList)
			semanticUnitList.add(su.copy());
		score = head.score;
	}
	
	public void connect(SemanticUnit u, SemanticUnit v)
	{
		if(u.equals(v))
			return;
		
		SemanticUnit su1 = null, su2 = null;
		for(SemanticUnit su: this.semanticUnitList)
			if(su.equals(u))
				su1 = su;
			else if(su.equals(v))
				su2 = su;
		if(su1 != null && su2 != null)
			if(!su1.neighborUnitList.contains(su2) && !su2.neighborUnitList.contains(su1))
			{
				su1.neighborUnitList.add(su2);
				su2.neighborUnitList.add(su1);
			}
	}
	
	public void merge(SemanticUnit u, SemanticUnit v)
	{
		SemanticUnit su1 = null, su2 = null;
		for(SemanticUnit su: this.semanticUnitList)
			if(su.equals(u))
				su1 = su;
			else if(su.equals(v))
				su2 = su;
		if(su1 != null && su2 != null)
		{
			for(SemanticUnit su: this.semanticUnitList)
				if(su != su2 && su.neighborUnitList.contains(su1) && !su.neighborUnitList.contains(su2))	//TODO: Notice, now REJECT multi-edges; The hash function of SR should be modified to allow multi-edges.
					su.neighborUnitList.add(su2);
			
			this.semanticUnitList.remove(su1);
			su2.neighborUnitList.remove(su1);
		}
	}

	@Override
	public int hashCode() {
		int code = 0;
		for(SemanticUnit su: this.semanticUnitList)
			code ^= su.hashCode();
		return code;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		if (o instanceof SemanticQueryGraph) 
		{
			int matchCnt = 0;
			for(SemanticUnit su1: ((SemanticQueryGraph) o).semanticUnitList)
				for(SemanticUnit su2: this.semanticUnitList)
				{
					if(su1.equals(su2))
					{
						if(su1.neighborUnitList.containsAll(su2.neighborUnitList) && su2.neighborUnitList.containsAll(su1.neighborUnitList))
							matchCnt++;
					}
				}
			if(matchCnt == ((SemanticQueryGraph) o).semanticUnitList.size() && matchCnt == this.semanticUnitList.size())
				return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(SemanticQueryGraph o) 
	{
		double diff = this.score - o.score;
		if (diff > 0) return -1;
		else if (diff < 0) return 1;
		else return 0;
	}
	
	public boolean isFinalState()
	{
		if(semanticUnitList == null || semanticUnitList.isEmpty())
			return false;
		
		// Basic assumption: a final Semantic Query Graph should be Connected.
		HashSet<SemanticUnit> visited = new HashSet<>();
		SemanticUnit start = semanticUnitList.get(0);
		visited.add(start);
		dfs(start, visited);
		
		if(visited.size() == semanticUnitList.size())
			return true;
		return false;
	}
	
	private void dfs(SemanticUnit headNode, HashSet<SemanticUnit> visited)
	{
		for(SemanticUnit curNode: headNode.neighborUnitList)
			if(!visited.contains(curNode))
			{
				visited.add(curNode);
				dfs(curNode, visited);
			}
			
		for(SemanticUnit curNode: semanticUnitList)
		{
			if(curNode.neighborUnitList.contains(headNode) || headNode.neighborUnitList.contains(curNode))
			{
				if(!visited.contains(curNode))
				{
					visited.add(curNode);
					dfs(curNode, visited);
				}
			}
		}
	}

	public void calculateScore(HashMap<Integer, SemanticRelation> potentialSemanticRelations) 
	{
		// 1. entity/type score
		double entSco = 0;
		for(SemanticUnit su: this.semanticUnitList)
		{
			Word w = su.centerWord;
			if(w.mayEnt && w.emList.size()>0)
				entSco += w.emList.get(0).score * 100;
			if(w.mayType && w.tmList.size()>0)
				entSco += w.tmList.get(0).score;
		}
		// 2. relation score
		double relSco = 0;
		int relCnt = 0;
		for(SemanticUnit su1: this.semanticUnitList)
			for(SemanticUnit su2: su1.neighborUnitList)
			{
				//Deduplicate
				if(su1.centerWord.position > su2.centerWord.position)
					continue;
				
				relCnt++;
				int key = su1.centerWord.getNnHead().hashCode() ^ su2.centerWord.getNnHead().hashCode();
				SemanticRelation sr = potentialSemanticRelations.get(key);
				if(sr == null)
					System.err.println("No semantic relation for: " + su1 + " & " + su2);
				else
				{
					relSco += sr.predicateMappings.get(0).score;
					semanticRelations.put(key, sr);
				}
			}
		relSco/=relCnt;	//average
		this.score = entSco + relSco;
	}
}
