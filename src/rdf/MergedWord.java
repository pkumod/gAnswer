package rdf;

import java.util.ArrayList;

import rdf.EntityMapping;
import rdf.TypeMapping;

public class MergedWord implements Comparable<MergedWord> 
{
	//original position
	public int st,ed;
	//position after merge (unselected is -1)
	public int mergedPos = -1;
	public String name;
	public boolean mayCategory = false;
	public boolean mayLiteral = false;
	public boolean mayEnt = false;
	public boolean mayType = false;
	public ArrayList<EntityMapping> emList = null;
	public ArrayList<TypeMapping> tmList = null;
	public String category = null;
	
	public MergedWord(int s,int e,String n)
	{
		st = s;
		ed = e;
		name = n;
	}
	
	@Override
	//long to short
	public int compareTo(MergedWord o) 
	{
		int lenDiff = (this.ed-this.st) - (o.ed-o.st);
		
		if (lenDiff > 0) return -1;
		else if (lenDiff < 0) return 1;
		return 0;
	}
	
}
