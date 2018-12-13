package nlp.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;  
import java.util.Stack;

public class DependencyTreeNode {
	public Word word = null;
	public String dep_father2child = null;
	
	public DependencyTreeNode father = null;
	public ArrayList<DependencyTreeNode> childrenList = null;
	
	public int levelInTree = -1;
	
	/**
	 * The constructor for knowing its father
	 * 
	 * @param w
	 * @param dep_father2child
	 * @param father
	 */
	public DependencyTreeNode(Word w, String dep_father2child, DependencyTreeNode father) 
	{
		word = w;
		this.dep_father2child = dep_father2child;
		this.father = father;
		this.childrenList = new ArrayList<DependencyTreeNode>();
		
		if(father==null) levelInTree = 0;
		else levelInTree = father.levelInTree+1;
	}

	/**
	 * The constructor for not knowing the father
	 * 
	 * @param word
	 */
	public DependencyTreeNode(Word w)
	{
		this.word = w;
		this.childrenList = new ArrayList<DependencyTreeNode>();
	}
	
	public void sortChildrenList () {
		childrenList.trimToSize();
		Collections.sort(childrenList, new DependencyTreeNodeComparator());
	}
	
	@Override
	public String toString(){
		return word.originalForm + "-" + word.posTag + "(" + dep_father2child + ")[" + word.position + "]";
	}
	
	public static void sortArrayList(ArrayList<DependencyTreeNode> list) {
		Collections.sort(list, new DependencyTreeNodeComparator());
	}
	
	public DependencyTreeNode containDependencyWithChildren (String dep) {
		for (DependencyTreeNode son : childrenList) {
			if (son.dep_father2child.equals(dep)) return son;
		}
		return null;
	}

	/**
	 * equal_or_startWith = true:   equal
	 * equal_or_startWith = false:  startWith
	 * 
	 * @param posChild
	 * @param equal_or_startWith
	 * @return
	 */
	public DependencyTreeNode containPosInChildren (String posChild, boolean equal_or_startWith) {
		for (DependencyTreeNode son : childrenList) {
			if (equal_or_startWith) {
				if (son.word.posTag.equals(posChild)) return son;
			}
			else {
				if (son.word.posTag.startsWith(posChild)) return son;
			}
		}
		return null;		
	}
	
	public DependencyTreeNode containWordBaseFormInChildren (String wordBaseFormChild) {
		for (DependencyTreeNode son : childrenList) {
			if (son.word.baseForm.equals(wordBaseFormChild)) return son;
		}
		return null;		
	}
	
	public DependencyTreeNode getNNTopTreeNode (DependencyTree T) {
		if(this.father != null && (this.dep_father2child.equals("nn") || (this.word.posTag.startsWith("NN") && this.dep_father2child.equals("dep")))) {
			return this.father.getNNTopTreeNode(T);
		}
		else return this;
	}
	
	public Word linkNN(DependencyTree T) {
		// (Now useless) backtracking the NN connections.
		ArrayList<DependencyTreeNode> nn = new ArrayList<DependencyTreeNode>();
		
		nn.add(this);

		if(this.father != null && (this.dep_father2child.equals("nn") 
				|| (this.word.posTag.startsWith("NN") && this.dep_father2child.equals("dep") && this.father.word.posTag.startsWith("NN")))) {
			nn.add(this.father);
			for(DependencyTreeNode son : this.father.childrenList) {
				if (son != this && son.dep_father2child.equals("nn")) {
					nn.add(son);
				}
			}
		}
		
		Stack<DependencyTreeNode> stack = new Stack<DependencyTreeNode>();
		stack.push(this);
		while (!stack.empty()) {
			DependencyTreeNode curNode = stack.pop();
			for(DependencyTreeNode son : curNode.childrenList) {
				if (son.dep_father2child.equals("nn") 
						|| (son.word.posTag.startsWith("NN") && son.dep_father2child.equals("dep") && son.father.word.posTag.startsWith("NN"))) {
					nn.add(son);
					stack.push(son);
				}
			}
		}
		
		DependencyTreeNode.sortArrayList(nn);

		int size = nn.size() - 1;
		for (int i = 0; i < size; i ++) {
			nn.get(i).word.nnNext = nn.get(i+1).word;
			nn.get(i+1).word.nnPrev = nn.get(i).word; 
		}
		
		return this.word.getNnHead();
	}

};


class DependencyTreeNodeComparator implements Comparator<DependencyTreeNode> {
  
    public int compare(DependencyTreeNode n1, DependencyTreeNode n2) { 
    	return n1.word.position - n2.word.position;
    }  
  
}  
