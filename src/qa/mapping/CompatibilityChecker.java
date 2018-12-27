package qa.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import qa.Globals;
import rdf.Sparql;
import rdf.Triple;
import fgmt.EntityFragment;
import fgmt.RelationFragment;
import fgmt.TypeFragment;
import fgmt.VariableFragment;

/**
 * Notice: one compatiblityChecker can be only used once to check a SPARQL.
 * @author husen
 */
public class CompatibilityChecker {
	
	static int EnumerateThreshold = 1000;
	public EntityFragmentDict efd = null;
	public HashMap<String, VariableFragment> variable_fragment = null;
	
	public CompatibilityChecker(EntityFragmentDict efd) {
		this.efd = efd;
		variable_fragment = new HashMap<String, VariableFragment>();
	}

	// Run this check function after pass "single triple check" (recoded)
	// Recoded: variable will find suitable entities, depend on the inMemory INDEX. Notice when variable = literal
	public boolean isSparqlCompatible3 (Sparql spq) 
	{
		boolean[] isFixed = new boolean[spq.tripleList.size()];	// record triple's compatibility whether need check
		for (int i = 0; i < spq.tripleList.size(); i ++) {
			isFixed[i] = false;
		}
		
		//System.out.println("tripleList size="+spq.tripleList.size());
		Iterator<Triple> it;
		boolean shouldContinue = true;
		// shouldContinue when: triple with variables updates variable fragment, then use updated variable fragment check the previous triples
		while (shouldContinue) 
		{
			shouldContinue = false;
			it = spq.tripleList.iterator();
			int t_cnt = 0;
			while (it.hasNext()) {
				Triple t = it.next();
				
				switch (getTripleType(t)) {	
				case 1:	// (1) E1, P, E2	
					if (!isFixed[t_cnt]) 
					{
						int ret = hs_check1_E1PE2(t);
						if (ret == 0)
							isFixed[t_cnt] = true;
						else if (ret == 5)
							return false;
					}
					break;
				case 2:	// (2) E,  P, V
					if(!isFixed[t_cnt])
					{
						int ret = hs_check2_EPV(t);
						if (ret == 5)
							return false;
						else 
						{
							isFixed[t_cnt] = true;	// Now V has set entities or literal; notice E/P->V maybe not unique, eg, xx's starring 
							if (ret == 1) 
								shouldContinue = true;
						}
					}
					break;
				case 3:	// (3) E,  <type1>, T
					if (!isFixed[t_cnt]) 
					{
						int ret = check3_Etype1T(t);
						if (ret == -2) return false;
						if (ret == 0) isFixed[t_cnt] = true;
					}
					break;
				case 4:	// (4) V,  P, E 
					if(!isFixed[t_cnt])
					{
						int ret = hs_check4_VPE(t);					
						if (ret == 5)
							return false;
						else 
						{
							isFixed[t_cnt] = true; // Now V has set entities or literal; notice E/P->V maybe not unique, eg, xx's starring 
							if (ret == 1) 
								shouldContinue = true;
						}
					}
					break;
				case 5:	// (5) V1, P, V2 (The most important and time consuming)
					if(!isFixed[t_cnt])
					{
						int ret = hs_check5_V1PV2(t); 
						if (ret == 5)
							return false;
						else 
						{
							isFixed[t_cnt] = true;	// Just set once and no re-check 
							if (ret == 1) 
								shouldContinue = true;
						}
					}
					break;
				case 6:	// (6) V,  <type1>, T
					if (!isFixed[t_cnt]) 
					{
						int ret = hs_check6_Vtype1T(t);
						if (ret == -2) return false;
						else
						{
							isFixed[t_cnt] = true;
							if (ret == 1)
								shouldContinue = true;
						}
					}
					break;
				case 7:
					// do nothing
					break;
				case 8:
				default:
					return false;
				}
				t_cnt ++;
			}
		}
		return true;
	}
	
	/**
	 * Get Triple's category
	 * (1) E1, P, E2
	 * (2) E,  P, V
	 * (3) E,  <type>, T
	 * (4) V,  P, E
	 * (5) V1, P, V2
	 * (6) V,  <type>, T
	 * (7) E,  <type>, V
	 * (8) error
	 * 
	 * E: Entity
	 * P: Predicate (exclude <type>)
	 * V: Variable
	 * T: Type
	 * 
	 * @param t
	 * @return
	 */
	public int getTripleType (Triple t) {
		if (t.predicateID == Globals.pd.typePredicateID) {
			boolean s = t.subject.startsWith("?");
			boolean o = t.object.startsWith("?");
			if (s && !o) return 6;
			else if (o && !s) return 7;
			else if (!s && !o) return 3;
			else return 8;
		}
		else if (t.subject.startsWith("?")) {
			if (t.object.startsWith("?")) return 5;
			else return 4;
		}
		else {
			if (t.object.startsWith("?")) return 2;
			else return 1;
		}
	}
	

	public int hs_check1_E1PE2(Triple t) 
	{
		int pid = t.predicateID;
		EntityFragment E1 = efd.getEntityFragmentByEid(t.subjId);
		EntityFragment E2 = efd.getEntityFragmentByEid(t.objId);

		// E2 is E1's one depth neighbor, connected with predicate "p"
		if(E1.outEntMap.containsKey(E2.eId))
		{
			ArrayList<Integer> pList = E1.outEntMap.get(E2.eId);
			if(pList.contains(pid))
				return 0;
		}
		
		return 5;
	}
	
	public int hs_check2_EPV(Triple t) 
	{
		int pid = t.predicateID;
		EntityFragment E = efd.getEntityFragmentByEid(t.subjId);
		VariableFragment V = variable_fragment.get(t.object);
		
		// P ∈ E.outEdges
		if (!E.outEdges.contains(pid)) {
			return 5;
		}

		// Set V, notice maybe literal
		if(V == null)
		{
			variable_fragment.put(t.object, new VariableFragment());
			V = variable_fragment.get(t.object);
			for(int vid: E.outEntMap.keySet())
			{
				if(E.outEntMap.get(vid).contains(pid))
				{
					V.candEntities.add(vid);
				}
			}
			// E's outEdges contain p, but cannot find neighbor ENT by p， then V maybe literal
			if(V.candEntities.size() == 0)
			{
				V.mayLiteral = true;
				return 0;
			}
		}
		else	
		{
			// just okay if V is literal, because fragment has not stored the literal information
			if(V.mayLiteral)
				return 0;
			
			// Update V's binding by current neighbor of E
			HashSet<Integer> newCandEntities = new HashSet<Integer>();
			if(V.candEntities.size() > 0 && V.candEntities.size() < E.outEntMap.size())
			{
				for(int vid: V.candEntities)
				{
					if(E.outEntMap.containsKey(vid) && E.outEntMap.get(vid).contains(pid))
					{
						newCandEntities.add(vid);
					}
				}
			}
			else
			{
				for(int vid: E.outEntMap.keySet())
				{
					if(E.outEntMap.get(vid).contains(pid) && (V.candEntities.size() == 0 || V.candEntities.contains(vid)))
					{
						newCandEntities.add(vid);
					}
				}
			}
			V.candEntities = newCandEntities;
		}
		
		if(V.candEntities.size() > 0)
			return 0;
		else
			return 5;
	}
	
	public int check3_Etype1T(Triple t) {
		String[] T = t.object.split("\\|");	// ע��"|"��Ҫת��
		EntityFragment E = efd.getEntityFragmentByEid(t.subjId);

		String newTypeString = "";
		boolean contained = false;

		// check whether each type int T is proper for E
		if (T.length == 0) return -2;
		for (String s : T) {
			contained = false;
			for (Integer i : TypeFragment.typeShortName2IdList.get(s)) {
				if (E.types.contains(i)) {
					if (!contained) {
						contained = true;
						newTypeString += s;
						newTypeString += "|";
					}
				}
			}
		}
		
		if (newTypeString.length() > 1) {
			t.object = newTypeString.substring(0, newTypeString.length()-1);
			return 0;
		}
		else return -2;
	}
		
	
	public int hs_check4_VPE(Triple t) 
	{
		int pid = t.predicateID;
		EntityFragment E = efd.getEntityFragmentByEid(t.objId);
		VariableFragment V = variable_fragment.get(t.subject);
		TypeFragment subjTf = SemanticItemMapping.getTypeFragmentByWord(t.getSubjectWord());
				
		// P ∈ E.inEdges
		if (!E.inEdges.contains(pid)) {
			return 5;
		}

		// Set V, notice V cannot be literal, because now V is subject
		if(V == null)
		{
			variable_fragment.put(t.subject, new VariableFragment());
			V = variable_fragment.get(t.subject);
			
			for(int vid: E.inEntMap.keySet())
			{
				if(E.inEntMap.get(vid).contains(pid) && (subjTf == null || subjTf.entSet.contains(vid)))
				{
					V.candEntities.add(vid);
				}
			}
			// E's inEdges contain p, but cannot find neighbor ENT by p, now V is subject and cannot be literal, so match fail
			if(V.candEntities.size() == 0)
			{
				return 5;
			}
		}
		else	
		{
			// if V is literal, fail because subject cannot be literal
			if(V.mayLiteral)
				return 5;
			
			// update V's binding by current E's neighbors
			HashSet<Integer> newCandEntities = new HashSet<Integer>();
			if(V.candEntities.size() > 0 && V.candEntities.size() < E.inEntMap.size())
			{
				for(int vid: V.candEntities)
				{
					if(E.inEntMap.containsKey(vid) && E.inEntMap.get(vid).contains(pid))
					{
						newCandEntities.add(vid);
					}
				}
			}
			else
			{
				for(int vid: E.inEntMap.keySet())
				{
					if(E.inEntMap.get(vid).contains(pid) && (V.candEntities.size() == 0 || V.candEntities.contains(vid)))
					{
						newCandEntities.add(vid);
					}
				}
			}
			V.candEntities = newCandEntities;
		}
		
		if(V.candEntities.size() > 0)
			return 0;
		else
			return 5;
	}
	
	public int check5_V1PV2(Triple t) {
		ArrayList<Integer> pidList = new ArrayList<Integer>();
		pidList.add(t.predicateID);
		VariableFragment V1 = variable_fragment.get(t.subject);
		VariableFragment V2 = variable_fragment.get(t.object);
		
		// V1 & V2's types, equal with types of one fragment of P
		Iterator<Integer> it_int = pidList.iterator();
		ArrayList<HashSet<Integer>> newCandTypes1 = new ArrayList<HashSet<Integer>>();
		ArrayList<HashSet<Integer>> newCandTypes2 = new ArrayList<HashSet<Integer>>();
		while (it_int.hasNext()) {
			Integer i = it_int.next();
			ArrayList<RelationFragment> flist = RelationFragment.relFragments.get(i);
			Iterator<RelationFragment> it_rln = flist.iterator();
			while (it_rln.hasNext()) {
				RelationFragment rf = it_rln.next();
				if (V1 == null && V2 == null) {
					newCandTypes1.add(rf.inTypes);
					newCandTypes2.add(rf.outTypes);
				}
				else if (V1 == null && V2 != null) {
					if (V2.containsAll(rf.outTypes)) {
						newCandTypes1.add(rf.inTypes);
						newCandTypes2.add(rf.outTypes);				
					}
				}
				else if (V2 == null && V1 != null) {
					if (V1.containsAll(rf.inTypes)) {
						newCandTypes1.add(rf.inTypes);
						newCandTypes2.add(rf.outTypes);				
					}
				}
				else {					
					if (V1.containsAll(rf.inTypes) && V2.containsAll(rf.outTypes)) 
					{
						newCandTypes1.add(rf.inTypes);
						newCandTypes2.add(rf.outTypes);				
					}
				}
			}
		}		
		
		if (newCandTypes1.size() > 0 && newCandTypes2.size() > 0) {
			if (V1 == null && V2 == null) {
				variable_fragment.put(t.subject, new VariableFragment());
				variable_fragment.get(t.subject).candTypes = newCandTypes1;
				
				variable_fragment.put(t.object, new VariableFragment());
				variable_fragment.get(t.object).candTypes = newCandTypes2;
				return 1;
			}
			else if (V1 == null && V2 != null) {
				variable_fragment.put(t.subject, new VariableFragment());
				variable_fragment.get(t.subject).candTypes = newCandTypes1;
				
				if (V2.candTypes.size() > newCandTypes2.size()) {
					V2.candTypes = newCandTypes2;
					return 1;
				}
				else return 0;
			}
			else if (V2 == null && V1 != null) {				
				variable_fragment.put(t.object, new VariableFragment());
				variable_fragment.get(t.object).candTypes = newCandTypes2;				

				if (V1.candTypes.size() > newCandTypes1.size()) {
					V1.candTypes = newCandTypes1;
					return 1;
				}
				else return 0;
			}
			else {
				if (V1.candTypes.size() > newCandTypes1.size() || V2.candTypes.size() > newCandTypes2.size()) {
					V1.candTypes = newCandTypes1;
					V2.candTypes = newCandTypes2;
					return 1;
				}
				else return 0;
			}
		}
		else return 5;
	}
	
	public int hs_check5_V1PV2(Triple t) 
	{
		int pid = t.predicateID;
		VariableFragment V1 = variable_fragment.get(t.subject);
		VariableFragment V2 = variable_fragment.get(t.object);
		
		if(V1 == null && V2 == null)	// The WORST case, current relation fragment has no records of two target entities, cannot check without types, so we should put this triple in the end
		{
			return 0;	// in fact should return 1, just expect the unchecked triples can provide candidates of V1,V2 then can check in the next turn 
		}
		else if(V2 == null)
		{
			if(V1.mayLiteral)
				return 5;
			
			variable_fragment.put(t.object, new VariableFragment());
			V2 = variable_fragment.get(t.object);
			
			HashSet<Integer> newV1cands = new HashSet<Integer>();
			int cnt = 0;
			for(int v1id: V1.candEntities)
			{
				cnt++;
				if(cnt > EnumerateThreshold)
					break;
				EntityFragment E = efd.getEntityFragmentByEid(v1id);
				if(E != null && E.outEdges.contains(pid))
				{
					newV1cands.add(v1id);
					for(int v2id: E.outEntMap.keySet())
					{
						if(E.outEntMap.get(v2id).contains(pid))
							V2.candEntities.add(v2id);
					}
				}
			}
			V1.candEntities = newV1cands;
		}
		else if(V1 == null)
		{
			if(V2.mayLiteral)
				return 0;
			
			variable_fragment.put(t.subject, new VariableFragment());
			V1 = variable_fragment.get(t.subject);
			
			HashSet<Integer> newV2cands = new HashSet<Integer>();
			int cnt = 0;
			for(int v2id: V2.candEntities)
			{
				cnt++;
				if(cnt > EnumerateThreshold)
					break;
				EntityFragment E = efd.getEntityFragmentByEid(v2id);
				if(E != null && E.inEdges.contains(pid))
				{
					newV2cands.add(v2id);
					for(int v1id: E.inEntMap.keySet())
					{
						if(E.inEntMap.get(v1id).contains(pid))
							V1.candEntities.add(v1id);
					}
				}
			}
			V2.candEntities = newV2cands;
		}
		else
		{
			if(V1.mayLiteral)
				return 5;
			if(V2.mayLiteral)
				return 0;
			
			HashSet<Integer> newV1cands = new HashSet<Integer>();
			HashSet<Integer> newV2cands = new HashSet<Integer>();
			for(int v1id: V1.candEntities)
			{
				EntityFragment E1 = efd.getEntityFragmentByEid(v1id);
				if(E1 != null && E1.outEdges.contains(pid))
					newV1cands.add(v1id);
			}
			V1.candEntities = newV1cands;
			for(int v2id: V2.candEntities)
			{
				EntityFragment E2 = efd.getEntityFragmentByEid(v2id);
				if(E2 != null && E2.inEdges.contains(pid))
					newV2cands.add(v2id);
			}
			V2.candEntities = newV2cands;
			
			newV1cands = new HashSet<Integer>();
			newV2cands = new HashSet<Integer>();
			for(int v1id: V1.candEntities)
			{
				EntityFragment E1 = efd.getEntityFragmentByEid(v1id);
				for(int v2id: V2.candEntities)
				{
					if(E1.outEntMap.containsKey(v2id) && E1.outEntMap.get(v2id).contains(pid))
					{
						newV1cands.add(v1id);
						newV2cands.add(v2id);
					}
				}
			}
			V1.candEntities = newV1cands;
			V2.candEntities = newV2cands;
		}
		
		if(V1.candEntities.size() == 0 || (V2.candEntities.size() == 0 && !RelationFragment.isLiteral(pid)))
			return 5;
		else
			return 0;
	}
	
	public int check6_Vtype1T(Triple t) {
		
		String[] T = t.object.split("\\|");	// notice "|" need "\\|"
		VariableFragment V = variable_fragment.get(t.subject);

		String newTypeString = "";
		boolean contained = false;

		// check whether each type in T is proper for V
		if (T.length == 0) return -2;
		
		ArrayList<HashSet<Integer>> newCandTypes = new ArrayList<HashSet<Integer>>();
		for (String s : T) 
		{
			contained = false;
			
			//YAGO type (uncoded types), just return because we have no INDEX to check it
			if(!TypeFragment.typeShortName2IdList.containsKey(s))
				return 0;
			
			for (Integer i : TypeFragment.typeShortName2IdList.get(s)) 
			{
				if (V == null) {
					// constraint V by user given types, flag it due to possible incomplete type
					HashSet<Integer> set = new HashSet<Integer>();
					set.add(i);
					set.add(VariableFragment.magic_number);
					newCandTypes.add(set);
					if (!contained) {
						contained = true;
						newTypeString += s;
						newTypeString += "|";
					}
				}
				else if (V.contains(i)) {
					if (!contained) {
						contained = true;
						newTypeString += s;
						newTypeString += "|";
					}
				}
			}
		}
		
		// check whether each fragment in V is proper for T
		// if not, delete the fragment (that means we can narrow the scope)
		ArrayList<HashSet<Integer>> deleteCandTypes = new ArrayList<HashSet<Integer>>();
		if (V != null) 
		{
			Iterator<HashSet<Integer>> it = V.candTypes.iterator();
			while(it.hasNext()) {
				HashSet<Integer> set = it.next();
				boolean isCandTypeOkay = false;
				//v get [constraint types] through other triples, at least one type can reserve, otherwise delete the [constriant types]
				for (String s : T) 
				{
					for (Integer i : TypeFragment.typeShortName2IdList.get(s)) {
						if (set.contains(i)) {
							isCandTypeOkay = true;
							break;
						}
					}
				}
				if (!isCandTypeOkay) {
					deleteCandTypes.add(set);
				}
			}
			V.candTypes.removeAll(deleteCandTypes);			
		}
		
		
		if (V == null) {
			variable_fragment.put(t.subject, new VariableFragment());
			variable_fragment.get(t.subject).candTypes = newCandTypes;
		}
		if (newTypeString.length() > 1) {
			t.object = newTypeString.substring(0, newTypeString.length()-1);
			if (deleteCandTypes.size() > 0) {
				return 1;
			}
			else {
				return 0;
			}
		}
		else return -2;
	}

	public int hs_check6_Vtype1T(Triple t) 
	{
		String[] tList = t.object.split("\\|");	// ע��"|"��Ҫת��
		VariableFragment V = variable_fragment.get(t.subject);

		if (tList.length == 0) return -2;
		
		// Simplify, only consider the first one
		if(!TypeFragment.typeShortName2IdList.containsKey(tList[0]))
			return 0;
		
		int tid = TypeFragment.typeShortName2IdList.get(tList[0]).get(0);
		TypeFragment T = TypeFragment.typeFragments.get(tid);
		if(V == null)
		{
			variable_fragment.put(t.subject, new VariableFragment());
			V = variable_fragment.get(t.subject);
			V.candEntities = T.entSet;
		}
		else
		{
			if(V.mayLiteral)	//literal cannot be subject
				return -2;
			
			HashSet<Integer> newVcands = new HashSet<Integer>();
			for(int vid: V.candEntities)
			{
				EntityFragment E = efd.getEntityFragmentByEid(vid);
				if(E.types.contains(tid))
					newVcands.add(vid);
			}
			V.candEntities = newVcands;	
		}
		
		if(V.candEntities.size() == 0)
			return -2;
		else
			return 0;
	}

	public void swapTriple (Triple t) {
		String temp = t.subject;
		t.subject = t.object;
		t.object = temp;
	}
};