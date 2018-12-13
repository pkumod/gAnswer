package lcn;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import qa.Globals;
import fgmt.TypeFragment;

public class BuildIndexForTypeShortName {
	public static void buildIndex(HashMap<String, ArrayList<Integer>> typeShortName2IdList) throws Exception
	{
		long startTime = new Date().getTime();
		File indexDir_li = new File("D:/husen/gAnswer/data/DBpedia2016/lucene/type_fragment_index");
		
		Analyzer luceneAnalyzer_li = new StandardAnalyzer();  
		IndexWriter indexWriter_li = new IndexWriter(indexDir_li, luceneAnalyzer_li,true); 
		
		int mergeFactor = 100000;
		int maxBufferedDoc = 1000;
		int maxMergeDoc = Integer.MAX_VALUE;
		
		//indexWriter.DEFAULT_MERGE_FACTOR = mergeFactor;
		indexWriter_li.setMergeFactor(mergeFactor);
		indexWriter_li.setMaxBufferedDocs(maxBufferedDoc);
		indexWriter_li.setMaxMergeDocs(maxMergeDoc);
		
		int count = 0;
		Iterator<String> it = typeShortName2IdList.keySet().iterator();
		while (it.hasNext()) 
		{
			String sn = it.next();
			if (sn.length() == 0) {
				continue;
			}
			
			count ++;
		
			StringBuilder splittedSn = new StringBuilder("");
			
			if(sn.contains("_"))
			{
				String nsn = sn.replace("_", " ");
				splittedSn.append(nsn.toLowerCase());
			}
			else
			{
				int last = 0, i = 0;
				for(i = 0; i < sn.length(); i ++) 
				{
					// if it were not a small letter, then break it.
					if(!(sn.charAt(i)>='a' && sn.charAt(i)<='z')) 
					{
						splittedSn.append(sn.substring(last, i).toLowerCase());
						splittedSn.append(' ');
						last = i;
					}
				}
				splittedSn.append(sn.substring(last, i).toLowerCase());
				while(splittedSn.charAt(0) == ' ') {
					splittedSn.deleteCharAt(0);
				}
			}
			
			System.out.println("SplitttedType: "+splittedSn);
			
			Document document = new Document(); 

			Field SplittedTypeShortName = new Field("SplittedTypeShortName", splittedSn.toString(), 
					Field.Store.YES,
					Field.Index.TOKENIZED,
					Field.TermVector.WITH_POSITIONS_OFFSETS);			
			Field TypeShortName = new Field("TypeShortName", sn,
					Field.Store.YES, Field.Index.NO);
			
			document.add(SplittedTypeShortName);
			document.add(TypeShortName);
			indexWriter_li.addDocument(document);	
		}
				
		indexWriter_li.optimize();
		indexWriter_li.close();

		// input the time of Build index
		long endTime = new Date().getTime();
		System.out.println("TypeShortName index has build ->" + count + " " + "Time:" + (endTime - startTime));
	}
	
	public static void main (String[] args) {
		try {
			Globals.localPath="D:/husen/gAnswer/";
			TypeFragment.load();
			BuildIndexForTypeShortName.buildIndex(TypeFragment.typeShortName2IdList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
