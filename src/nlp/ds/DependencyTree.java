package nlp.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import nlp.tool.StanfordParser;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class DependencyTree {
	public DependencyTreeNode root = null;
	public ArrayList<DependencyTreeNode> nodesList = null;
	
//	public GrammaticalStructure gs = null;		// Method 2: Stanford Parser
	
	public HashMap<String, ArrayList<DependencyTreeNode>> wordBaseFormIndex = null;
	
	public DependencyTree (Sentence sentence, StanfordParser stanfordParser) {
	
		HashMap<Integer, DependencyTreeNode> map = new HashMap<Integer, DependencyTreeNode>();
		nodesList = new ArrayList<DependencyTreeNode>();
		
//	    String[] sent = { "这", "是", "一个", "简单", "的", "句子", "。" };
	    String[] sent = sentence.getWordsArr();
	    List<CoreLabel> rawWords = SentenceUtils.toCoreLabelList(sent);
		List<TypedDependency> tdl = stanfordParser.getTypedDependencyList(rawWords);
		
		// 1. generate all nodes.
	    for (TypedDependency td : tdl) {
	    	// gov
	    	if (!map.containsKey(td.gov().index()) && !td.reln().getShortName().equals("root")) {
	    		Word w = sentence.getWordByIndex(td.gov().index());
	    		w.posTag = td.gov().tag();	// POS TAG
	    		DependencyTreeNode newNode = new DependencyTreeNode(w);
	    		map.put(td.gov().index(), newNode);
	    		nodesList.add(newNode);
	    	}
	    	// dep
	    	if (!map.containsKey(td.dep().index())) {
	    		Word w = sentence.getWordByIndex(td.dep().index());
	    		w.posTag = td.dep().tag(); // POS TAG
	    		DependencyTreeNode newNode = new DependencyTreeNode(w);
	    		map.put(td.dep().index(), newNode);
	    		nodesList.add(newNode);    		
	    	}
	    }
	    // 2. add edges.
	    for (TypedDependency td : tdl) {
	    	if (td.reln().getShortName().equals("root")) {
	    		this.root = map.get(td.dep().index());
	    		this.root.levelInTree = 0;
	    		this.root.dep_father2child = "root";
	    	}
	    	else {
		    	DependencyTreeNode gov = map.get(td.gov().index());
		    	DependencyTreeNode dep = map.get(td.dep().index());
		    	
		    	dep.father = gov;
		    	gov.childrenList.add(dep);
		    	dep.dep_father2child = td.reln().getShortName();
	    	}
	    }
	    
	    // add levelInTree, sort childrenList & nodesList
	    Stack<DependencyTreeNode> stack = new Stack<DependencyTreeNode>();
	    stack.push(this.root);
	    while (!stack.empty()) {
	    	DependencyTreeNode dtn = stack.pop();
	    	if (dtn.father != null) {	    	
		    	dtn.levelInTree = dtn.father.levelInTree + 1;
		    	dtn.sortChildrenList();
	    	}
	    	for (DependencyTreeNode chd : dtn.childrenList) {
	    		stack.push(chd);
	    	}
	    }
	    Collections.sort(nodesList, new DependencyTreeNodeComparator()); 
//	    for (DependencyTreeNode dtn : nodesList) {
//	    	dtn.linkNN(this);
//	    }
	}
	
	public DependencyTreeNode setRoot(Word w) {
		root = new DependencyTreeNode(w, "root", null);			
		return root;
	}
	
	public DependencyTreeNode setRoot(DependencyTreeNode root) {
		this.root = root;
		return this.root;
	}
	
	public void buildWordBaseFormIndex () {
		wordBaseFormIndex = new HashMap<String, ArrayList<DependencyTreeNode>>();
		for (DependencyTreeNode dtn: nodesList) {
			String w = dtn.word.baseForm;
			if (!wordBaseFormIndex.keySet().contains(w))
				wordBaseFormIndex.put(w, new ArrayList<DependencyTreeNode>());
			wordBaseFormIndex.get(w).add(dtn);
		}
	}
	
	public DependencyTreeNode insert(DependencyTreeNode father, Word w, String dep_father2child) {
		if (father == null || w == null)
			return null;
		
		DependencyTreeNode newNode = new DependencyTreeNode(w, dep_father2child, father);
		father.childrenList.add(newNode);
		return newNode;
	}
	
	public DependencyTreeNode getRoot() {
		return root;
	}
	
	public ArrayList<DependencyTreeNode> getNodesList(){
		return nodesList;
	}

	public ArrayList<DependencyTreeNode> getShortestNodePathBetween(DependencyTreeNode n1, DependencyTreeNode n2) 
	{
		if(n1 == n2) {
			return new ArrayList<DependencyTreeNode>();
		}
		
		ArrayList<DependencyTreeNode> path1 = getPath2Root(n1);
		ArrayList<DependencyTreeNode> path2 = getPath2Root(n2);
		
		int idx1 = path1.size()-1;
		int idx2 = path2.size()-1;
		DependencyTreeNode curNode1 = path1.get(idx1);
		DependencyTreeNode curNode2 = path2.get(idx2);
		
		while (curNode1 == curNode2) {
			idx1 --;
			idx2 --;
			if(idx1 < 0 || idx2 < 0) break;
			curNode1 = path1.get(idx1);
			curNode2 = path2.get(idx2);			
		}
		
		ArrayList<DependencyTreeNode> shortestPath = new ArrayList<DependencyTreeNode>();
		for (int i = 0; i <= idx1; i ++) {
			shortestPath.add(path1.get(i));
		}
		for (int i = idx2+1; i >= 0; i --) {
			shortestPath.add(path2.get(i));
		}
		
		System.out.println("Shortest Path between <" + n1 + "> and <" + n2 + ">:");
		System.out.print("\t-");
		for (DependencyTreeNode dtn : shortestPath) {
			System.out.print("<" + dtn + ">-");
		}
		System.out.println();
		
		return shortestPath;
	}
	
	public ArrayList<DependencyTreeNode> getPath2Root(DependencyTreeNode n1) {
		ArrayList<DependencyTreeNode> path = new ArrayList<DependencyTreeNode>();
		DependencyTreeNode curNode = n1;
		path.add(curNode);
		while (curNode.father != null) {
			curNode = curNode.father;
			path.add(curNode);
		}
		return path;
	}
	
	public ArrayList<DependencyTreeNode> getTreeNodesListContainsWords(String words) {
		ArrayList<DependencyTreeNode> ret = new ArrayList<DependencyTreeNode>();
		for (DependencyTreeNode dtn : nodesList) {
			if (dtn.word.originalForm.equalsIgnoreCase(words)
				|| dtn.word.baseForm.equalsIgnoreCase(words)
				|| words.contains(dtn.word.originalForm)
				|| words.contains(dtn.word.baseForm))
				ret.add(dtn);
		}
		return ret;
	}
	
	public DependencyTreeNode getNodeByIndex (int posi) {
		for (DependencyTreeNode dt : nodesList) {
			if (dt.word.position == posi) {
				return dt;
			}
		}
		return null;
	}
	
	public DependencyTreeNode getFirstPositionNodeInList(ArrayList<DependencyTreeNode> list) {
		int firstPosi = Integer.MAX_VALUE;
		DependencyTreeNode firstNode = null;
		for (DependencyTreeNode dtn : list) {
			if (dtn.word.position < firstPosi) {
				firstPosi = dtn.word.position;
				firstNode = dtn;
			}
		}
		return firstNode;
	}
	
	@Override
	public String toString() {
		String ret = "";

		Stack<DependencyTreeNode> stack = new Stack<DependencyTreeNode>();
		stack.push(root);
		while(!stack.empty()) {
			DependencyTreeNode curNode = stack.pop();
			for (int i = 0; i <= curNode.levelInTree; i ++)
				ret += " ";
			ret += "-> ";
			ret += curNode.word.baseForm;
			ret += "-";
			ret += curNode.word.posTag;
			ret += " (";
			ret += curNode.dep_father2child;
			ret += ")";
			ret += "[" + curNode.word.position + "]\n";
			
			for (DependencyTreeNode child : curNode.childrenList) {
				stack.push(child);
			}
		}		
		return ret;
	}	
}
