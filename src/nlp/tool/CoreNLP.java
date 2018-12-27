package nlp.tool;

import java.util.List;
import java.util.Properties;

import nlp.ds.Word;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class CoreNLP {

	// CoreNLP can also recognize TIME and NUMBER (see SUTime)
	private StanfordCoreNLP pipeline_lemma;
	
	public CoreNLP () {
	    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    /*Properties props_all = new Properties();
	    props_all.put("annotators", "tokenize, ssplit, pos, lemma, parse");	// full list: "tokenize, ssplit, pos, lemma, ner, parse, dcoref"
	    pipeline_all = new StanfordCoreNLP(props_all);*/

	    Properties props_lemma = new Properties();
	    props_lemma.put("annotators", "tokenize, ssplit, pos, lemma");
	    pipeline_lemma = new StanfordCoreNLP(props_lemma);		

	}
	
	// For more efficient usage, refer to "http://www.jarvana.com/jarvana/view/edu/stanford/nlp/stanford-corenlp/1.2.0/stanford-corenlp-1.2.0-javadoc.jar!/edu/stanford/nlp/process/Morphology.html"
	public String getBaseFormOfPattern (String text) {
		String ret = new String("");
		
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    // run all Annotators on this text
	    pipeline_lemma.annotate(document);


	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    int count = 0;
	    for(CoreMap sentence: sentences) {
	      // traversing the words in the current sentence
	      // a CoreLabel is a CoreMap with additional token-specific methods
	      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        // this is the base form (lemma) of the token
	        String lemma = token.getString(LemmaAnnotation.class);
	        ret += lemma;
	        ret += " ";
	      }
	      count ++;
	      if (count % 100 == 0) {
	    	  System.out.println(count);
	      }
	    }
	    
	    return ret.substring(0, ret.length()-1);
	}
	
	public SemanticGraph getBasicDependencies (String s) {
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(s);
	    
	    // run all Annotators on this text
	    pipeline_lemma.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	      // this is the Stanford dependency graph of the current sentence
	      SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);
	      return dependencies;
	    }
	    
	    return null;
	}

	public Tree getParseTree (String text) {
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline_lemma.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	// this is the parse tree of the current sentence
	    	return sentence.get(TreeAnnotation.class);
	    }	    
	    
	    return null;
	}
	
	/**
	 * How to use:
	 * for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
	 * 		// this is the text of the token
	 * 		String word = token.get(TextAnnotation.class);
	 *		// this is the POS tag of the token
	 *		String pos = token.get(PartOfSpeechAnnotation.class);
	 *	}
	 * @param s
	 * @return
	 */
	public CoreMap getPOS (String s) {
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(s);
	    
	    // run all Annotators on this text
	    pipeline_lemma.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	      // this is the sentence with POS Tags
	      return sentence;
	    }
	    
	    return null;
	}
	
	public Word[] getTaggedWords (String sentence) {
		CoreMap taggedSentence = getPOS(sentence);
		Word[] ret = new Word[taggedSentence.get(TokensAnnotation.class).size()];
		int count = 0;
		for (CoreLabel token : taggedSentence.get(TokensAnnotation.class)) {
			// this is the text of the token
			String word = token.get(TextAnnotation.class);
			// this is the POS tag of the token
			String pos = token.get(PartOfSpeechAnnotation.class);
			//System.out.println(word+"["+pos+"]");
			ret[count] = new Word(getBaseFormOfPattern(word.toLowerCase()), word, pos, count+1);
			count ++;
		}
		return ret;
	}
	
	/*public void demo () {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    // read some text in the text variable
	    String text = ... // Add your text here!
	    
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	      // traversing the words in the current sentence
	      // a CoreLabel is a CoreMap with additional token-specific methods
	      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        // this is the text of the token
	        String word = token.get(TextAnnotation.class);
	        // this is the POS tag of the token
	        String pos = token.get(PartOfSpeechAnnotation.class);
	        // this is the NER label of the token
	        String ne = token.get(NamedEntityTagAnnotation.class);       
	      }

	      // this is the parse tree of the current sentence
	      Tree tree = sentence.get(TreeAnnotation.class);

	      // this is the Stanford dependency graph of the current sentence
	      SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	    }

	    // This is the coreference link graph
	    // Each chain stores a set of mentions that link to each other,
	    // along with a method for getting the most representative mention
	    // Both sentence and token offsets start at 1!
	    Map<Integer, CorefChain> graph = 
	      document.get(CorefChainAnnotation.class);
	}*/
}
