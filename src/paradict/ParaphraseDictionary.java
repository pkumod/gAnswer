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
import java.util.List;

import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;

import qa.Globals;
import qa.extract.EntityRecognitionCh;

public class ParaphraseDictionary {
	public static String relation_paraphrases_path;
	public static String predicate_id_path;
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
	
	public ParaphraseDictionary () {
		String fixedPath = Globals.localPath+"data/pkubase/";

		System.out.println(System.getProperty("user.dir"));
		relation_paraphrases_path = fixedPath + "paraphrase/pkubase-paraphrase.txt";
		predicate_id_path = fixedPath + "fragments/id_mappings/pkubase_predicate_id.txt";
		
		bannedTypes = new HashSet<String>();
		
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
		
		prepositions = new HashSet<String>(); //TODO: safe delete

		try {
			loadPredicateId();
			addPredicateAsNLPattern();
			addHandwriteAsNLPattern();
//			loadDboPredicate();
//			loadParaDict();
			buildInvertedIndex();
			typePredicateID = predicate_2_id.get("类型"); 
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
				
		File file = new File(predicate_id_path);
		InputStreamReader in = null;
		BufferedReader br = null;
		try{
			in = new InputStreamReader(new FileInputStream(file), "utf-8");
			br = new BufferedReader(in);
			String line = null;
			while ((line = br.readLine())!= null) {
			String[] lines = line.split("\t");
			if(lines[0].startsWith("<") && lines[0].endsWith(">"))
				lines[0] = lines[0].substring(1, lines[0].length()-1);
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
			in = new InputStreamReader(new FileInputStream(new File(relation_paraphrases_path)), "utf-8");
			br = new BufferedReader(in);
			String line = null;
			int lineCount = 0;
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
		if(nlPattern_2_predicateList == null)
			nlPattern_2_predicateList = new HashMap<String, ArrayList<PredicateIDAndSupport>>();
		
		final int support = 200;
		int predicate_id;
		for (String p : predicate_2_id.keySet()) 
		{
			predicate_id = predicate_2_id.get(p);

			// TODO: segmentation: 1) tokenize 2) single ch-word
			String patternString = "";
			List<SegToken> q=EntityRecognitionCh.segmenter.process(p, SegMode.SEARCH);
			for (SegToken t:q)
			{
				patternString += t.word + " ";
			}
			patternString = patternString.trim();
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
		InputStreamReader in = null;
		BufferedReader br = null;
		
		try{
			in = new InputStreamReader(new FileInputStream(new File(relation_paraphrases_path)), "utf-8");
			br = new BufferedReader(in);
			
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#") || line.isEmpty()) continue;

				String[] content = line.split("\t");
				
				if(!predicate_2_id.containsKey(content[0]))
					continue;
				
				int predicateID = predicate_2_id.get(content[0]);
				String nlPattern = content[1];
				int support = Integer.parseInt(content[2]);
				
				// Need Segmentation
				if(!nlPattern.contains(" "))
				{
					String patternString = "";
					List<SegToken> q=EntityRecognitionCh.segmenter.process(nlPattern, SegMode.SEARCH);
					for (SegToken t:q)
					{
						patternString += t.word + " ";
					}
					patternString = patternString.trim();
					nlPattern = patternString;
				}
				
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
//		Globals.coreNLP = new CoreNLP();
		Globals.pd = new ParaphraseDictionary();
		//Globals.pd.showNLPatterns();
	}
}
