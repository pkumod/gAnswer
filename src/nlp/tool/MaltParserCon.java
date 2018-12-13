package nlp.tool;

import java.io.File;
import java.net.URL;

import nlp.ds.Sentence;
import nlp.ds.Word;

import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;
import org.maltparser.core.exception.MaltChainedException;
//import org.maltparser.core.syntaxgraph.DependencyStructure;


public class MaltParserCon {
	private ConcurrentMaltParserModel model = null;
	public ConcurrentDependencyGraph outputGraph = null;
	
	public MaltParserCon(){
		try{
			System.out.println("Loading Maltparser...\n");
			URL ModelURL = new File("output/engmalt.linear-1.7.mco").toURI().toURL();
			model = ConcurrentMaltParserService.initializeParserModel(ModelURL);
			firstTest();
			System.out.println("ok!\n");
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("MaltParser exception: " + e.getMessage());
		}
	}
	
	private void firstTest(){
		String[] tokens = new String[12];
		tokens[0] = "1\tIn\t_\tIN\tIN\t_"; 
		tokens[1] = "2\twhich\t_\tWDT\tWDT\t_";
		tokens[2] = "3\tmovies\t_\tNNS\tNNS\t_";
		tokens[3] = "4\tdirected\t_\tVBN\tVBN\t_";
		tokens[4] = "5\tby\t_\tIN\tIN\t_";
		tokens[5] = "6\tGarry\t_\tNNP\tNNP\t_";
		tokens[6] = "7\tMarshall\t_\tNNP\tNNP\t_";
		tokens[7] = "8\twas\t_\tVBD\tVBD\t_";
		tokens[8] = "9\tJulia\t_\tNNP\tNNP\t_";
		tokens[9] = "10\tRoberts\t_\tNNP\tNNP\t_";
		tokens[10] = "11\tstarring\t_\tVBG\tVBG\t_";
		tokens[11] = "12\t?\t_\t.\t.\t_";
		try {
			outputGraph = model.parse(tokens);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(outputGraph);
	}
	
	public ConcurrentDependencyGraph getDependencyStructure (Sentence sentence) {
		try {
			return model.parse(getTaggedTokens(sentence));
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String[] getTaggedTokens (Sentence sentence) {
		String[] ret = new String[sentence.words.length];
		int count = 0;
		for (Word w : sentence.words) {
			ret[count] = new String(""+w.position+"\t"+w.originalForm+"\t_\t"+w.posTag+"\t"+w.posTag+"\t_");
			count ++;
		}
		return ret;
	}
}
