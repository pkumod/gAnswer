package nlp.ds;

import java.util.ArrayList;

import rdf.EntityMapping;
import rdf.Triple;
import rdf.TypeMapping;

public class Word implements Comparable<Word> 
{
	public boolean mayCategory = false;
	public boolean mayLiteral = false;
	public boolean mayEnt = false;
	public boolean mayType = false;
	public boolean mayExtendVariable = false;
	public String category = null;
	public ArrayList<EntityMapping> emList = null;
	public ArrayList<TypeMapping> tmList = null;
	public Triple embbededTriple = null;
	
	public String baseForm = null;
	public String originalForm = null;
	public String posTag = null;
	public int position = -1;	// Notice the first word's position = 1
	public String key = null;
	
	public boolean isCovered = false;
	public boolean isIgnored = false;
	
	//Notice: These variables are not used because we merge a phrase to a word if it is a node now.
	public String ner = null;	// record NER result
	public Word nnNext = null;
	public Word nnPrev = null;
	public Word crr	= null;		// coreference resolution result
	
	public Word represent = null; // This word is represented by others, eg, "which book is ..." "which"
	public boolean omitNode = false; // This word can not be node
	public Word modifiedWord = null; // This word modify which word (it modify itself if it is not a modified word)
	
	public Word (String base, String original, String pos, int posi) {
		baseForm = base;
		originalForm = original;
		posTag = pos;
		position = posi;		
		key = new String(originalForm+"["+position+"]");
	}
	
	@Override
	public String toString() {
		return key;
	}

	public int compareTo(Word another) {
		return this.position-another.position;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Word) 
			&& originalForm.equals(((Word)o).originalForm)
			&& position == ((Word)o).position;
	}
	
	// We now discard all NN information and return the word itself. | husen 2016
	public Word getNnHead() {
		Word w = this;
		return w;
		
//		if(w.mayEnt || w.mayType)
//			return w;
//		
//		while (w.nnPrev != null) {
//			w = w.nnPrev;
//		}
//		return w;
	}
	
	public String getFullEntityName() {
		Word w = this.getNnHead();
		return w.originalForm;
		
//		if(w.mayEnt || w.mayType)
//			return w.originalForm;
//		
//		StringBuilder sb = new StringBuilder("");
//		while (w != null) {
//			sb.append(w.originalForm);			
//			sb.append(' ');
//			w = w.nnNext;
//		}
//		sb.deleteCharAt(sb.length()-1);
//		return sb.toString();
	}
	
	public String getBaseFormEntityName() {
		Word w = this.getNnHead();
		if(w.mayEnt || w.mayType)
			return w.baseForm;
				
		StringBuilder sb = new StringBuilder("");
		while (w != null) {
			sb.append(w.baseForm);
			sb.append(' ');
			w = w.nnNext;
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	public String isNER () {
		return this.getNnHead().ner;
	}
		
	public void setIsCovered () {
		Word w = this.getNnHead();
		while (w != null) {
			w.isCovered = true;
			w = w.nnNext;
		}
	}	
}
