package rdf;

import java.util.ArrayList;

import rdf.SimpleRelation;

import nlp.ds.Word;

public class SemanticRelation {
	public Word arg1Word = null;
	public Word arg2Word = null;
	public String relationParaphrase = null;	// longest match
	public double LongestMatchingScore = 0;		// longest match score
	
	//judge difference when copy semantic relation from special pattern
	public int arg1SuffixId = 0;
	public int arg2SuffixId = 0;
	
	public Word arg1Word_beforeCRR = null;
	public Word arg2Word_beforeCRR = null;
	
	public ArrayList<PredicateMapping> predicateMappings = null;

	public boolean isArg1Constant = false;
	public boolean isArg2Constant = false;
	
	public char extractingMethod = ' ';	// S: StanfordParser; M: MaltParser; N: N-gram; R: rules
	
	public SemanticRelation dependOnSemanticRelation = null;
	public Word preferredSubj = null;
	
	public boolean isSteadyEdge = true;
	
	public SemanticRelation(SemanticRelation r2) {
		arg1Word = r2.arg1Word;
		arg2Word = r2.arg2Word;
		relationParaphrase = r2.relationParaphrase;
		LongestMatchingScore = r2.LongestMatchingScore;
		
		arg1SuffixId = r2.arg1SuffixId;
		arg2SuffixId = r2.arg2SuffixId;
		
		arg1Word_beforeCRR = r2.arg1Word_beforeCRR;
		arg2Word_beforeCRR = r2.arg2Word_beforeCRR;
		
		arg1Word.emList = r2.arg1Word.emList;
		arg2Word.emList = r2.arg2Word.emList;
		predicateMappings = r2.predicateMappings;
		
//		arg1Types = r2.arg1Types;
//		arg2Types = r2.arg2Types;
		
		isArg1Constant = r2.isArg1Constant;
		isArg2Constant = r2.isArg2Constant;		
		
		extractingMethod = r2.extractingMethod;
		
		dependOnSemanticRelation = r2.dependOnSemanticRelation;
		preferredSubj = r2.preferredSubj;
	}
	
	public void swapArg1Arg2()
	{
		Word tmpWord = arg1Word;
		arg1Word = arg2Word;
		arg2Word = tmpWord;
		int tmpSuffixId = arg1SuffixId;
		arg1SuffixId = arg2SuffixId;
		arg2SuffixId = tmpSuffixId;
		tmpWord = arg1Word_beforeCRR;
		arg1Word_beforeCRR = arg2Word_beforeCRR;
		arg2Word_beforeCRR = tmpWord;
		boolean tmpBool = isArg1Constant;
		isArg1Constant = isArg2Constant;
		isArg2Constant = tmpBool;
	}
	
	public SemanticRelation (SimpleRelation simr) {
		if (simr.preferredSubj == null) {
			if (simr.arg1Word.compareTo(simr.arg2Word) < 0) {
				this.arg1Word = simr.arg1Word;
				this.arg2Word = simr.arg2Word;
				this.arg1Word_beforeCRR = simr.arg1Word_beforeCRR;
				this.arg2Word_beforeCRR = simr.arg2Word_beforeCRR;
			}
			else {
				this.arg1Word = simr.arg2Word;
				this.arg2Word = simr.arg1Word;
				this.arg1Word_beforeCRR = simr.arg2Word_beforeCRR;
				this.arg2Word_beforeCRR = simr.arg1Word_beforeCRR;			
			}
			this.extractingMethod = simr.extractingMethod;
		}
		else {
			if (simr.arg1Word == simr.preferredSubj) {
				this.arg1Word = simr.arg1Word;
				this.arg2Word = simr.arg2Word;
				this.arg1Word_beforeCRR = simr.arg1Word_beforeCRR;
				this.arg2Word_beforeCRR = simr.arg2Word_beforeCRR;
				this.preferredSubj = simr.preferredSubj;
			}
			else {
				this.arg1Word = simr.arg2Word;
				this.arg2Word = simr.arg1Word;
				this.arg1Word_beforeCRR = simr.arg2Word_beforeCRR;
				this.arg2Word_beforeCRR = simr.arg1Word_beforeCRR;
				this.preferredSubj = simr.preferredSubj;
			}
			this.extractingMethod = simr.extractingMethod;
		}
	}
	
	@Override
	public int hashCode() {
		return arg1Word.hashCode() ^ arg2Word.hashCode() + arg1SuffixId + arg2SuffixId;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SemanticRelation) {
			SemanticRelation sr2 = (SemanticRelation) o;
			if (this.arg1Word.equals(sr2.arg1Word)
			&&	this.arg2Word.equals(sr2.arg2Word)
			&&	this.arg1SuffixId == sr2.arg1SuffixId
			&&	this.arg2SuffixId == sr2.arg2SuffixId
			&&	this.relationParaphrase.equals(sr2.relationParaphrase)
			&&	this.LongestMatchingScore == sr2.LongestMatchingScore) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return arg1Word.originalForm + "," + arg2Word.originalForm + "," + relationParaphrase + "," + LongestMatchingScore + "["+extractingMethod+"]";
//		return arg1Word.getFullEntityName() + "," + arg2Word.getFullEntityName() + "," + relationParaphrase + "," + LongestMatchingScore + "["+extractingMethod+"]";
	}
	
	public void normalizeScore()
	{
		double maxScore;
		
		if (arg1Word.emList!=null && !arg1Word.emList.isEmpty())
		{
			maxScore=0.0;
			for (EntityMapping em : arg1Word.emList)		
				maxScore = Math.max(maxScore, em.score);
			for (EntityMapping em : arg1Word.emList)
				em.score = em.score/maxScore;
		}

		if (arg2Word.emList!=null && !arg2Word.emList.isEmpty())
		{
			maxScore=0.0;
			for (EntityMapping em : arg2Word.emList)		
				maxScore = Math.max(maxScore, em.score);
			for (EntityMapping em : arg2Word.emList)
				em.score = em.score/maxScore;	
		}
		
		if (predicateMappings!=null && !predicateMappings.isEmpty())
		{
			maxScore=0.0;
			for (PredicateMapping pm : predicateMappings)
				maxScore = Math.max(maxScore, pm.score);
			for (PredicateMapping pm : predicateMappings)
				pm.score = pm.score/maxScore;	
		}
	}
}
