package fgmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class VariableFragment extends Fragment {
	public static final int magic_number = -265;

	public ArrayList<HashSet<Integer>> candTypes = null;
	public HashSet<Integer> candEntities = null;
	public boolean mayLiteral = false;
	
	public VariableFragment() 
	{
		fragmentType = typeEnum.VAR_FRAGMENT;
		candTypes = new ArrayList<HashSet<Integer>>();
		candEntities = new HashSet<Integer>();
	}
	
	@Override
	public String toString() 
	{
		return "("+ candEntities.size() +")";
	}
	
	public boolean containsAll(HashSet<Integer> s1) {
		Iterator<HashSet<Integer>> it = candTypes.iterator();
		while(it.hasNext()) {
			HashSet<Integer> s2 = it.next();
			if (s2.contains(magic_number)) {
				if (!Collections.disjoint(s1, s2)) {
					return true;
				}
			}
			else {
				if (s1.containsAll(s2) && s2.containsAll(s1)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean contains(Integer i) {
		Iterator<HashSet<Integer>> it = candTypes.iterator();
		while(it.hasNext()) {
			HashSet<Integer> s = it.next();
			if (s.contains(i)) {
				return true;
			}
		}
		return false;		
	}
}
