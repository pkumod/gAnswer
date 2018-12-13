package rdf;

import fgmt.TypeFragment;
import qa.Globals;
import lcn.EntityFragmentFields;

public class ImplicitRelation {

	public String subj = null;
	public String obj = null;
	
	public int pId = -1;
	public double score = 0;
	
	//Role :  1|ent , 2|type_ , 3|var
	public enum roleEnum {ENTITY, TYPE_CONSTANT, TYPE_VARIABLE, VARIABLE}; 
	public int subjRole = -1;
	public int objRole = -1;
	public int subjId = -1;
	public int objId = -1;
	
	public ImplicitRelation(String s, String o, int pid, double sc)
	{
		pId = pid;
		subj = s;
		obj = o;
		score = sc;
		subjId = EntityFragmentFields.entityName2Id.get(s);
		if(pId != Globals.pd.typePredicateID)
			objId = EntityFragmentFields.entityName2Id.get(o);
		else
			objId = TypeFragment.typeShortName2IdList.get(o).get(0);
	}
	
	public ImplicitRelation(Integer sId, Integer oId, int pid, double sc)
	{
		pId = pid;
		subjId = sId;
		objId = oId;
		score = sc;
	}
	
	public void setSubjectId(Integer s)
	{
		subjId = s;
	}
	
	public void setObjectId(Integer o)
	{
		objId = o;
	}
	
	public void setSubject(String s)
	{
		subj = s;
	}
	
	public void setObject(String o)
	{
		obj = o;
	}
	
	public int hashCode() 
	{
		return new Integer(pId).hashCode() ^ new Integer(subjId).hashCode() ^ new Integer(objId).hashCode();
	} 
	
    @Override 
    public boolean equals(Object ir) 
    { 
        ImplicitRelation tmpIr = (ImplicitRelation) ir; 
        if (pId == tmpIr.pId && subjId == tmpIr.subjId && objId == tmpIr.objId) 
        	return true; 
        else return false; 
    } 
    
}
