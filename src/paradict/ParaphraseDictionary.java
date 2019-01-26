package paradict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;




import nlp.tool.CoreNLP;
import qa.Globals;

public class ParaphraseDictionary {
	public static String localDataPath;
	public static String dbpedia_relation_paraphrases_baseform_withScore;
	public static String dbpedia_relation_paraphrases_baseform_withScore_rerank;
	public static String dbpedia_relation_paraphrases_handwrite;
	public static String dbpedia_predicate_id;
	public static String dbpedia_dbo_predicate;

	public HashMap<String, Integer> predicate_2_id = null;
	public HashMap<Integer, String> id_2_predicate = null;
	public HashSet<Integer> dbo_predicate_id = null;
	public HashMap<String, ArrayList<PredicateIDAndSupport>> nlPattern_2_predicateList = null;
	public HashMap<String, ArrayList<String>> invertedIndex = null;
	
	public HashSet<String> relns_subject;
	public HashSet<String> relns_object;
	public HashSet<String> prepositions;
	public HashSet<String> bannedTypes;
	
	public int typePredicateID = 5157;	//Dbpedia 2016 <type>=5157 | It will be updated according the dataset
	public int totalPredCount = 0;
	public int paraphrasedPredCount = 0;
	public int lineCount = 0;
	
	/**
	 * constructor
	 * @param parser
	 * @param ner
	 */
	public ParaphraseDictionary () {
		String fixedPath = Globals.localPath;

		System.out.println(System.getProperty("user.dir"));
		localDataPath = fixedPath + "data/DBpedia2016/parapharse/";
		dbpedia_relation_paraphrases_baseform_withScore_rerank = localDataPath + "dbpedia-relation-paraphrases-withScore-baseform-merge-sorted-rerank-slct.txt";
		dbpedia_relation_paraphrases_handwrite = localDataPath + "dbpedia-relation-paraphrase-handwrite.txt";
		
		dbpedia_predicate_id = localDataPath + "16predicate_id.txt";
		dbpedia_dbo_predicate = localDataPath + "16dbo_predicates.txt";
		
		bannedTypes = new HashSet<String>();
		bannedTypes.add("Mayor");
		
		relns_subject = new HashSet<String>();
		relns_subject.add("subj");
		relns_subject.add("csubjpass");
		relns_subject.add("csubj");
		relns_subject.add("xsubj");
		relns_subject.add("nsubjpass");
		relns_subject.add("nsubj");
		relns_subject.add("poss");	// Obama's wife
		relns_subject.add("dobj");		
		
		relns_object = new HashSet<String>();
		relns_object.add("dobj");
		relns_object.add("iobj");
		relns_object.add("obj");
		relns_object.add("pobj");
		
		prepositions = new HashSet<String>();
		prepositions.add("in");//in at on with to from before after of for
		prepositions.add("at");
		prepositions.add("on");
		prepositions.add("with");
		prepositions.add("to");
		prepositions.add("from");
		prepositions.add("before");
		prepositions.add("after");
		prepositions.add("of");
		prepositions.add("for");
		prepositions.add("as");

		try {
			loadPredicateId();
			loadDboPredicate();
			loadParaDict();
			buildInvertedIndex();
			typePredicateID = predicate_2_id.get("type"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load the mapping between predicates and their IDs.
	 * @throws IOException
	 */
	public void loadPredicateId () throws IOException {
		predicate_2_id = new HashMap<String, Integer>();
		id_2_predicate = new HashMap<Integer, String>();
				
		String input_filename = dbpedia_predicate_id;
		File file = new File(input_filename);
		InputStreamReader in = null;
		BufferedReader br = null;
		try{
			in = new InputStreamReader(new FileInputStream(file), "utf-8");
			br = new BufferedReader(in);
			String line = null;
			while ((line = br.readLine())!= null) {
			String[] lines = line.split("\t");
			predicate_2_id.put(lines[0], Integer.parseInt(lines[1]));
			id_2_predicate.put(Integer.parseInt(lines[1]), lines[0]);
		}	
		}catch(IOException e){
			System.out.println("NLPatterns.loadPredicateId() : IOException!");
			e.printStackTrace();
		}finally{
			if(br != null){
				try{
					br.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		System.out.println("NLPatterns.loadPredicateId() : ok!");
	}
	
	public void loadDboPredicate() throws IOException
	{
		dbo_predicate_id = new HashSet<Integer>();
		int cnt = 0;
		
		String input_filename = dbpedia_dbo_predicate;
		InputStreamReader in = null;
		BufferedReader br = null;
		try{
			File file = new File(input_filename);
			in = new InputStreamReader(new FileInputStream(file), "utf-8");
			br = new BufferedReader(in);
			String line = null;
			while ((line = br.readLine())!= null) 
			{
				if (!predicate_2_id.containsKey(line))
				{
					cnt++;
					//System.out.println("error: not found "+line+" id.");
					continue;
				}	
				dbo_predicate_id.add(predicate_2_id.get(line));
			}	
		}catch(IOException e){
			System.out.println("NLPatterns.loadDboPredicate() : IOException!");
			
		}finally{
			if(br!=null){
				try{
					br.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			
		}
		System.out.println("Warning: DBO not found id count: "+cnt);
		System.out.println("NLPatterns.loadDboPredicate() : ok!");
	}
	
	/**
	 * Get predicate by its id
	 * @param predicateID
	 * @return
	 */
	public String getPredicateById (int predicateID) {
		return id_2_predicate.get(predicateID);
	}
	
	public void loadParaDict () throws Exception {
		nlPattern_2_predicateList = new HashMap<String, ArrayList<PredicateIDAndSupport>>();
		HashSet<String> missInDBP2014 = new HashSet<String>();
		
		InputStreamReader in = null;
		BufferedReader br = null;
		try{
			String inputFileName = dbpedia_relation_paraphrases_baseform_withScore_rerank;
			File file = new File(inputFileName);
			in = new InputStreamReader(new FileInputStream(file), "utf-8");
			br = new BufferedReader(in);
			String line = null;
			int lineCount = 0;
			//line = br.readLine();//read the first line which indicates the format
			while ((line = br.readLine()) != null) 
			{
				if (line.startsWith("#")) continue;
				lineCount ++;
				String[] content = line.split("\t");
				
				if(!predicate_2_id.containsKey(content[0]))
				{
					missInDBP2014.add(content[0]);
					continue;
				}
				
				int predicateID = predicate_2_id.get(content[0]);
				String nlPattern = content[1].toLowerCase();
				int support = Integer.parseInt(content[2]);
				//double score = Double.parseDouble(content[3]);
				String []slctString = content[3].split(" ");
				double[] slct = new double[slctString.length];
				for (int i=0; i < slct.length; i++) {
					slct[i] = Double.parseDouble(slctString[i]);
				}
				
				if (!nlPattern_2_predicateList.containsKey(nlPattern)) {
					nlPattern_2_predicateList.put(nlPattern, new ArrayList<PredicateIDAndSupport>());
				}
				nlPattern_2_predicateList.get(nlPattern).add(new PredicateIDAndSupport(predicateID, support, slct));
			}
			
			System.out.println("Number of NL-Patterns-to-predicate mappings = " + lineCount);
			System.out.println("NLPatterns.size = " + nlPattern_2_predicateList.size());
			System.out.println("Predicate.size = " + predicate_2_id.size());
			System.out.println("Warning: Predicates not in DBpedia 2014 count: "+missInDBP2014.size());

			// Notice predicate itself and handwritten patterns have no wordSelectivity.
			addPredicateAsNLPattern(); // This is very important. 
			addHandwriteAsNLPattern();
			
			Iterator<String> it = nlPattern_2_predicateList.keySet().iterator();
			while (it.hasNext()) {
				Collections.sort(nlPattern_2_predicateList.get(it.next()));
			}
			
		}catch(IOException e){
			System.out.println("NLPatterns.Paradict() : IOException!");
		}finally{
			if(br!=null){
				try{
					br.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		System.out.println("NLPatterns.Paradict() : ok!");
	}
	
	/**
	 * A set of very important NL patterns are the predicates themselves!
	 */
	public void addPredicateAsNLPattern () {
		final int support = 200;
		int predicate_id;
		for (String p : predicate_2_id.keySet()) 
		{
			// TODO: Omitting some bad relations (should be discarded in future)
			if(p.equals("state") || p.equals("states"))
				continue;
			
			predicate_id = predicate_2_id.get(p);
			StringBuilder pattern = new StringBuilder("");
			
			// Work/runtime	11,SpaceStation/volume	68 and some predicates have prefix (DBpedia 2015), discard the prefix when generating pattern
			if(p.contains("/"))
			{
				if(p.charAt(0)>='A' && p.charAt(0)<='Z')
					p = p.substring(p.indexOf("/")+1);
				//gameW/l	1974
				else
					p = p.replace("/", "");
			}
			
			int last = 0, i = 0;
			for(i = 0; i < p.length(); i ++) {
				// if it were not a small letter, then break it.
				if(!(p.charAt(i)>='a' && p.charAt(i)<='z')) {
					pattern.append(p.substring(last, i).toLowerCase());
					pattern.append(" ");
					last = i;
				}
			}
			pattern.append(p.substring(last, i).toLowerCase());
			for (i = 3; i < pattern.length(); i ++) {
				// the blank between two digits should be deleted.
				if (pattern.charAt(i)>='0' && pattern.charAt(i)<='9'
					&& pattern.charAt(i-1)==' '
					&& pattern.charAt(i-2)>='0' && pattern.charAt(i-2)<='9') {
					pattern.deleteCharAt(i-1);
				}
				// the blank between I and D should be deleted.
				else if (pattern.charAt(i)=='d'
					&& pattern.charAt(i-1)==' '
					&& pattern.charAt(i-2)=='i'
					&& pattern.charAt(i-3)==' ') {
					pattern.deleteCharAt(i-1);
				}
				// the blank between D and B should be deleted.
				else if (pattern.charAt(i)=='b'
					&& pattern.charAt(i-1)==' '
					&& pattern.charAt(i-2)=='d'
					&& pattern.charAt(i-3)==' ') {
					pattern.deleteCharAt(i-1);
				}
			}
			
			// pattern -> base form
			/*String[] ptns = pattern.toString().split(" ");
			pattern = new StringBuilder("");
			for (String s : ptns) {
				pattern.append(Globals.coreNLPparser.getBaseFormOfPattern(s));
				pattern.append(" ");
			}
			pattern.deleteCharAt(pattern.length()-1);
			String patternString = pattern.toString();*/
			
			// Special case cannot use base form, eg, foundingYear	//TODO: maybe Porter's Algorithm
			String patternString = Globals.coreNLP.getBaseFormOfPattern(pattern.toString());
			//System.out.println(p + "-->" + patternString);
			
			if (!nlPattern_2_predicateList.containsKey(patternString)) {
				nlPattern_2_predicateList.put(patternString, new ArrayList<PredicateIDAndSupport>());
			}
			nlPattern_2_predicateList.get(patternString).add(
					new PredicateIDAndSupport(predicate_id, 
							support, 
							PredicateIDAndSupport.genSlct(patternString.split(" ").length)));
		}
		
		System.out.println("NLPatterns.addPredicateAsNLPattern(): ok!");
	}
	
	public void addHandwriteAsNLPattern() throws IOException {
		String inputFileName = dbpedia_relation_paraphrases_handwrite;
		InputStreamReader in = null;
		BufferedReader br = null;
		
		try{
			File file = new File(inputFileName);
			in = new InputStreamReader(new FileInputStream(file), "utf-8");
			br = new BufferedReader(in);
			
			String line = null;
			//int lineCount = 0;
			//line = br.readLine();//read the first line which indicates the format
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#") || line.isEmpty()) continue;
				//lineCount ++;
				String[] content = line.split("\t");
				
				if(!predicate_2_id.containsKey(content[0]))
					continue;
				
				int predicateID = predicate_2_id.get(content[0]);
				String nlPattern = content[1].toLowerCase();
				int support = Integer.parseInt(content[2]);
				
				if (!nlPattern_2_predicateList.containsKey(nlPattern)) {
					nlPattern_2_predicateList.put(nlPattern, new ArrayList<PredicateIDAndSupport>());
				}
				nlPattern_2_predicateList.get(nlPattern).add(
						new PredicateIDAndSupport(predicateID, 
								support,
								PredicateIDAndSupport.genSlct(nlPattern.split(" ").length)));			
			}
		}catch(IOException e){
			System.out.println("NLPatterns.addHandwriteAsNLPattern(): IOException!");
		}finally{
			if(br!=null){
				try{
					br.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("NLPatterns.addHandwriteAsNLPattern(): ok!");
	}

	/**
	 * Show the NLPatterns
	 */
	public void showNLPatterns () {
		/*for (String s: syntacticMarker) {
			System.out.println(s);
		}
		GlobalTools.systemPause();*/
		
		System.out.println("predicate-->id");
		for (String s : predicate_2_id.keySet()) {
			System.out.println(s + "-->" + predicate_2_id.get(s));
		}
		Globals.systemPause();
		
		int count = 1;
		System.out.println("nlPattern-->predicate<support>");
		for (String p : nlPattern_2_predicateList.keySet()) {
			System.out.print("" + (count++) + ".\t" + p + "\t[" + nlPattern_2_predicateList.get(p).size() + "]\t");
			for (PredicateIDAndSupport i : nlPattern_2_predicateList.get(p)) {
				System.out.print(id_2_predicate.get(i.predicateID) + "<" + i.support + ">" + ", ");
			}
			System.out.println();
		}
	}
	
	/**
	 * Build the inverted index, where each word will be mapped to the patterns that it occurs
	 */
	public void buildInvertedIndex () {
		invertedIndex = new HashMap<String, ArrayList<String>>();
		// traversing all patterns
		for (String p : nlPattern_2_predicateList.keySet()) {
			String[] tokens = p.split(" ");
			for (String token : tokens) {
				if (token.length() < 1) continue;
				if (!invertedIndex.containsKey(token)) {
					invertedIndex.put(token, new ArrayList<String>());
				}
				invertedIndex.get(token).add(p);
			}
		}
		
		System.out.println("NLPatterns.buildInvertedIndex(): ok!");
	}
	
	public static void main (String[] args) {
		Globals.coreNLP = new CoreNLP();
		Globals.pd = new ParaphraseDictionary();
		//Globals.pd.showNLPatterns();
	}
}
