package nlp.tool;

import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.international.pennchinese.ChineseGrammaticalStructure;

public class StanfordParser {
	private LexicalizedParser lp;
	private ChineseGrammaticalStructure gs;
	
//	private TokenizerFactory<CoreLabel> tokenizerFactory;
//	private TreebankLanguagePack tlp;
//	private GrammaticalStructureFactory gsf;
	
	public StanfordParser() {
//		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
//	    tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
//	    tlp = new PennTreebankLanguagePack();
//	    gsf = tlp.grammaticalStructureFactory();
	    
	    lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/chinesePCFG.ser.gz");
	}
	
//	public GrammaticalStructure getGrammaticalStructure (String sentence) {
//	    List<CoreLabel> rawWords2 = 
//		      tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
//	    
//	    Tree parse = lp.apply(rawWords2);
//
//	    return gsf.newGrammaticalStructure(parse);
//	}
	
	public List<TypedDependency> getTypedDependencyList(List<CoreLabel> rawWords) 
	{
	    Tree parse = lp.apply(rawWords);
	    gs = new ChineseGrammaticalStructure(parse);
	    
	    return gs.typedDependenciesCCprocessed();
	}
}
