package nlp.tool;

import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class StanfordParser {
	private LexicalizedParser lp;
	private TokenizerFactory<CoreLabel> tokenizerFactory;
	private TreebankLanguagePack tlp;
	private GrammaticalStructureFactory gsf;
	
	public StanfordParser() {
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	    tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	    tlp = new PennTreebankLanguagePack();
	    gsf = tlp.grammaticalStructureFactory();
	}
	
	public GrammaticalStructure getGrammaticalStructure (String sentence) {
	    List<CoreLabel> rawWords2 = 
		      tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
	    // Converts a Sentence/List/String into a Tree.
	    // In all circumstances, the input will be treated as a single sentence to be parsed.
	    Tree parse = lp.apply(rawWords2);

	    return gsf.newGrammaticalStructure(parse);
	    /*List<TypedDependency> tdl = gs.typedDependencies(false);
	    for (TypedDependency td : tdl) {
	    	System.out.println(td.reln().getShortName()+"("+td.gov()+","+td.dep()+")");
	    	System.out.println("gov="+td.gov()
	    			+"\tgov.index="
	    			+td.gov().index()
	    			+"\tgov.value="
	    			+td.gov().value()
	    			+"\tgov.pos="
	    			+((TreeGraphNode)td.gov().parent()).value());
	    }*/
	    //System.out.println(tdl);
	}
}
