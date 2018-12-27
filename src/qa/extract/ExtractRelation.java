package qa.extract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import log.QueryLogger;
import nlp.ds.DependencyTree;
import nlp.ds.DependencyTreeNode;
import paradict.ParaphraseDictionary;
import qa.Globals;
import rdf.SimpleRelation;
import rdf.PredicateMapping;
import rdf.SemanticRelation;
import rdf.SemanticUnit;

public class ExtractRelation {

	public static final int notMatchedCountThreshold = 1; // the bigger, the looser (more relations can be extracted)
	public static final int notCoverageCountThreshold = 2; 
	
	/*
	 * Find relations by dependency tree & paraphrases.
	 * */
	public ArrayList<SimpleRelation> findRelationsBetweenTwoUnit(SemanticUnit su1, SemanticUnit su2, QueryLogger qlog)
	{
		DependencyTree T = qlog.s.dependencyTreeStanford;
		if(qlog.isMaltParserUsed)
			T = qlog.s.dependencyTreeMalt;
		
		DependencyTreeNode n1 = T.getNodeByIndex(su1.centerWord.position), n2 = T.getNodeByIndex(su2.centerWord.position);
		ArrayList<DependencyTreeNode> shortestPath = T.getShortestNodePathBetween(n1,n2);
		
		ArrayList<SimpleRelation> ret = new ArrayList<SimpleRelation>();
		HashSet<String> BoW_T = new HashSet<String>();
		HashSet<String> SubBoW_T = new HashSet<String>();
				
		// (Fix shortest path) Some cases consider the words not in shortest path | eg: What [be] [ent] (famous) for?
		// what-be-[ent], the word [be] is useless but we need (famous)
		if(shortestPath.size() == 3 && shortestPath.get(1).word.baseForm.equals("be") && T.nodesList.size() > shortestPath.get(2).word.position)
		{
			shortestPath.remove(1);
			shortestPath.add(1, T.getNodeByIndex(shortestPath.get(1).word.position + 1));
		}
			
		// Shortest path -> SubBag of Words
		for(DependencyTreeNode curNode: shortestPath)
		{
			String text = curNode.word.baseForm;
			if(!curNode.word.isIgnored && !Globals.stopWordsList.isStopWord(text))
			{
				//!split words |eg, soccer club -> soccer_club(after node recognition) -> soccer club(used in matching paraphrase)
				if(curNode.word.mayEnt || curNode.word.mayType)
				{
					String [] strArray = curNode.word.baseForm.split("_");
					for(String str: strArray)
						SubBoW_T.add(str);
				}
				else
				{
					SubBoW_T.add(text);
				}
			}
		}
		
		// DS tree -> Bag of Words
		for (DependencyTreeNode curNode : T.getNodesList()) 
		{
			if (!curNode.word.isIgnored) 
			{
				String text = curNode.word.baseForm;
				if(curNode.word.mayEnt || curNode.word.mayType)
				{
					String [] strArray = curNode.word.baseForm.split("_");
					for(String str: strArray)
						BoW_T.add(str);
				}
				else
				{
					BoW_T.add(text);	
				}
			}
		}
		// Find candidate patterns by SubBoW_T & inveretdIndex
		HashSet<String> candidatePatterns = new HashSet<String>();
		for (String curWord : SubBoW_T) 
		{
			ArrayList<String> postingList = Globals.pd.invertedIndex.get(curWord);
			if (postingList != null) 
			{
				candidatePatterns.addAll(postingList);
			}
		}
		
		// Check patterns by BoW_P & subtree matching
		int notMatchedCount = 0;
		HashSet<String> validCandidatePatterns = new HashSet<String>();
		for (String p : candidatePatterns) 
		{
			String[] BoW_P = p.split(" ");
			notMatchedCount = 0;	// not match number between pattern & question
			for (String s : BoW_P) 
			{
				if (s.length() < 2)
					continue;
				if (s.startsWith("["))
					continue;
				if (Globals.stopWordsList.isStopWord(s))
					continue;
				if (!BoW_T.contains(s)) 
				{
					notMatchedCount ++;
					if (notMatchedCount > notMatchedCountThreshold)
						break;
				}
			}
			if (notMatchedCount <= notMatchedCountThreshold) 
			{
				validCandidatePatterns.add(p);
				//TODO: to support matching like [soccer_club]
				subTreeMatching(p, BoW_P, shortestPath, T, qlog, ret, 'S');
			}
		}
		
		// Another chance for [soccer_club] (the relation embedded in nodes)
		if(validCandidatePatterns.size() > 0)
		{
			if(n1.word.originalForm.contains("_") || n2.word.originalForm.contains("_"))
			{
				for (String p : validCandidatePatterns) 
				{
					String[] BoW_P = p.split(" ");
					notMatchedCount = 0;
					int mappedCharacterCount = 0;
					int matchedWordInArg = 0;

					boolean[] matchedFlag = new boolean[BoW_P.length];
					for(int idx = 0; idx < BoW_P.length; idx ++) {matchedFlag[idx] = false;}
					int idx = 0;
					for (String s : BoW_P) 
					{	
						if(n1.word.baseForm.contains(s) || n2.word.baseForm.contains(s)) // Hit nodes
							matchedWordInArg++;
						if(BoW_T.contains(s))
						{
							mappedCharacterCount += s.length();
							matchedFlag[idx] = true;
						}
						idx++;
						if (s.length() < 2) 
							continue;
						if (s.startsWith("["))
							continue;
						if (Globals.stopWordsList.isStopWord(s))
							continue;
						if (!BoW_T.contains(s)) 
							notMatchedCount ++;
					}
					// Success if has 2 hits
					if(matchedWordInArg >= 2)
					{
						double matched_score = ((double)(BoW_P.length-notMatchedCount))/((double)(BoW_P.length));
						if (matched_score > 0.95) 
							matched_score *= 10; // award for WHOLE match 
						
						// TODO: this will make LONGER one has LARGER score, sometimes unsuitable | eg, be bear die in
						matched_score = matched_score * Math.sqrt(mappedCharacterCount);
						
						SimpleRelation sr = new SimpleRelation();
						sr.arg1Word = n1.word;
						sr.arg2Word = n2.word;
						sr.relationParaphrase = p;
						sr.matchingScore = matched_score;
						sr.extractingMethod = 'X';
						
						if (n1.dep_father2child.endsWith("subj"))
							sr.preferredSubj = sr.arg1Word;
						
						sr.arg1Word.setIsCovered();
						sr.arg2Word.setIsCovered();
						
						sr.setPasList(p, matched_score, matchedFlag);
						sr.setPreferedSubjObjOrder(T);
						
						ret.add(sr);
					}
				}
			}
		}
		return ret;
	}
	
	// Core function of paraphrase matching
	private void subTreeMatching (String pattern, String[] BoW_P, 
			ArrayList<DependencyTreeNode> shortestPath,
			DependencyTree T, QueryLogger qlog, 
			ArrayList<SimpleRelation> ret, char extractingMethod) 
	{
		DependencyTreeNode n1 = shortestPath.get(0);
		DependencyTreeNode n2 = shortestPath.get(shortestPath.size()-1);
		
		ParaphraseDictionary pd = Globals.pd;
		Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
		queue.add(T.getRoot());
				
		for(DependencyTreeNode curOuterNode: shortestPath)
		{
			outer:
			for(String s: BoW_P)
			{
				if(s.equals(curOuterNode.word.baseForm))
				{
					// try to match all nodes
					ArrayList<DependencyTreeNode> subTreeNodes = new ArrayList<DependencyTreeNode>();
					Queue<DependencyTreeNode> queue2 = new LinkedList<DependencyTreeNode>();
					queue2.add(curOuterNode);
					
					int unMappedLeft = BoW_P.length;
					int mappedCharacterCount = 0;
					int hitPathCnt = 0;	// words in pattern hit the shortest path
					int hitPathBetweenTwoArgCnt = 0; //words in pattern hit the shortest path and excluding the two target nodes
					double mappedCharacterCountPunishment = 0;	// punishment when contains [[]] (function word)
					
					DependencyTreeNode curNode;
					boolean[] matchedFlag = new boolean[BoW_P.length];
					for(int idx = 0; idx < BoW_P.length; idx ++) {matchedFlag[idx] = false;}			

					while (unMappedLeft > 0 && (curNode=queue2.poll())!=null) 
					{
						if (curNode.word.isIgnored) continue;
						int idx = 0;
						for (String ss : BoW_P) 
						{
							// words in pattern only can be matched once
							if (!matchedFlag[idx]) 
							{
								// check word 
								if (ss.equals(curNode.word.baseForm)) 
								{	
									unMappedLeft --;
									subTreeNodes.add(curNode);
									queue2.addAll(curNode.childrenList);
									matchedFlag[idx] = true;
									mappedCharacterCount += ss.length();
									if(shortestPath.contains(curNode))
									{
										hitPathCnt++;
										if(curNode!=n1 && curNode!=n2)
											hitPathBetweenTwoArgCnt++;
									}
									break;
								}
								// check POS tag
								else if (ss.startsWith("[") && posSame(curNode.word.posTag, ss)) 
								{	
									unMappedLeft --;
									subTreeNodes.add(curNode);
									queue2.addAll(curNode.childrenList);
									matchedFlag[idx] = true;
									mappedCharacterCount += curNode.word.baseForm.length();
									mappedCharacterCountPunishment += 0.01;
									break;
								}
							}
							idx ++;
						}
					}
					int unMatchedNoneStopWordCount = 0;
					int matchedNoneStopWordCount = 0;
					for (int idx = 0; idx < BoW_P.length; idx ++) {
						if (BoW_P[idx].startsWith("[")) continue;
						if (!matchedFlag[idx]) {
							if (!Globals.stopWordsList.isStopWord(BoW_P[idx]))	// unmatched
								unMatchedNoneStopWordCount ++;
						}
						else {
							if (!Globals.stopWordsList.isStopWord(BoW_P[idx]))	// matched
								matchedNoneStopWordCount ++;
						}							
					}

					if (unMatchedNoneStopWordCount > notMatchedCountThreshold) {
						if(qlog.MODE_debug) System.out.println("----But the pattern\"" + pattern + "\" is not a subtree.");
						break outer;
					}
					
					// MUST have notional words matched, non stop words > 0
					if (matchedNoneStopWordCount == 0){
						if(qlog.MODE_debug) System.out.println("----But the matching for pattern \"" + pattern + "\" does not have content words.");
						break outer;
					}
					
					// IF partial match and be covered by other pattern, give up the current pattern
					if (unMappedLeft > 0) {
						StringBuilder subpattern = new StringBuilder();
						for (int idx = 0; idx < BoW_P.length; idx ++) {
							if (matchedFlag[idx]) {
								subpattern.append(BoW_P[idx]);
								subpattern.append(' ');
							}
						}
						subpattern.deleteCharAt(subpattern.length()-1);
						if (pd.nlPattern_2_predicateList.containsKey(subpattern)) {
							if(qlog.MODE_debug) System.out.println("----But the partially matched pattern \"" + pattern + "\" is another pattern.");
							break outer;
						}
					}
					
					// !Preposition | suppose only have one preposition
					// TODO: consider more preposition | the first preposition may be wrong
					DependencyTreeNode prep = null;
					for (DependencyTreeNode dtn : subTreeNodes) {
						outer2:
						for (DependencyTreeNode dtn_child : dtn.childrenList) {							
							if(pd.prepositions.contains(dtn_child.word.baseForm)) {
								prep = dtn_child;
								break outer2;
							}
						}
					}
					boolean isContained = false;
					for(DependencyTreeNode dtn_contain : subTreeNodes) {
						if(dtn_contain == prep) isContained = true;
					}
					if(!isContained && prep != null) {
						subTreeNodes.add(prep);
					}
					
					// Relation extracted, set COVER flags
					for (DependencyTreeNode dtn : subTreeNodes) 
					{
						dtn.word.isCovered = true;
					}
					
					int cnt = 0;
					double matched_score = ((double)(BoW_P.length-unMappedLeft))/((double)(BoW_P.length));
					if (matched_score > 0.95) 
						matched_score *= 10; // Award for WHOLE match
					
					// The match ratio between pattern and path larger, the score higher; especially when uncovered with the two target nodes
					if(hitPathCnt != 0)
					{
						double hitScore = 1 + (double)hitPathCnt/(double)BoW_P.length;
						if(hitPathBetweenTwoArgCnt == hitPathCnt)
							hitScore += 1;
						else if(shortestPath.size() >= 4)	// If path long enough, pattern still cover with the target nodes, punishment 
						{
							//hitScore = 0.5;
							if(hitPathBetweenTwoArgCnt == 0) // If path long enough, pattern cover with target nodes totally, punishment a lot
								hitScore = 0.25;
						}
						matched_score *= hitScore;
					}
					
					matched_score = matched_score * Math.sqrt(mappedCharacterCount) - mappedCharacterCountPunishment;	// the longer, the better (unsuitable in some cases)
					if (qlog.MODE_debug) System.out.println("☆" + pattern + ", score=" + matched_score);

					DependencyTreeNode subject = n1;
					DependencyTreeNode object = n2;
					if (subject != object) 
					{	
						SimpleRelation sr = new SimpleRelation();
						sr.arg1Word = subject.word;
						sr.arg2Word = object.word;
						sr.relationParaphrase = pattern;
						sr.matchingScore = matched_score;
						sr.extractingMethod = extractingMethod;
						
						if (subject.dep_father2child.endsWith("subj"))
							sr.preferredSubj = sr.arg1Word;
						
						sr.arg1Word.setIsCovered();
						sr.arg2Word.setIsCovered();
						
						sr.setPasList(pattern, matched_score, matchedFlag);
						sr.setPreferedSubjObjOrder(T);
						
						ret.add(sr);
						cnt ++;
						//String binaryRelation = "<" + subjectString + "> <" + pattern + "> <" + objectString + ">";
					}
					if (cnt == 0) break outer;
				}
			}
		}
		
	}
	
	// [[det]], [[num]], [[adj]], [[pro]], [[prp]], [[con]], [[mod]]
	public boolean posSame(String tag, String posWithBracket) {
		if (	(posWithBracket.charAt(2) == 'd' && tag.equals("DT"))
			||	(posWithBracket.charAt(2) == 'n' && tag.equals("CD"))
			||	(posWithBracket.charAt(2) == 'a' && (tag.startsWith("JJ") || tag.startsWith("RB")))
			||	(posWithBracket.charAt(2) == 'c' && tag.startsWith("CC"))//TODO: how about "IN: subordinating conjunction"?
			||	(posWithBracket.charAt(2) == 'm' && tag.equals("MD"))) {
			return true;
		}
		else if (posWithBracket.charAt(2) == 'p') {
			if (	(posWithBracket.charAt(4) == 'o' && tag.startsWith("PR"))
				||	(posWithBracket.charAt(4) == 'p' && (tag.equals("IN") || tag.equals("TO")))) {
				return true;
			}
		}
		return false;
	}
	
	public HashMap<Integer, SemanticRelation> groupSimpleRelationsByArgsAndMapPredicate (ArrayList<SimpleRelation> simpleRelations) {
		System.out.println("==========Group Simple Relations=========");
		
		HashMap<Integer, SemanticRelation> ret = new HashMap<Integer, SemanticRelation>();
		HashMap<Integer, HashMap<Integer, StringAndDouble>>  key2pasMap = new HashMap<Integer, HashMap<Integer, StringAndDouble>>();
		for(SimpleRelation simr : simpleRelations) 
		{
			int key = simr.getHashCode();
			if (!ret.keySet().contains(key)) 
			{
				ret.put(key, new SemanticRelation(simr));
				key2pasMap.put(key, new HashMap<Integer, StringAndDouble>());
			}
			SemanticRelation semr = ret.get(key);
			HashMap<Integer, StringAndDouble> pasMap = key2pasMap.get(key);
						
			// Just use to display.
			if (simr.matchingScore > semr.LongestMatchingScore) 
			{
				semr.LongestMatchingScore = simr.matchingScore;
				semr.relationParaphrase = simr.relationParaphrase;
			}
			
			// for pid=x, no wonder from which pattern, we only record the highest score and the related pattern.
			for (int pid : simr.pasList.keySet()) {
				double score = simr.pasList.get(pid);
				if (!pasMap.containsKey(pid)) {
					pasMap.put(pid, new StringAndDouble(simr.relationParaphrase, score));
				}
				else if (score > pasMap.get(pid).score) {
					pasMap.put(pid, new StringAndDouble(simr.relationParaphrase, score));
				}
			}
		}
		
		for (Integer key : key2pasMap.keySet()) {
			SemanticRelation semr = ret.get(key);
			HashMap<Integer, StringAndDouble> pasMap = key2pasMap.get(key);
			semr.predicateMappings = new ArrayList<PredicateMapping>();
			//System.out.print("<"+semr.arg1Word.getFullEntityName() + "," + semr.arg2Word.getFullEntityName() + ">:");
			for (Integer pid : pasMap.keySet()) 
			{	
				semr.predicateMappings.add(new PredicateMapping(pid, pasMap.get(pid).score, pasMap.get(pid).str));
				//System.out.print("[" + Globals.pd.getPredicateById(pid) + "," + pasMap.get(pid).str + "," + pasMap.get(pid).score + "]");
			}
			Collections.sort(semr.predicateMappings);
		}
		System.out.println("=========================================");
		return ret;
	}	
	
	
}

class StringAndDouble {
	public String str;
	public double score;
	public StringAndDouble (String str, double score) {
		this.str = str;
		this.score = score;
	}
}
