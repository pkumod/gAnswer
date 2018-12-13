package qa.extract;

import java.util.ArrayList;
import java.util.HashSet;

import qa.Globals;

import log.QueryLogger;

import nlp.ds.DependencyTree;
import nlp.ds.DependencyTreeNode;
import nlp.ds.Word;
import rdf.SimpleRelation;

public class CorefResolution {
	/**
	 * 1. a very simple reference resolution
	 * 2. Coref Resolution should be done after relation extraction and before items mapping
	 */
	public void process(ArrayList<SimpleRelation> simpleRelations, QueryLogger qlog) {
		if (qlog.s.words.length <= 4) return; // if the sentence is too short, skip the coref step.
		System.out.println("=====Co-reference resolution=======");		
		ArrayList<SimpleRelation> deleteList = new ArrayList<SimpleRelation>();
		
		for(SimpleRelation sr : simpleRelations) {
			Word w1=null, w2=null;
			
			if (sr.extractingMethod == 'S') {
				w1 = getRefWord(sr.arg1Word.getNnHead(), qlog.s.dependencyTreeStanford, qlog);
				w2 = getRefWord(sr.arg2Word.getNnHead(), qlog.s.dependencyTreeStanford, qlog);
			}
			else if (sr.extractingMethod == 'M') {
				w1 = getRefWord(sr.arg1Word.getNnHead(), qlog.s.dependencyTreeMalt, qlog);
				w2 = getRefWord(sr.arg2Word.getNnHead(), qlog.s.dependencyTreeMalt, qlog);				
			}
			else {
				continue;
			}
			
			if (w1 != null) {
				sr.arg1Word_beforeCRR = sr.arg1Word;
				sr.arg1Word = w1;
			}
			if (w2 != null) {
				sr.arg2Word_beforeCRR = sr.arg2Word;
				sr.arg2Word = w2;
			}
			
			if (sr.arg1Word == sr.arg2Word)
				deleteList.add(sr);
		}
		
		simpleRelations.removeAll(deleteList);
		
		printCRR(qlog);
		System.out.println("===================================");
	}

	// return the reference word of w
	public Word getRefWord (Word w, DependencyTree dt, QueryLogger qlog) {
		w = w.getNnHead();
		
		if (w.crr != null) {
			return w.crr;
		}
						
		/*
		 * method: (suitable for stanford parser (old version))
		 * (1) WDT --det--> []   eg: Which city is located in China?
		 * (2) WDT -------> V/J --rcmod--> []   eg: Who is married to someone that was born in Rome?
		 * "when is the sth" is conflict with this rule, so discarded. (3) W   -------> be <------- []	eg: Who is the author of WikiLeaks?
		 * (4) WDT -------> V --ccomp--> []   eg: The actor that married the child of a politician.
		 * (5) DT(that, which) --dep--> V  eg:The actors that married an athlete.   // DS parser error.
		 * (6) W(position=1) ------> NN	eg:What are the language used in China?	// DS parser error, should eliminate "WRB"：When was Carlo Giuliani shot?
		 * (7) where <--advmod-- V <--advcl-- V --prep/pobj--> []  eg: Who graduate from the school where Keqiang Li graduates?
		 */

		DependencyTreeNode dtn = dt.getNodeByIndex(w.position);
	
		// no need for root 
		if (dtn.father == null) return null;
		
		try {
			if(dtn.word.posTag.equals("WDT") && dtn.dep_father2child.equals("det")) {	// (1)
				if(qlog.MODE_debug) System.out.println(w + "-->" + dtn.father.word.getNnHead());
				w.crr = dtn.father.word.getNnHead();
			}
			else if(dtn.word.posTag.startsWith("W") && !dtn.word.posTag.equals("WRB") && dtn.word.position == 1 && dtn.father.word.posTag.equals("NN")) {	// (6)
				if(qlog.MODE_debug) System.out.println(w + "-->" + dtn.father.word.getNnHead());
				w.crr = dtn.father.word.getNnHead();
			}
			else if(dtn.word.posTag.equals("DT") 
					&& dtn.dep_father2child.equals("dep") 
					&& (dtn.word.baseForm.equals("that")||dtn.word.baseForm.equals("which"))) {	// (5)
				if(qlog.MODE_debug) System.out.println(w + "-->" + dtn.father.word.getNnHead());
				w.crr = dtn.father.word.getNnHead();
			}
//			else if(dtn.word.posTag.startsWith("W")
//					&& dtn.father.word.baseForm.equals("be")) {	// (3)  //&& dtn.dep_father2child.equals("attr")
//				DependencyTreeNode target = dtn.father.containDependencyWithChildren("nsubj");
//				if (target != null) {
//					if(qlog.MODE_debug) System.out.println(w + "-->" + target.word.getNnHead());
//					w.crr = target.word.getNnHead();
//				}
//			}
			else if(dtn.word.posTag.equals("WDT") 
					&& (dtn.father.word.posTag.startsWith("V") || dtn.father.word.posTag.startsWith("J"))
					&& dtn.father.dep_father2child.equals("rcmod")) {	// (2)
				if(qlog.MODE_debug) System.out.println(w + "-->" + dtn.father.father.word.getNnHead());
				w.crr = dtn.father.father.word.getNnHead();
			}
			else if(dtn.word.posTag.equals("WDT") 
					&& dtn.father.word.posTag.startsWith("V")
					&& dtn.father.dep_father2child.equals("ccomp")) {	// (4)
				if(qlog.MODE_debug) System.out.println(w + "-->" + dtn.father.father.word.getNnHead());
				w.crr = dtn.father.father.word.getNnHead();
			}
			else if (dtn.word.baseForm.equals("where")
					&& dtn.dep_father2child.equals("advmod")
					&& dtn.father.dep_father2child.equals("advcl")) {	// (7)
				DependencyTreeNode target = dtn.father.father.containDependencyWithChildren("prep");
				if (target != null) {
					target = target.containDependencyWithChildren("pobj");
				}
				else {
					for (DependencyTreeNode n : dtn.father.father.childrenList) {
						if (Globals.pd.relns_object.contains(n.dep_father2child)) {
							target = n;
						}
					}
				}
				if (target != null) {
					if(qlog.MODE_debug) System.out.println(w + "-->" + target.word.getNnHead());
					w.crr = target.word.getNnHead();
				}
			}
		} catch (Exception e) {}
		
		return w.crr;
	}
	
	public void printCRR (QueryLogger qlog) {
		HashSet<Word> printed = new HashSet<Word>();
		for (Word w : qlog.s.words) {
			w = w.getNnHead();
			if (printed.contains(w)) 
				continue;
			if (w.crr != null) 
				System.out.println("\""+w.getFullEntityName() + "\" is resoluted to \"" + w.crr.getFullEntityName() + "\"");
			printed.add(w);
		}
	}
}
