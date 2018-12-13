package fgmt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import qa.Globals;
import utils.FileUtil;

public class RelationFragment extends Fragment 
{
	public static HashMap<Integer, ArrayList<RelationFragment>> relFragments = null;
	public static HashMap<String, ArrayList<Integer>> relationShortName2IdList = null;
	public static HashSet<Integer> literalRelationSet = null;
	
	public HashSet<Integer> inTypes = new HashSet<Integer>();
	public HashSet<Integer> outTypes = new HashSet<Integer>();
	
	public static final int literalTypeId = -176;
	
	public RelationFragment(String inFgmt, String outFgmt, int fid) 
	{
		fragmentId = fid;
		fragmentType = typeEnum.RELATION_FRAGMENT;
		String[] nums;
		
		// in
		nums = inFgmt.split(",");
		for(String s: nums) 
			if(s.length() > 0) 
				inTypes.add(Integer.parseInt(s));
		
		// out
		if(outFgmt.equals("itera"))
			outTypes.add(literalTypeId);
		else 
		{
			nums = outFgmt.split(",");
			for(String s: nums)
				if(s.length() > 0)
					outTypes.add(Integer.parseInt(s));		
		}
	}
	
	public static void load() throws Exception 
	{		
		String filename = Globals.localPath + "data/DBpedia2016/fragments/predicate_RDF_fragment/predicate_fragment.txt"; 
		List<String> inputs = FileUtil.readFile(filename);
		relFragments = new HashMap<Integer, ArrayList<RelationFragment>>();
		literalRelationSet = new HashSet<Integer>();
		
		for(String line: inputs)
		{
			String[] lines = line.split("\t");
			String inString = lines[0].substring(1, lines[0].length()-1);
			int pid = Integer.parseInt(lines[1]);
			String outString = lines[2].substring(1, lines[2].length()-1);
			
			// Record which relations can connect LITERAL objects.
			if(outString.equals("itera"))	// "literal".substring(1, length()-1)
				literalRelationSet.add(pid);
			
			if(!relFragments.containsKey(pid))
				relFragments.put(pid, new ArrayList<RelationFragment>());
			relFragments.get(pid).add(new RelationFragment(inString, outString, pid));
		}		

		loadId();
	}
	
	public static void loadId() throws IOException 
	{
		String filename = Globals.localPath + "data/DBpedia2016/fragments/id_mappings/16predicate_id.txt";
		List<String> inputs = FileUtil.readFile(filename);
		relationShortName2IdList = new HashMap<String, ArrayList<Integer>>();

		for(String line: inputs)
		{
			String[] lines = line.split("\t");
			String rlnShortName = lines[0];
			
			if (!relationShortName2IdList.containsKey(rlnShortName))
				relationShortName2IdList.put(rlnShortName, new ArrayList<Integer>());
			relationShortName2IdList.get(rlnShortName).add(Integer.parseInt(lines[1]));
		}
	}
	
	public static boolean isLiteral (String p) 
	{
		for (Integer i : relationShortName2IdList.get(p))
			if (literalRelationSet.contains(i)) 
				return true;
		return false;
	}
	
	public static boolean isLiteral (int pid) 
	{
		if (literalRelationSet.contains(pid)) 
			return true;
		else 
			return false;
	}
}
