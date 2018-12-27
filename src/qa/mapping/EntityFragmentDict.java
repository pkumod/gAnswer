package qa.mapping;

import java.util.HashMap;

import fgmt.EntityFragment;

public class EntityFragmentDict {
	//public HashMap<String, EntityFragment> entityFragmentDictionary = new HashMap<String, EntityFragment>();
	public HashMap<Integer, EntityFragment> entityFragmentDictionary = new HashMap<Integer, EntityFragment>();
	
	public EntityFragment getEntityFragmentByEid (Integer eid) 
	{
		if (!entityFragmentDictionary.containsKey(eid)) 
		{
			entityFragmentDictionary.put(eid, EntityFragment.getEntityFragmentByEntityId(eid));
		}
		return entityFragmentDictionary.get(eid);

	}
	
	/*
	 * Old version, search by name
	 * */
//	public EntityFragment getEntityFragmentByName (String name) {
//		if (name.startsWith("?")) {
//			return null;
//		}
//		if (!entityFragmentDictionary.containsKey(name)) {
//			String fgmt = EntityFragment.getEntityFgmtStringByName(name);
//			if (fgmt != null) 
//			{
//				int eid = EntityFragmentFields.entityName2Id.get(name);
//				entityFragmentDictionary.put(name, new EntityFragment(eid, fgmt));
//			}
//			else {
//				entityFragmentDictionary.put(name, null);
//			}
//		}
//		return entityFragmentDictionary.get(name);
//
//	}
}
