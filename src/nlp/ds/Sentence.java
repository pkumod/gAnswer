package nlp.ds;

import java.util.ArrayList;
import java.util.HashMap;

import qa.Globals;
import qa.Query;
import rdf.MergedWord;

public class Sentence {
	public String plainText = null;
	public Word[] words = null;
	public HashMap<String, Word> map = null;
	
	public DependencyTree dependencyTreeStanford = null;
	public DependencyTree dependencyTreeMalt = null;
	
	public enum SentenceType {SpecialQuestion,GeneralQuestion,ImperativeSentence}
	public SentenceType sentenceType = SentenceType.SpecialQuestion;
	
	public Sentence (String s) 
	{
		plainText = s;
		words = Globals.coreNLP.getTaggedWords(plainText);
		map = new HashMap<String, Word>();
		for (Word w : words)
			map.put(w.key, w);
	}
	
	public Sentence (Query query, String s)
	{
		plainText = s;
		words = Globals.coreNLP.getTaggedWords(plainText);
		// inherit NodeRecognition's information
		for(Word word: words)
		{
			for(MergedWord mWord: query.mWordList)
			{
				if(word.originalForm.equals(mWord.name))
				{
					word.mayLiteral = mWord.mayLiteral;
					word.mayEnt = mWord.mayEnt;
					word.mayType = mWord.mayType;
					word.mayCategory = mWord.mayCategory;
					word.tmList = mWord.tmList;
					word.emList = mWord.emList;
					word.category = mWord.category;
				}
			}
		}
		map = new HashMap<String, Word>();
		for (Word w : words)
			map.put(w.key, w);
	}
	public ArrayList<Word> getWordsByString (String w) {
		ArrayList<Word> ret = new ArrayList<Word>();
		for (Word wo: words) {
			if (wo.originalForm.equals(w)) ret.add(wo);
		}
		return ret;
	}
	
	public Word getWordByIndex (int idx) {
		return words[idx-1];
	}
	
	public Word getWordByKey (String k) {
		return map.get(k);
	}
	
	public boolean hasModifier(Word w)
	{
		for(Word word: words)
			if(word!=w && word.modifiedWord==w)
				return true;
		return false;
	}
	
	public void printNERResult () {
		for (Word word : words) {
			System.out.print(word + "   ");
			System.out.println("ner=" + word.ner);
		}
	}		
}



