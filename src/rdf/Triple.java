package rdf;

import nlp.ds.Word;
import qa.Globals;

public class Triple implements Comparable<Triple>{
	public String subject = null;	// subject/object after disambiguation.
	public String object = null;
	
	static public int TYPE_ROLE_ID = -5;
	static public int VAR_ROLE_ID = -2;
	static public int CAT_ROLE_ID = -8;	// Category
	static public String VAR_NAME = "?xxx";
	
	// subjId/objId: entity id | TYPE_ROLE_ID | VAR_ROLE_ID
	public int subjId = -1;
	public int objId = -1;
	public int predicateID = -1;
	public Word subjWord = null;	// only be used when semRltn == null
	public Word objWord = null;
	
	public SemanticRelation semRltn = null;
	public double score = 0;
	public boolean isSubjObjOrderSameWithSemRltn = true;
	public boolean isSubjObjOrderPrefered = false;
	
	public Word typeSubjectWord = null; // for "type" triples only
	
	public Triple (Triple t) {
		subject = t.subject;
		object = t.object;
		subjId = t.subjId;
		objId = t.objId;
		predicateID = t.predicateID;
		
		semRltn = t.semRltn;
		score = t.score;
		isSubjObjOrderSameWithSemRltn = t.isSubjObjOrderSameWithSemRltn;
		isSubjObjOrderPrefered = t.isSubjObjOrderPrefered;
	}
	
	// A final triple (subject/object order will not changed), does not rely on semantic relation (sr == null), from one word (type variable | embedded info) 
	public Triple (int sId, String s, int p, int oId, String o, SemanticRelation sr, double sco) {
		subjId = sId;
		objId = oId;
		subject = s;
		predicateID = p;
		object = o;
		semRltn = sr;
		score = sco;
	}

	// A triple translated from a semantic relation (subject/object order can be changed in later)
	public Triple (int sId, String s, int p, int oId, String o, SemanticRelation sr, double sco, boolean isSwap) {
		subjId = sId;
		objId = oId;
		subject = s;
		predicateID = p;
		object = o;
		semRltn = sr;
		score = sco;
		isSubjObjOrderSameWithSemRltn = isSwap;
	}
	
	// A final triple (subject/object order will not changed), does not rely on semantic relation (sr == null), from two word (implicit relations of modifier)
	public Triple(int sId, String s, int p, int oId, String o, SemanticRelation sr, double sco, Word subj, Word obj) {
		subjId = sId;
		objId = oId;
		subject = s;
		predicateID = p;
		object = o;
		semRltn = sr;
		score = sco;
		subjWord = subj;
		objWord = obj;
	}

	public Triple copy() {
		Triple t = new Triple(this);
		return t;
	}
	
	public Triple copySwap() {
		Triple t = new Triple(this);
		String temp;
		int tmpId;

		tmpId = t.subjId;
		t.subjId = t.objId;
		t.objId = tmpId;
		
		temp = t.subject;
		t.subject = t.object;
		t.object = temp;
		
		t.isSubjObjOrderSameWithSemRltn = !this.isSubjObjOrderSameWithSemRltn;
		t.isSubjObjOrderPrefered = !this.isSubjObjOrderPrefered;
		
		return t;
	}
	
	public void addScore(double s) {
		score += s;
	}
	
	public double getScore() {
		return score;
	}
	
	@Override 
	public int hashCode() 
    { 
        return new Integer(subjId).hashCode() ^ new Integer(objId).hashCode() ^ new Integer(predicateID).hashCode(); 
    } 
		
	@Override
	public String toString() {
		return subjId+":<" + subject + "> <" + Globals.pd.getPredicateById(predicateID) + "> "+objId+":<" + object + ">" + " : " + score;
	}

	public String toStringForGStore() {
		StringBuilder sb = new StringBuilder("");
		
		String _subject = subject;
		if(_subject.startsWith("?")) 
			sb.append(_subject+"\t");
		else 
			sb.append("<" + _subject + ">\t");
		
		sb.append("<" + Globals.pd.getPredicateById(predicateID) + ">\t");
		
		String _object;
		if(predicateID == Globals.pd.typePredicateID && object.contains("|")) 
			_object = object.substring(0, object.indexOf('|'));
		else 
			_object = object;
		if(_object.startsWith("?")) 
			sb.append(_object);
		else 
			sb.append("<" + _object + ">");
		
		return sb.toString().replace(' ', '_');
	}
	
	public String toStringWithoutScore() {
		return "<" + subject + "> <" + Globals.pd.getPredicateById(predicateID) + "> <" + object + ">";
	}
	
	public Word getSubjectWord () {
		if (predicateID == Globals.pd.typePredicateID) {
			return typeSubjectWord;
		}
		else if(semRltn == null)
		{
			return subjWord;
		}
		else {
			if (isSubjObjOrderSameWithSemRltn) return semRltn.arg1Word;
			else return semRltn.arg2Word;			
		}
		
	}
	
	public Word getObjectWord () {
		if (predicateID == Globals.pd.typePredicateID) {
			return typeSubjectWord;
		}
		else if(semRltn == null)
		{
			return objWord;
		}
		else {
			if (isSubjObjOrderSameWithSemRltn) return semRltn.arg2Word;
			else return semRltn.arg1Word;
		}
	}
	
	public boolean isSubjConstant () {
		if (predicateID == Globals.pd.typePredicateID) {
			return !subject.startsWith("?");
		}
		else {
			// Triple from semantic (obvious) relation 
			if(semRltn != null)
			{
				if (isSubjObjOrderSameWithSemRltn) return semRltn.isArg1Constant;
				else return semRltn.isArg2Constant;
			}
			// Triple from implicit relation (no semantic relation), it is final triple
			else
			{
				if(subjId != Triple.VAR_ROLE_ID && subjId != Triple.TYPE_ROLE_ID)
					return true;
				else
					return false;
			}
		}
	}
	
	public boolean isObjConstant () {
		if (predicateID == Globals.pd.typePredicateID) {
			return !object.startsWith("?");
		}
		else {
			if(semRltn != null)
			{
				if (isSubjObjOrderSameWithSemRltn) return semRltn.isArg2Constant;
				else return semRltn.isArg1Constant;
			}
			else
			{
				if(objId != Triple.VAR_ROLE_ID && objId != Triple.TYPE_ROLE_ID)
					return true;
				else
					return false;
			}
		}
	}
	
	public int compareTo(Triple o) 
	{
		//Order: Type, Ent&Ent, Ent&Var, Var&Var
		if(this.predicateID == Globals.pd.typePredicateID)
		{
			if(o.predicateID == Globals.pd.typePredicateID)
				return 0;
			else
				return -1;
		}
		int cnt1 = 0, cnt2 = 0;
		if(!this.subject.startsWith("?"))
			cnt1++;
		if(!this.object.startsWith("?"))
			cnt1++;
		if(!o.subject.startsWith("?"))
			cnt2++;
		if(!o.object.startsWith("?"))
			cnt2++;
		
		if(cnt1 == cnt2)
			return 0;
		else if(cnt1 > cnt2)
			return -1;
		else
			return 1;
	}
	
	public void swapSubjObjOrder() {		
		String temp = subject;
		int tmpId = subjId;
		subject = object;
		subjId = objId;
		object = temp;
		objId = tmpId;
		isSubjObjOrderSameWithSemRltn = !isSubjObjOrderSameWithSemRltn;
	}
};