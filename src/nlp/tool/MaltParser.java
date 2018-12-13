package nlp.tool;


import nlp.ds.Sentence;
import nlp.ds.Word;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;

import qa.Globals;

public class MaltParser {
	private MaltParserService service = null;
	public MaltParser() {
		try
		{
			System.out.print("Loading MaltParser ...");
			service = new MaltParserService();
			// Inititalize the parser model 'model0' and sets the working directory to '.' and sets the logging file to 'parser.log'
			//service.initializeParserModel("-c engmalt.linear-1.7 -m parse -w . -lfi parser.log");
			service.initializeParserModel("-c engmalt.linear-1.7 -m parse -w "+Globals.localPath+"lib/maltparser-1.9.1 -lfi parser.log");
			firstParse();
			System.out.println("ok!");
		} catch (MaltChainedException e) {
			e.printStackTrace();
			System.err.println("MaltParser exception: " + e.getMessage());
		}
	}
	
	private void firstParse() {
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
			service.parse(tokens);
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
	}
	
	public DependencyStructure getDependencyStructure (Sentence sentence) {
		try {
			return service.parse(getTaggedTokens(sentence));
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
