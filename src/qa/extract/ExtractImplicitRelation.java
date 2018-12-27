package qa.extract;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import paradict.ParaphraseDictionary;
import qa.Globals;
import rdf.Sparql;
import rdf.Triple;
import rdf.ImplicitRelation;
import lcn.EntityFragmentFields;
import log.QueryLogger;
import fgmt.EntityFragment;
import fgmt.TypeFragment;
import nlp.ds.Word;
import nlp.tool.CoreNLP;

public class ExtractImplicitRelation {
	
	static final int SamplingNumber = 100;	// the maximum sampling number in calculation
	static final int k = 3;	// select top-k when many suitable relations; select top-k entities for a word
	
	/*
	 * Implicit Relations:
	 * eg, Which is the film directed by Obama and starred by a Chinese ?x
	 * 1. [What] is in a [chocolate_chip_cookie]  ?var + ent
	 * 2. What [country] is [Sitecore] from       ?type + ent	= [?var p ent + ?var<-type]
	 * 3. Czech movies | Chinese actor            ent + ?type
	 * 4. President Obama 						  type + ent
	 * 5. Andy Liu's Hero(film)					  ent + ent
	 * */
	public ExtractImplicitRelation()
	{
	}
	
	// Notice, it is usually UNNECESSARY for two constant, so we unimplemented this function. 
	// eg, "president Obama", "Andy Liu's Hero(film)".
	public ArrayList<Integer> getPrefferdPidListBetweenTwoConstant(Word w1, Word w2)
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		int w1Role = 0, w2Role = 0;	// 0:var	1:ent	2:type
		if(w1.mayEnt && w1.emList.size()>0)
			w1Role = 1;
		if(w1.mayType && w1.tmList.size()>0)
			w1Role = 2;
		if(w2.mayEnt && w2.emList.size()>0)
			w2Role = 1;
		if(w2.mayType && w2.tmList.size()>0)
			w2Role = 2;
			
		//Reject variables | two types
		if(w1Role == 0 || w2Role == 0 || (w1Role == 2 && w2Role == 2))
			return null;
		
		//ent1 & ent2
		//if(w1Role == 1 && w2Role == 1)
		//{
			//EntityFragment ef = null;
			// TODO: implement.
		//}
		
		return res;
	}
	
	public ArrayList<Triple> supplementTriplesByModifyWord(QueryLogger qlog)
	{
		ArrayList<Triple> res = new ArrayList<Triple>();
		ArrayList<Word> typeVariableList = new ArrayList<Word>();
		
		// Modifier 
		for(Word word: qlog.s.words)
		{
			if(word.modifiedWord != null && word.modifiedWord != word)
			{
				ArrayList<ImplicitRelation> irList = null;
				// ent -> typeVariable | eg, Chinese actor, Czech movies | TODO: consider more types of modifier
				if(word.mayEnt && word.modifiedWord.mayType)
				{
					typeVariableList.add(word.modifiedWord);
					int tId = word.modifiedWord.tmList.get(0).typeID; // select the top-1 type
					String tName = word.modifiedWord.originalForm;
					for(int i=0; i<k&&i<word.emList.size(); i++) // select the top-k entities
					{
						int eId = word.emList.get(i).entityID;
						String eName = word.emList.get(i).entityName;
						irList = getPrefferdPidListBetween_Entity_TypeVariable(eId, tId);
						
						if(irList!=null && irList.size()>0)
						{
							ImplicitRelation ir = irList.get(0);
							String subjName = null, objName = null;
							Word subjWord = null, objWord = null;
							if(ir.subjId == eId)
							{
								subjName = eName;
								objName = "?"+tName;
								subjWord = word;
								objWord = word.modifiedWord;
							}
							else
							{
								subjName = "?"+tName;
								objName = eName;
								subjWord = word.modifiedWord;
								objWord = word;
							}
							Triple triple = new Triple(ir.subjId, subjName, ir.pId, ir.objId, objName, null, ir.score, subjWord, objWord);
							res.add(triple);
							break;
						}
					}
				}
			}
		}
		
		if(qlog.rankedSparqls == null || qlog.rankedSparqls.size() == 0)
		{
			if(res != null && res.size() > 0)
			{
				Sparql spq = new Sparql();
				for(Triple t: res)
					spq.addTriple(t);
				
				// Add type info
				for(Word typeVar: typeVariableList)
				{
					Triple triple =	new Triple(Triple.VAR_ROLE_ID, "?"+typeVar.originalForm, Globals.pd.typePredicateID, Triple.TYPE_ROLE_ID, typeVar.tmList.get(0).typeName, null, 100);
					spq.addTriple(triple);
				}
				
				qlog.rankedSparqls.add(spq);
			}
		}
		else
		{
			// Supplement implicit relations (modified) for each SPARQL.
			for(Sparql spq: qlog.rankedSparqls)
			{
				for(Triple t: res)
					spq.addTriple(t);
			}
		}
		
		return res;
	}
	
	/*
	 * eg：Czech|ent movies|?type	Chinese|ent actor|?type
	 * type variable + entity -> entities belong to type + entity 
	 * */
	public ArrayList<ImplicitRelation> getPrefferdPidListBetween_Entity_TypeVariable(Integer entId, Integer typeId)
	{
		ArrayList<ImplicitRelation> res = new ArrayList<ImplicitRelation>();
		
		TypeFragment tf = TypeFragment.typeFragments.get(typeId);
		EntityFragment ef2 = EntityFragment.getEntityFragmentByEntityId(entId);
		if(tf == null || ef2 == null)
		{
			System.out.println("Error in getPrefferdPidListBetween_TypeVariable_Entity ：Type(" + 
					TypeFragment.typeId2ShortName.get(typeId) + ") or Entity(" + EntityFragmentFields.entityId2Name.get(entId) + ") no fragments.");
			return null;
		}
		
		// select entities belong to type, count relations | TODO: random select
		int samplingCnt = 0;
		HashMap<ImplicitRelation, Integer> irCount = new HashMap<ImplicitRelation, Integer>();
		for(int candidateEid: tf.entSet)
		{
			EntityFragment ef1 = EntityFragment.getEntityFragmentByEntityId(candidateEid);
			if(ef1 == null)
				continue;
			
			ArrayList<ImplicitRelation> tmp = getPrefferdPidListBetween_TwoEntities(ef1, ef2);
			if(tmp == null || tmp.size() == 0)
				continue;
			
			if(samplingCnt++ > SamplingNumber)
				break;
			
			for(ImplicitRelation ir: tmp)
			{
				if(ir.subjId == candidateEid)
					ir.setSubjectId(Triple.VAR_ROLE_ID);
				else if(ir.objId == candidateEid)
					ir.setObjectId(Triple.VAR_ROLE_ID);
				
				if(irCount.containsKey(ir))
					irCount.put(ir, irCount.get(ir)+1);
				else
					irCount.put(ir, 1);
			}
		}
		
		//sort, get top-k
		ByValueComparator bvc = new ByValueComparator(irCount);
		List<ImplicitRelation> keys = new ArrayList<ImplicitRelation>(irCount.keySet());
        Collections.sort(keys, bvc);
        for(ImplicitRelation ir: keys)
        {
        	res.add(ir);
        	if(res.size() >= k)
        		break;
        }
    	
		return res;
	}
	
	public ArrayList<ImplicitRelation> getPrefferdPidListBetween_Entity_TypeVariable(String entName, String typeName)
	{
		if(!TypeFragment.typeShortName2IdList.containsKey(typeName) || !EntityFragmentFields.entityName2Id.containsKey(entName))
			return null;
		return getPrefferdPidListBetween_Entity_TypeVariable(EntityFragmentFields.entityName2Id.get(entName), TypeFragment.typeShortName2IdList.get(typeName).get(0));
	}
	
	static class ByValueComparator implements Comparator<ImplicitRelation> {
        HashMap<ImplicitRelation, Integer> base_map;
  
        public ByValueComparator(HashMap<ImplicitRelation, Integer> base_map) {
            this.base_map = base_map;
        }
 
        public int compare(ImplicitRelation arg0, ImplicitRelation arg1) {
            if (!base_map.containsKey(arg0) || !base_map.containsKey(arg1))
                return 0;
            if (base_map.get(arg0) < base_map.get(arg1))
                return 1;
            else if (base_map.get(arg0) == base_map.get(arg1)) 
            	return 0;
            else 
                return -1;
        }
    }
	
	/*
	 * Notice, this function has not been used in fact.
	 * eg：[What] is in a [chocolate_chip_cookie]
	 * Just guess by single entity: select the most frequent edge.
	 * */
	public ArrayList<ImplicitRelation> getPrefferdPidListBetween_Entity_Variable(Integer entId, String var)
	{
		ArrayList<ImplicitRelation> res = new ArrayList<ImplicitRelation>();
		
		EntityFragment ef = null;
		ef = EntityFragment.getEntityFragmentByEntityId(entId);
		
		if(ef == null)
		{
			System.out.println("Error in getPrefferdPidListBetween_Entity_Variable: Entity No Fragments!");
			return null;
		}
			
		// find most frequent inEdge
		int pid = findMostFrequentEdge(ef.inEntMap, ef.inEdges);
		if(pid != -1)
			res.add(new ImplicitRelation(Triple.VAR_ROLE_ID, entId, pid, 100));
		
		// find most frequent outEdge
		pid = findMostFrequentEdge(ef.outEntMap, ef.outEdges);
		if(pid != -1)
			res.add(new ImplicitRelation(entId, Triple.VAR_ROLE_ID, pid, 100));
			
		return res;
	}
	
	public ArrayList<ImplicitRelation> getPrefferdPidListBetween_Entity_Variable(String entName, String var)
	{
		return getPrefferdPidListBetween_Entity_Variable(EntityFragmentFields.entityName2Id.get(entName), var);
	}
	
	public int findMostFrequentEdge(HashMap<Integer, ArrayList<Integer>> entMap, HashSet<Integer> edges)
	{
		int mfPredicateId = -1, maxCount = 0;
		HashMap<Integer, Integer> edgeCount = new HashMap<Integer, Integer>();
		for(int key: entMap.keySet())
		{
			for(int edge: entMap.get(key))
			{
				if(!edgeCount.containsKey(edge))
					edgeCount.put(edge, 1);
				else
					edgeCount.put(edge, edgeCount.get(edge)+1);
				if(maxCount < edgeCount.get(edge))
				{
					maxCount = edgeCount.get(edge);
					mfPredicateId = edge;
				}
			}
		}
		
		return mfPredicateId;
	}

	// Unnecessary.
	public ArrayList<ImplicitRelation> getPrefferdPidListBetween_TypeConstant_Entity(Integer typeId, Integer entId)
	{
		ArrayList<ImplicitRelation> res = new ArrayList<ImplicitRelation>();
		TypeFragment tf = TypeFragment.typeFragments.get(typeId);
		
		if(tf == null)
		{
			System.out.println("Error in getPrefferdPidListBetween_TypeConstant_Entity: Type No Fragments!");
			return null;
		}
			
		// subj : ent1
		if(tf.entSet.contains(entId))
		{
			ImplicitRelation  ir = new ImplicitRelation(entId, typeId, Globals.pd.typePredicateID, 100);
			res.add(ir);
		}
			
		return res;
	}
	
	public ArrayList<ImplicitRelation> getPrefferdPidListBetween_TwoEntities(String eName1, String eName2)
	{
		return getPrefferdPidListBetween_TwoEntities(EntityFragmentFields.entityName2Id.get(eName1), EntityFragmentFields.entityName2Id.get(eName2));
	}
	
	public ArrayList<ImplicitRelation> getPrefferdPidListBetween_TwoEntities(Integer eId1, Integer eId2)
	{	
		EntityFragment ef1 = null, ef2 = null;
		ef1 = EntityFragment.getEntityFragmentByEntityId(eId1);
		ef2 = EntityFragment.getEntityFragmentByEntityId(eId2);
		
		if(ef1 == null || ef2 == null)
		{
			System.out.println("Error in GetPrefferdPidListBetweenTwoEntities: Entity No Fragments!");
			return null;
		}
	
		return getPrefferdPidListBetween_TwoEntities(ef1,ef2);
	}
	
	public ArrayList<ImplicitRelation> getPrefferdPidListBetween_TwoEntities(EntityFragment ef1, EntityFragment ef2)
	{
		ArrayList<ImplicitRelation> res = new ArrayList<ImplicitRelation>();
		if(ef1 == null || ef2 == null)
			return null;
		
		int eId1 = ef1.eId;
		int eId2 = ef2.eId;
		
		// subj : ent1
		if(ef1.outEntMap.containsKey(eId2))
		{
			ArrayList<Integer> pidList = ef1.outEntMap.get(eId2);
			for(int pid: pidList)
			{
				// TODO: other score strategy 
				ImplicitRelation  ir = new ImplicitRelation(eId1, eId2, pid, 100);
				res.add(ir);
			}
		}
		// subj : ent2
		else if(ef2.outEntMap.containsKey(eId1))
		{
			ArrayList<Integer> pidList = ef2.outEntMap.get(eId1);
			for(int pid: pidList)
			{
				ImplicitRelation ir = new ImplicitRelation(eId2, eId1, pid, 100);
				res.add(ir);
			}
		}
			
		return res;
	}
	
	public static void main(String[] args) throws Exception {
		
		Globals.coreNLP = new CoreNLP();
		Globals.pd = new ParaphraseDictionary();
		try 
		{
			EntityFragmentFields.load();
			TypeFragment.load();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		ExtractImplicitRelation eir = new ExtractImplicitRelation();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String name1,name2;
		while(true)
		{
			System.out.println("Input two node to extract their implicit relations:");
			name1 = br.readLine();
			name2 = br.readLine();
			
			ArrayList<ImplicitRelation> irList = null;
			
			irList = eir.getPrefferdPidListBetween_TwoEntities(name1, name2);
			if(irList == null || irList.size()==0)
				System.out.println("Can't find!");
			else
			{
				for(ImplicitRelation ir: irList)
				{
					int pId = ir.pId;
					String p = Globals.pd.getPredicateById(pId);
					System.out.println(ir.subjId+"\t"+p+"\t"+ir.objId);
					System.out.println(ir.subj+"\t"+p+"\t"+ir.obj);
				}
			}
			
//			irList = eir.getPrefferdPidListBetween_TypeConstant_Entity(name1, name2);
//			if(irList == null || irList.size()==0)
//				System.out.println("Can't find!");
//			else
//			{
//				for(ImplicitRelation ir: irList)
//				{
//					int pId = ir.pId;
//					String p = Globals.pd.getPredicateById(pId);
//					System.out.println(ir.subj+"\t"+p+"\t"+ir.obj);
//				}
//			}
			
//			irList = eir.getPrefferdPidListBetween_Entity_Variable(name1, name2);
//			if(irList == null || irList.size()==0)
//				System.out.println("Can't find!");
//			else
//			{
//				for(ImplicitRelation ir: irList)
//				{
//					int pId = ir.pId;
//					String p = Globals.pd.getPredicateById(pId);
//					System.out.println(ir.subjId+"\t"+p+"\t"+ir.objId);
//				}
//			}
			
//			irList = eir.getPrefferdPidListBetween_Entity_TypeVariable(name1, name2);
//			if(irList == null || irList.size()==0)
//				System.out.println("Can't find!");
//			else
//			{
//				for(ImplicitRelation ir: irList)
//				{
//					int pId = ir.pId;
//					String p = Globals.pd.getPredicateById(pId);
//					System.out.println(ir.subjId+"\t"+p+"\t"+ir.objId);
//				}
//			}
		}
	}
}
