package qa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lcn.EntityFragmentFields;
import fgmt.RelationFragment;
import fgmt.TypeFragment;
import paradict.ParaphraseDictionary;
import nlp.tool.StanfordParser;
import nlp.tool.StopWordsList;

public class Globals {
	// nlp tools
	public static StanfordParser stanfordParser;
	public static StopWordsList stopWordsList;
	// relation paraphrase dictionary
	public static ParaphraseDictionary pd;
	// entity linking system
	public static int MaxAnswerNum = 100;
	public static String Dataset = "pkubase";
	public static String Version = "0.1.2";
	public static String GDBsystem = "gStore v0.7.2";
	
	/*
	 * evaluationMethod:
	 * 1. baseline(SQG), does not allow CIRCLE and WRONG edge. The structure may be different by changing the TARGET.
	 * 2. super SQG, allow CIRCLE and WRONG edge. The structure is decided by DS tree, and can be changed in query evaluation(TOP-K match) stage. 
	 * */
	public static int evaluationMethod = 2; 
	
	public static String localPath = "./././";
	public static String QueryEngineIP = "pkubase.gstore-pku.com";	// Notice, PORT number is in the evaluation function.
	public static int QueryEnginePort = 80;
	
	public static void init () 
	{
		System.out.println("====== gAnswer2.0 over Pkubase ======");

		long t1, t2, t3, t4, t5, t6, t7, t8, t9;
		
		t1 = System.currentTimeMillis();
		stanfordParser = new StanfordParser();
		
		t2 = System.currentTimeMillis();
		stopWordsList = new StopWordsList();
		
		t3 = System.currentTimeMillis();
		pd = new ParaphraseDictionary();
		
		t4 = System.currentTimeMillis();
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
		
		t5 = System.currentTimeMillis();
		System.out.println("======Initialization======");
		System.out.println("StanfordParser: " + (t2-t1) + "ms.");
		System.out.println("StopWordsList: " + (t3-t2) + "ms.");
		System.out.println("ParaphraseDict: " + (t4-t3) + "ms.");
		System.out.println("GraphFragments: " + (t5-t4) + "ms.");
		System.out.println("* Total *: " + (t5-t1) + "ms.");
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
