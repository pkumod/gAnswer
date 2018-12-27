package addition;

import nlp.ds.DependencyTree;
import nlp.ds.DependencyTreeNode;
import nlp.ds.Word;
import qa.Globals;
import rdf.Sparql;
import rdf.Triple;
import log.QueryLogger;

public class AggregationRecognition {

	// Numbers
    static String x[]={"zero","one","two","three","four","five","six","seven","eight","nine"};
	static String y[]={"ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen"};
	static String z[]={"twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"};
	static int b;

    public static Integer translateNumbers(String str) // 1~100
    {
    	int flag;
    	try {		
    	     b=Integer.valueOf(str);
    	     flag=1;
    	} 
    	catch (Exception e){
    	      flag=2;
    	}
    	int i,j;
    	switch(flag)
    	{
			case 1:
				return b;          		
			case 2:	                     // Words need to be translated into numbers
			   for(i=0;i<8;i++)                // 20~99
			    {
			    	for(j=0;j<10;j++)
			    	{
			    		String str1=z[i],str2=x[j];
			    		if(str.equals((str1))){     
			    			return i*10+20; // 1x   		
			    	    }       
			    		           		
			    		else if(str.equals((str1+" "+str2))){
			    			return i*10+j+20;
			            }     
			        }
			    }
			   
				for(i=0;i<10;i++){             
					if(str.equals(x[i])){
						return i;
			     	}            	
			     	else if(str.equals(y[i])){
			     		return 10+i;
			     	}                	
				} 
				
				System.out.println("Warning: Can not Translate Number: " + str);
		 }
    	return 1;
    }

	
	public void recognize(QueryLogger qlog)
	{
		DependencyTree ds = qlog.s.dependencyTreeStanford;
		if(qlog.isMaltParserUsed)
			ds = qlog.s.dependencyTreeMalt;
		
		Word[] words = qlog.s.words;
		
		// how often | how many
		if(qlog.s.plainText.indexOf("How many")!=-1||qlog.s.plainText.indexOf("How often")!=-1||qlog.s.plainText.indexOf("how many")!=-1||qlog.s.plainText.indexOf("how often")!=-1)
		{
			for(Sparql sp: qlog.rankedSparqls)
			{
				sp.countTarget = true;
				//  How many pages does War and Peace have? --> res:War_and_Peace dbo:numberOfPages ?n . 
				//	 ?uri dbo:populationTotal ?inhabitants . 
				for(Triple triple: sp.tripleList)
				{
					String p = Globals.pd.getPredicateById(triple.predicateID).toLowerCase();
					if(p.contains("number") || p.contains("total") || p.contains("calories") || p.contains("satellites"))
					{
						sp.countTarget = false;
					}
				}
			}
		}
		
		// more than [num] [node]
		for(DependencyTreeNode dtn: ds.nodesList)
		{
			if(dtn.word.baseForm.equals("more"))
			{
				if(dtn.father!=null && dtn.father.word.baseForm.equals("than"))
				{
					DependencyTreeNode tmp = dtn.father;
					if(tmp.father!=null && tmp.father.word.posTag.equals("CD") && tmp.father.father!=null && tmp.father.father.word.posTag.startsWith("N"))
					{
						DependencyTreeNode target = tmp.father.father;
						
						// Which caves have more than 3 entrances | entranceCount | filter
						for(Sparql sp: qlog.rankedSparqls)
						{
							if(target.father !=null && target.father.word.baseForm.equals("have"))
							{
								sp.moreThanStr = "GROUP BY ?" + qlog.target.originalForm + "\nHAVING (COUNT(?"+target.word.originalForm + ") > "+tmp.father.word.baseForm+")";
							}
							else
							{
								int num = translateNumbers(tmp.father.word.baseForm);
								sp.moreThanStr = "FILTER (?"+target.word.originalForm+"> " + num + ")";
							}
						}
					}
				}
			}
		}
		
		// most
		for(Word word: words)
		{
			if(word.baseForm.equals("most"))
			{
				Word modifiedWord = word.modifiedWord;
				if(modifiedWord != null)
				{
					for(Sparql sp: qlog.rankedSparqls)
					{
						//  Which Indian company has the most employees? --> ... dbo:numberOfEmployees ?n . || ?employees dbo:company ...
						sp.mostStr = "ORDER BY DESC(COUNT(?"+modifiedWord.originalForm+"))\nOFFSET 0 LIMIT 1";
						for(Triple triple: sp.tripleList)
						{
							String p = Globals.pd.getPredicateById(triple.predicateID).toLowerCase();
							if(p.contains("number") || p.contains("total"))
							{
								sp.mostStr = "ORDER BY DESC(?"+modifiedWord.originalForm+")\nOFFSET 0 LIMIT 1";
							}
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println(translateNumbers("Twelve"));
		System.out.println(translateNumbers("thirty two"));
	}

}
