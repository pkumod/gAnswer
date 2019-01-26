package qa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lcn.EntityFragmentFields;
import fgmt.RelationFragment;
import fgmt.TypeFragment;
import paradict.ParaphraseDictionary;
import qa.mapping.DBpediaLookup;
import nlp.tool.NERecognizer;
import nlp.tool.CoreNLP;
import nlp.tool.MaltParser;
import nlp.tool.StanfordParser;
import nlp.tool.StopWordsList;

public class Globals {
	// nlp tools
	public static CoreNLP coreNLP;
	public static StanfordParser stanfordParser;
	public static StopWordsList stopWordsList;
	public static MaltParser maltParser;
	public static NERecognizer nerRecognizer;
	// relation paraphrase dictionary
	public static ParaphraseDictionary pd;
	// entity linking system
	public static DBpediaLookup dblk;
	public static int MaxAnswerNum = 100;
	public static String Dataset = "dbpedia 2016";
	public static String Version = "0.1.2";
	public static String GDBsystem = "gStore v0.7.2";
	
	/*
	 * evaluationMethod:
	 * 1. baseline(SQG), does not allow CIRCLE and WRONG edge. The structure may be different by changing the TARGET.
	 * 2. super SQG, allow CIRCLE and WRONG edge. The structure is decided by DS tree, and can be changed in query evaluation(TOP-K match) stage. 
	 * */
	public static int evaluationMethod = 2; 
	
	public static String localPath = "./././";
	public static String QueryEngineIP = "dbpedia16.gstore-pku.com";	// Notice, PORT number is in the evaluation function.
	public static int QueryEnginePort = 80;
	
	public static void init () 
	{
		System.out.println("====== gAnswer2.0 over DBpedia ======");

		long t1, t2, t3, t4, t5, t6, t7, t8, t9;
		
		t1 = System.currentTimeMillis();
		coreNLP = new CoreNLP();
		
		t2 = System.currentTimeMillis();
		stanfordParser = new StanfordParser();
		
		t3 = System.currentTimeMillis();
		maltParser = new MaltParser();
		
		t4 = System.currentTimeMillis();
		nerRecognizer = new NERecognizer();
		
		t5 = System.currentTimeMillis();
		stopWordsList = new StopWordsList();
		
		t6 = System.currentTimeMillis();
		pd = new ParaphraseDictionary();
		
		t7 = System.currentTimeMillis();
		try 
		{	
			EntityFragmentFields.load();
			RelationFragment.load();
			TypeFragment.load();
		} 
		catch (Exception e1) {
			System.out.println("EntityIDs and RelationFragment and TypeFragment loading error!");
			e1.printStackTrace();
		}
		
		t8 = System.currentTimeMillis();
		dblk = new DBpediaLookup();
		
		t9 = System.currentTimeMillis();
		System.out.println("======Initialization======");
		System.out.println("CoreNLP(Lemma): " + (t2-t1) + "ms.");
		System.out.println("StanfordParser: " + (t3-t2) + "ms.");
		System.out.println("MaltParser: " + (t4-t3) + "ms.");
		System.out.println("NERecognizer: " + (t5-t4) + "ms.");
		System.out.println("StopWordsList: " + (t6-t5) + "ms.");
		System.out.println("ParaphraseDict & posTagPattern: " + (t7-t6) + "ms.");
		System.out.println("GraphFragments: " + (t8-t7) + "ms.");
		System.out.println("DBpediaLookup: " + (t9-t8) + "ms.");
		System.out.println("* Total *: " + (t9-t1) + "ms.");
		System.out.println("==========================");
	}

	
	/**
	 * Use as system("pause") in C
	 */
	public static void systemPause () {
		System.out.println("System pause ...");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
