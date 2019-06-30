package qa.parsing;

import log.QueryLogger;
import nlp.ds.DependencyTree;
import nlp.ds.DependencyTreeNode;
import nlp.ds.Word;
import nlp.ds.Sentence.SentenceType;
import qa.Globals;
import rdf.Sparql;
import rdf.Triple;

public class QuestionParsing {
	public void process(QueryLogger qlog) {
		getDependenciesAndNER(qlog);
		recognizeSentenceType(qlog);
	}
	
	public void getDependenciesAndNER (QueryLogger qlog) {
		try {
			long t1 = System.currentTimeMillis();
			
			qlog.s.dependencyTreeStanford = new DependencyTree(qlog.s, Globals.stanfordParser);
			
			long t2 = System.currentTimeMillis();
//			Globals.nerRecognizer.recognize(qlog.s);	//TODO: check NER
			
			System.out.println("====StanfordDependencies("+(t2-t1)+"ms)====");
			System.out.println(qlog.s.dependencyTreeStanford);
//			qlog.s.printNERResult();
			
			qlog.timeTable.put("StanfordParser", (int)(t2-t1));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void recognizeSentenceType(QueryLogger qlog)
	{
		boolean IsImperativeSentence = recognizeImperativeSentence(qlog.s.dependencyTreeStanford);
		if (IsImperativeSentence)
		{
			qlog.s.sentenceType = SentenceType.ImperativeSentence;
			//two dependencyTree's ignored words should equal
			for (DependencyTreeNode sNode : qlog.s.dependencyTreeStanford.nodesList)
				for (DependencyTreeNode mNode : qlog.s.dependencyTreeMalt.nodesList)
					if (sNode.equals(mNode) && (sNode.word.isIgnored||mNode.word.isIgnored))
						sNode.word.isIgnored = mNode.word.isIgnored = true;
			return;
		}
		
		boolean IsSpecialQuestion = recognizeSpecialQuestion(qlog.s.dependencyTreeStanford);
		if (IsSpecialQuestion)
		{
			qlog.s.sentenceType = SentenceType.SpecialQuestion;
			return;
		}
		
		boolean IsGeneralQuestion = recognizeGeneralQuestion(qlog.s.dependencyTreeStanford);
		if (IsGeneralQuestion)
		{
			qlog.s.sentenceType = SentenceType.GeneralQuestion;
			return;
		}
		
		//default is special
		qlog.s.sentenceType = SentenceType.SpecialQuestion;
		
	}
	
	//if imperative, omitting those polite words
	private boolean recognizeImperativeSentence(DependencyTree tree) {
		if(tree.getRoot().word.posTag.startsWith("V") || tree.getRoot().word.posTag.startsWith("NN")) {
			DependencyTreeNode dobj = null;
			DependencyTreeNode iobj = null;
			for (DependencyTreeNode n : tree.getRoot().childrenList) {
				if (n.dep_father2child.equals("dobj")) {
					dobj = n;
				}
				else if (n.dep_father2child.equals("iobj")) {
					iobj = n;
				}
			}
			if (dobj != null && iobj != null) {
				tree.getRoot().word.isIgnored = true;
				iobj.word.isIgnored = true;
				
				// give me a list of ..
				if (dobj.word.baseForm.equals("list"))
				{
					dobj.word.isIgnored = true;
				}
			
				return true;
			}
			
			//start with "List": List all games by GMT.
			if (dobj != null && tree.getRoot().word.baseForm.equals("list"))
			{
				//System.out.println("isListSentence!");
				tree.getRoot().word.isIgnored = true;
				
				return true;
			}
		}
		return false;
	}
	
	private boolean recognizeSpecialQuestion(DependencyTree tree)
	{
		DependencyTreeNode firstNode = null;
		for (DependencyTreeNode dtn : tree.nodesList)
			if (dtn.word.position == 1)
			{
				firstNode = dtn;
				break;
			}
		//eg. In which city...
		if (firstNode!=null && 
			(firstNode.word.posTag.equals("IN")||firstNode.word.posTag.equals("TO"))&&
			firstNode.dep_father2child.startsWith("prep"))
		{
			firstNode = null;
			for (DependencyTreeNode dtn : tree.nodesList)
				if (dtn.word.position == 2)
				{
					firstNode = dtn;
					break;
				}			
		}

		if (firstNode != null)
		{
			if (firstNode.word.posTag.startsWith("W"))
				return true;
		}
		return false;
	}
	
	private boolean recognizeGeneralQuestion(DependencyTree tree)
	{
		DependencyTreeNode firstNode = null;
		for (DependencyTreeNode dtn : tree.nodesList)
			if (dtn.word.position == 1)
			{
				firstNode = dtn;
				break;
			}
		
		if (firstNode != null)
		{
			String dep = firstNode.dep_father2child;
			String pos = firstNode.word.posTag;
			String baseform = firstNode.word.baseForm;
			
			if ((baseform.equals("be")||baseform.equals("do")) &&
				pos.startsWith("VB") &&
				(dep.equals("root")||dep.equals("cop")||dep.startsWith("aux")))
				return true;
		}
		return false;
	}
	
	public static String detectQuestionFocus(Sparql spq) {
		String ret = null;
		int posi = Integer.MAX_VALUE;
		for (Triple t : spq.tripleList) {
			
			if (!t.isSubjConstant()) {
				Word subj = t.getSubjectWord();
				if (subj!=null && subj.position < posi) {
					posi = subj.position;
					ret = t.subject;
				}
			}
			if (!t.isObjConstant()) {
				Word obj = t.getObjectWord();
				if (obj!=null && obj.position < posi) {
					posi = obj.position;
					ret = t.object;
				}
			}
		}
		if (ret != null) return ret.replace(' ', '_');
		else return null;
	}
}
