package fgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import qa.Globals;


public class TypeFragment extends Fragment {

	public static HashMap<Integer, TypeFragment> typeFragments = null;
	public static HashMap<String, ArrayList<Integer>> typeShortName2IdList = null;
	public static HashMap<Integer, String> typeId2ShortName = null;
	public static final int NO_RELATION = -24232;
	
	public static HashSet<String> yagoTypeList = null;
	
	public HashSet<Integer> inEdges = new HashSet<Integer>();
	public HashSet<Integer> outEdges = new HashSet<Integer>();
	public HashSet<Integer> entSet = new HashSet<Integer>();
	
	/*
	 * Eliminate some bad YAGO Types which conflict with:
	 * 1, ENT: amazon、earth、the_hunger_game、sparkling_wine
	 * 2, TYPE: type
	 * 3, RELATION: flow、owner、series、shot、part、care
	 * 4, others: peace、vice
	 */
	public static ArrayList<String> stopYagoTypeList = null;
	static void loadStopYagoTypeList()
	{
		stopYagoTypeList = new ArrayList<String>();
		stopYagoTypeList.add("Amazon");
		stopYagoTypeList.add("Earth");
		stopYagoTypeList.add("TheHungerGames");
		stopYagoTypeList.add("SparklingWine");
		stopYagoTypeList.add("Type");
		stopYagoTypeList.add("Flow");
		stopYagoTypeList.add("Owner");
		stopYagoTypeList.add("Series");
		stopYagoTypeList.add("Shot");
		stopYagoTypeList.add("Part");
		stopYagoTypeList.add("Care");
		stopYagoTypeList.add("Peace");
		stopYagoTypeList.add("Vice");
		stopYagoTypeList.add("Dodo");
		stopYagoTypeList.add("CzechFilms");
		stopYagoTypeList.add("ChineseFilms");
	}
	
	public TypeFragment(String fgmt, int fid) 
	{
		fragmentId = fid;
		fragmentType = typeEnum.TYPE_FRAGMENT;
		
		fgmt = fgmt.replace('|', '#');
		String[] ss = fgmt.split("#");
		String[] nums;
		
		if (ss[0].length() > 0) {
			nums = ss[0].split(",");
			for(int i = 0; i < nums.length; i ++) {
				if (nums[i].length() > 0) {
					inEdges.add(Integer.parseInt(nums[i]));
				}
			}
		}
		else {
			inEdges.add(NO_RELATION);
		}

		if (ss.length > 1 && ss[1].length() > 0) {
			nums = ss[1].split(",");
			for(int i = 0; i < nums.length; i ++) {
				if (nums[i].length() > 0) {
					outEdges.add(Integer.parseInt(nums[i]));
				}
			}
		}
		else {
			outEdges.add(NO_RELATION);
		}		
		
		if(ss.length > 2 && ss[2].length() > 0)
		{
			nums = ss[2].split(",");
			for(int i = 0; i < nums.length; i ++) {
				if (nums[i].length() > 0) {
					entSet.add(Integer.parseInt(nums[i]));
				}
			}
		}
	}
	
	public static void load() throws Exception 
	{	
		String filename = Globals.localPath+"data/DBpedia2016/fragments/class_RDF_fragment/16type_fragment.txt"; 
		
		File file = new File(filename);
		InputStreamReader in = new InputStreamReader(new FileInputStream(file),"utf-8");
		BufferedReader br = new BufferedReader(in);

		typeFragments = new HashMap<Integer, TypeFragment>();
		
		System.out.println("Loading type IDs and Fragments ...");
		String line;
		while((line = br.readLine()) != null) {			
			String[] lines = line.split("\t");
			TypeFragment tfgmt = null;
			if(lines[0].length() > 0 && !lines[0].equals("literal")) {
				int tid = Integer.parseInt(lines[0]);
				try{tfgmt = new TypeFragment(lines[1], tid);}
				catch(Exception e){}
				
				
				typeFragments.put(tid, tfgmt);
			}
		}	
		
		br.close();
		
		// can fix some data there
		// load Type Id
		loadId();
		System.out.println("Load "+typeId2ShortName.size()+" basic types and "+yagoTypeList.size()+" yago types.");
	}
	
	public static void loadId() throws IOException 
	{
		String filename = Globals.localPath+"data/DBpedia2016/fragments/id_mappings/16basic_types_id.txt";
		String yagoFileName = Globals.localPath+"data/DBpedia2016/fragments/id_mappings/16yago_types_list.txt";

		File file = new File(filename);
		InputStreamReader in = new InputStreamReader(new FileInputStream(file),"utf-8");
		BufferedReader br = new BufferedReader(in);

		typeShortName2IdList = new HashMap<String, ArrayList<Integer>>();
		typeId2ShortName = new HashMap<Integer, String>();

		String line;
		while((line = br.readLine()) != null) {			
			String[] lines = line.split("\t");
			String typeShortName = lines[0];
			// reserve typeShortName's capitalization
			if (!typeShortName2IdList.containsKey(typeShortName)) {
				typeShortName2IdList.put(typeShortName, new ArrayList<Integer>());
			}
			typeShortName2IdList.get(typeShortName).add(Integer.parseInt(lines[1]));
			typeId2ShortName.put(Integer.parseInt(lines[1]), typeShortName);
		}
		
		// literalType
		typeShortName2IdList.put("literal_HRZ", new ArrayList<Integer>());
		typeShortName2IdList.get("literal_HRZ").add(RelationFragment.literalTypeId);
		typeId2ShortName.put(RelationFragment.literalTypeId, "literal_HRZ");
		
		br.close();
		
		//load YAGO types
		in = new InputStreamReader(new FileInputStream(yagoFileName),"utf-8");
		br = new BufferedReader(in);
		yagoTypeList = new HashSet<String>();
		while((line = br.readLine())!=null)
		{
			String[] lines = line.split("\t");
			String typeName = lines[0];
			yagoTypeList.add(typeName);
		}
		
		loadStopYagoTypeList();
		yagoTypeList.removeAll(stopYagoTypeList);
	}
}
