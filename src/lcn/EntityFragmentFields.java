package lcn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fgmt.EntityFragment;
import qa.Globals;
import utils.FileUtil;

public class EntityFragmentFields {
		
	// entity dictionary
	public static HashMap<String, Integer> entityName2Id = null;
	public static HashMap<Integer, String> entityId2Name = null;
	public static HashMap<Integer, String> entityFragmentString = null;
	
	public static void load() throws IOException 
	{
		String filename = Globals.localPath+"data/pkubase/fragments/id_mappings/pkubase_entity_id.txt";
		String fragmentFileName = Globals.localPath+"data/pkubase/fragments/pkubase_entity_fragment.txt";
		File file = new File(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));

		entityName2Id = new HashMap<String, Integer>();
		entityId2Name = new HashMap<Integer, String>();

		long t1, t2, t3;
		
		t1 = System.currentTimeMillis();
		// load entity id
		System.out.println("Loading entity id ...");
		String line;
		while((line = br.readLine()) != null) 
		{
			String[] lines = line.split("\t");
			String entName = lines[0].trim().substring(1, lines[0].length()-1);
	
			entityName2Id.put(entName, Integer.parseInt(lines[1]));	
			entityId2Name.put(Integer.parseInt(lines[1]), entName);
		}
		br.close();
		t2 = System.currentTimeMillis();
		System.out.println("Load "+entityId2Name.size()+" entity ids in "+ (t2-t1) + "ms.");
		
		// load entity fragment
		System.out.println("Loading entity fragments ...");
		br = new BufferedReader(new InputStreamReader(new FileInputStream(fragmentFileName),"utf-8"));
		entityFragmentString = new HashMap<Integer, String>();
		while((line = br.readLine()) != null)
		{
			String[] lines = line.split("\t");
			if(lines.length != 2)
				continue;
			int eId = Integer.parseInt(lines[0]);
			entityFragmentString.put(eId, lines[1]);
		}
		t3 = System.currentTimeMillis();
		System.out.println("Load "+entityFragmentString.size()+" entity fragments in "+ (t3-t2) + "ms.");
		
		br.close();
	}
	
	public static void genmini()
	{
		String filename = Globals.localPath+"data/pkubase/fragments/id_mappings/pkuentity_id.txt";
		String fragmentFileName = Globals.localPath+"data/pkubase/fragments/pkubase_entity_fragment_mini.txt";
		List<String> fragments = FileUtil.readFile(fragmentFileName);
		ArrayList<Integer> eids = new ArrayList<Integer>();
		for(String fragment: fragments)
		{
			int eid = Integer.parseInt(fragment.split("\t")[0]);
			String fgmt = fragment.split("\t")[1];
			EntityFragment ef = new EntityFragment(eid, fgmt);
			eids.add(eid);
			for(int ent: ef.inEntMap.keySet())
			{
				eids.add(ent);
			}
			for(int ent: ef.outEntMap.keySet())
			{
				eids.add(ent);
			}
		}
		System.out.println(eids.size());
		System.out.println("Loading entity id ...");
		List<String> data = FileUtil.readFile(filename);
		for(String line: data)
		{
			String[] lines = line.split("\t");
			int eid = Integer.parseInt(lines[1]);
			if(eids.contains(eid))
				System.out.println(line);
		}
	}
	
	public static void main(String[] args) {
		EntityFragmentFields.genmini();
	}
}
