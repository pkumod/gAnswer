package nlp.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import nlp.tool.CoreNLP;
import nlp.tool.MaltParser;
import nlp.tool.StanfordParser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;

public class DependencyTree {
	public DependencyTreeNode root = null;
	public ArrayList<DependencyTreeNode> nodesList = null;
	
	public SemanticGraph dependencies = null;	// Method 1: CoreNLP (discarded)
	public GrammaticalStructure gs = null;		// Method 2: Stanford Parser
	public DependencyStructure maltGraph = null;	// Method 3: MaltParser
	
	public HashMap<String, ArrayList<DependencyTreeNode>> wordBaseFormIndex = null;
	
	public DependencyTree (Sentence sentence, CoreNLP coreNLPparser) {
		SemanticGraph dependencies = coreNLPparser.getBasicDependencies(sentence.plainText);
		this.dependencies = dependencies;
		
		Stack<IndexedWord> stack = new Stack<IndexedWord>();
		IndexedWord iwRoot = dependencies.getFirstRoot();
		
		HashMap<IndexedWord, DependencyTreeNode> map = new HashMap<IndexedWord, DependencyTreeNode>();
		nodesList = new ArrayList<DependencyTreeNode>();

		stack.push(iwRoot);
		root = this.setRoot(sentence.getWordByIndex(iwRoot.index()));
		map.put(iwRoot, root);

		while (!stack.empty())
		{
			IndexedWord curIWNode = stack.pop();
			DependencyTreeNode curDTNode = map.get(curIWNode);
			
			for (IndexedWord iwChild : dependencies.getChildList(curIWNode)) {
				Word w = sentence.getWordByIndex(iwChild.index());
				DependencyTreeNode newDTNode = this.insert(
						curDTNode, 
						w, 
						dependencies.reln(curIWNode, iwChild).getShortName());
				map.put(iwChild, newDTNode);
				stack.push(iwChild);
			}
			
			curDTNode.sortChildrenList();
			nodesList.add(curDTNode);
		}
	}
	
	public DependencyTree (Sentence sentence, StanfordParser stanfordParser) {
		this.gs = stanfordParser.getGrammaticalStructure(sentence.plainText);
		
		HashMap<Integer, DependencyTreeNode> map = new HashMap<Integer, DependencyTreeNode>();
		nodesList = new ArrayList<DependencyTreeNode>();
		
		List<TypedDependency> tdl = gs.typedDependencies(false);
		// 1. generate all nodes.
	    for (TypedDependency td : tdl) {
	    	// gov
	    	if (!map.containsKey(td.gov().index()) && !td.reln().getShortName().equals("root")) {
	    		Word w = sentence.getWordByIndex(td.gov().index());
	    		DependencyTreeNode newNode = new DependencyTreeNode(w);
	    		map.put(td.gov().index(), newNode);
	    		nodesList.add(newNode);
	    	}
	    	// dep
	    	if (!map.containsKey(td.dep().index())) {
	    		Word w = sentence.getWordByIndex(td.dep().index());
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
	    for (DependencyTreeNode dtn : nodesList) {
	    	dtn.linkNN(this);
	    }
	}
	
	public DependencyTree (Sentence sentence, MaltParser maltParser)throws MaltChainedException {
		try {
			// the tokens are parsed in the following line
			DependencyStructure graph = maltParser.getDependencyStructure(sentence);
			this.maltGraph = graph;
			//System.out.println(graph);
			
			HashMap<Integer, DependencyTreeNode> map = new HashMap<Integer, DependencyTreeNode>();
			ArrayList<DependencyTreeNode> list = new ArrayList<DependencyTreeNode>();
			Stack<DependencyNode> stack = new Stack<DependencyNode>();
			DependencyNode nroot = graph.getDependencyRoot();
			stack.add(nroot);
			// 1. generate all nodes.
			while (!stack.isEmpty()) {
				DependencyNode n = stack.pop();
				DependencyNode sib = n.getRightmostDependent();
				int key = n.getIndex();
				//System.out.println("[current node][key="+key+"] "+n+" <"+n.getHeadEdge()+">");
				boolean flag = true;
				while (sib != null) {
					flag = false;
					stack.push(sib);
					sib = sib.getLeftSibling();
				}
				if (flag) {
					sib = n.getLeftmostDependent();
					while (sib != null) {
						stack.push(sib);
						sib = sib.getRightSibling();
					}
				}
				if (n.hasHead() && !map.containsKey(key)) {
					//String snode = n.toString(); 
					String sedge = n.getHeadEdge().toString();
					//System.out.println("[" + snode + "]  <" + sedge + ">");

					/*int position = 0;
					String wordOriginal = null;
					String wordBase;
					String postag = null;*/
					String dep = null;					
					int idx1, idx2;
					
					/*// position
					idx1 = snode.indexOf("ID:")+3;
					idx2 = snode.indexOf(' ', idx1);
					position = Integer.parseInt(snode.substring(idx1, idx2));
					
					// word
					idx1 = snode.indexOf("FORM:", idx2)+5;
					idx2 = snode.indexOf(' ', idx1);
					wordOriginal = snode.substring(idx1, idx2);
					wordBase = Globals.coreNLP.getBaseFormOfPattern(wordOriginal.toLowerCase());
					
					// postag
					idx1 = snode.indexOf("POSTAG:", idx2)+7;
					idx2 = snode.indexOf(' ', idx1);
					postag = snode.substring(idx1, idx2);*/
					
					// dep
					idx1 = sedge.lastIndexOf(':')+1;
					idx2 = sedge.lastIndexOf(' ');
					dep = sedge.substring(idx1, idx2);
					if (dep.equals("null")) {
						dep = null;
					}
					else if (dep.equals("punct")) {// No consider about punctuation
						continue;
					}
					
		    		DependencyTreeNode newNode = new DependencyTreeNode(sentence.getWordByIndex(key));
		    		newNode.dep_father2child = dep;
		    		map.put(key, newNode);
		    		list.add(newNode);
				}
			}
			
			
		    // 2. add edges
		    for (Integer k : map.keySet()) {
		    	DependencyNode n = graph.getDependencyNode(k);
		    	DependencyTreeNode dtn = map.get(k);
		    	if (dtn.dep_father2child == null) {
		    		this.setRoot(dtn);
		    		this.root.levelInTree = 0;
		    		this.root.dep_father2child = "root";
		    	}
		    	else {
			    	DependencyTreeNode father = map.get(n.getHead().getIndex());
			    	DependencyTreeNode child = map.get(n.getIndex());
			    	child.father = father;
			    	father.childrenList.add(child);
		    	}
		    }
		    
		    // Fix the tree for some cases.
		    if(list.size() > 11)
		    {
		    	DependencyTreeNode dt1 = list.get(11), dt2 = list.get(5);
		    	if(dt1!=null && dt2!=null && dt1.word.baseForm.equals("star") && dt1.father.word.baseForm.equals("be"))
		    	{
	    			if (dt2.word.baseForm.equals("film") || dt2.word.baseForm.equals("movie")) 
	    			{
	    				dt1.father.childrenList.remove(dt1);
	    				dt1.father = dt2;
	    				dt2.childrenList.add(dt1);
	    			}
		    	}
		    }
		    
		    // add levelInTree, sort childrenList & nodesList
		    for (DependencyTreeNode dtn : list) {
		    	if (dtn.father != null) {	    	
			    	dtn.levelInTree = dtn.father.levelInTree + 1;
			    	dtn.sortChildrenList();
		    	}		    	
		    }
		    
		    nodesList = list;
		    Collections.sort(nodesList, new DependencyTreeNodeComparator());	
		    for (DependencyTreeNode dtn : nodesList) {
		    	dtn.linkNN(this);
		    }
		} catch (MaltChainedException e) {
			//e.printStackTrace();
			//System.err.println("MaltParser exception: " + e.getMessage());
			throw e;
		}
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
