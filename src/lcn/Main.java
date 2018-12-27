package lcn;

import java.util.ArrayList;
import java.util.Scanner;

import qa.Globals;
import qa.mapping.EntityFragmentDict;


public class Main {
	//Test: searching Entities and Types through Lucene Index.
	public static void main(String[] aStrings) throws Exception{
		
		//SearchInLiteralSubset se = new SearchInLiteralSubset();
		SearchInTypeShortName st = new SearchInTypeShortName();
		SearchInEntityFragments sf = new SearchInEntityFragments();
		EntityFragmentDict  efd = new EntityFragmentDict();
		EntityFragmentFields eff = null;
		Globals.localPath = "D:/husen/gAnswer/";
		Scanner sc = new Scanner(System.in);
		System.out.print("input name: ");
		
		while(sc.hasNextLine())
		{	
			String literal = sc.nextLine();
			System.out.println(literal);
			
			//literal = cnlp.getBaseFormOfPattern(literal);
			
//search Type	
			ArrayList<String> result = st.searchType(literal, 0.4, 0.8, 10);
			System.out.println("TypeShortName-->RESULT:");
			for (String s : result) {
				System.out.println("<"+s + ">");
			}

//search Ent Fragment
//			int eId = EntityFragmentFields.entityName2Id.get(literal);
//			EntityFragment ef = EntityFragment.getEntityFragmentByEntityId(eId);
//			System.out.println(ef);

//search Ent Name
//			ArrayList<EntityNameAndScore> result = sf.searchName(literal, 0.4, 0.8, 50);
//			System.out.println("EntityName-->RESULT:");
//			for(EntityNameAndScore enas: result)
//			{
//				System.out.println(enas);
//			}
			
			System.out.print("input name: ");
		}
		sc.close();
	}	

}
