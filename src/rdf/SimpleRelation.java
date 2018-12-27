package rdf;

import java.util.ArrayList;
import java.util.HashMap;

import paradict.PredicateIDAndSupport;
import qa.Globals;

import nlp.ds.DependencyTree;
import nlp.ds.DependencyTreeNode;
import nlp.ds.Word;

// allow repetition
public class SimpleRelation {
	public Word arg1Word = null;
	public Word arg2Word = null;
	public String relationParaphrase = null;
	public double matchingScore = 0;
	
	public Word arg1Word_beforeCRR = null;
	public Word arg2Word_beforeCRR = null;
	
	public HashMap<Integer, Double> pasList = new HashMap<Integer, Double>();
	
	public Word preferredSubj = null;
	
	public char extractingMethod = ' ';	// S: StanfordParser; M: MaltParser; N: N-gram; R: rules
	
	public SimpleRelation()
	{
		
	}
	
	public SimpleRelation(SimpleRelation sr) 
	{
		arg1Word = sr.arg1Word;
		arg2Word = sr.arg2Word;
		relationParaphrase = sr.relationParaphrase;
		matchingScore = sr.matchingScore;
		arg1Word_beforeCRR = sr.arg1Word_beforeCRR;
		arg2Word_beforeCRR = sr.arg2Word_beforeCRR;
		pasList = sr.pasList;
		preferredSubj = sr.preferredSubj;
		extractingMethod = 'R';
	}

	@Override
	public String toString() {
		return arg1Word.originalForm + "," + arg2Word.originalForm + "," + relationParaphrase + "," + matchingScore + "["+extractingMethod+"]";
		//return arg1Word.getFullEntityName() + "," + arg2Word.getFullEntityName() + "," + relationParaphrase + "," + matchingScore + "["+extractingMethod+"]";
	}
	
	public int getHashCode() {
		return arg1Word.hashCode() ^ arg2Word.hashCode();
	}
	
	public void setPasList (String pattern, double matchingScore, boolean[] matchedFlag) {
		ArrayList<PredicateIDAndSupport> list = Globals.pd.nlPattern_2_predicateList.get(pattern);
		for (PredicateIDAndSupport pidsup : list) {
			double sumSelectivity = 0;
			for (int i = 0; i < matchedFlag.length; i ++) {
				if (matchedFlag[i]) {
					sumSelectivity += pidsup.wordSelectivity[i];
				}
			}
			sumSelectivity = matchingScore*sumSelectivity*pidsup.support;			
			int pid = pidsup.predicateID;
			if (Globals.pd.dbo_predicate_id.contains(pid)) sumSelectivity *= 1.5; 
			
			if (!pasList.containsKey(pid))
				pasList.put(pid, sumSelectivity);
			else if (sumSelectivity > pasList.get(pid))
				pasList.put(pid, sumSelectivity);
		}
	}
	
	public void setPreferedSubjObjOrder(DependencyTree tree) {
		DependencyTreeNode n1 = tree.getNodeByIndex(this.arg1Word.position).getNNTopTreeNode(tree);
		DependencyTreeNode n2 = tree.getNodeByIndex(this.arg2Word.position).getNNTopTreeNode(tree);
		if (n1.father != null && n1.father.word.baseForm.equals("of") && n1.dep_father2child.equals("pobj")) {
			this.preferredSubj = this.arg1Word;
		}
		else if (n2.father != null && n2.father.word.baseForm.equals("of") && n2.dep_father2child.equals("pobj")) {
			this.preferredSubj = this.arg2Word;
		}
	}

}
