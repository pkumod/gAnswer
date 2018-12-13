package qa.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import nlp.ds.Word;
import nlp.ds.Sentence.SentenceType;
import fgmt.EntityFragment;
import fgmt.RelationFragment;
import fgmt.TypeFragment;
import log.QueryLogger;
import qa.Globals;
import rdf.EntityMapping;
import rdf.PredicateMapping;
import rdf.SemanticRelation;
import rdf.Sparql;
import rdf.Triple;
import rdf.TypeMapping;

public class SemanticItemMapping {
	
	public HashMap<Word, ArrayList<EntityMapping>> entityDictionary = new HashMap<Word, ArrayList<EntityMapping>>();
	public static int k = 10;	// useless now
	public static int t = 10;	// Depth of enumerating candidates of each node/edge. O(t^n).
	ArrayList<Sparql> rankedSparqls = new ArrayList<Sparql>();
	HashSet<String> checkedSparqlStrs = new HashSet<String>();
	
	public ArrayList<ArrayList<EntityMapping>> entityPhrasesList = new ArrayList<ArrayList<EntityMapping>>();
	public ArrayList<Word> entityWordList = new ArrayList<Word>();
	public HashMap<Integer, EntityMapping> currentEntityMappings = new HashMap<Integer, EntityMapping>();
	
	public ArrayList<ArrayList<PredicateMapping>> predicatePhraseList = new ArrayList<ArrayList<PredicateMapping>>();
	public ArrayList<SemanticRelation> predicateSrList = new ArrayList<SemanticRelation>();
	public HashMap<Integer, PredicateMapping> currentPredicateMappings = new HashMap<Integer, PredicateMapping>();
	
	public HashMap<Integer, SemanticRelation> semanticRelations = null;
	public QueryLogger qlog = null;
	
	public EntityFragmentDict efd = new EntityFragmentDict();
	
	public boolean isAnswerFound = false; 
	public int tripleCheckCallCnt = 0;
	public int sparqlCheckCallCnt = 0;
	public int sparqlCheckId = 0;
	
	SemanticRelation firstFalseSr = null;
	long tripleCheckTime = 0;
	long sparqlCheckTime = 0;
		
	/*
	 * A best-first top-down method, enumerate all possible query graph and sort.
	 * Notice, we use fragment checking to simulate graph matching and generate the TOP-k SPARQL queries, which can be executed via GStore or Virtuoso.
	 * */
	public void process(QueryLogger qlog, HashMap<Integer, SemanticRelation> semRltn) 
	{
		semanticRelations = semRltn;
		this.qlog = qlog;
		long t1;
		t = 10;	// Notice, t is adjustable. 

		entityPhrasesList.clear();
		entityWordList.clear();
		currentEntityMappings.clear();
		predicatePhraseList.clear();
		predicateSrList.clear();
		currentPredicateMappings.clear();
		
		// 1. collect info of constant nodes(entities)
		Iterator<Map.Entry<Integer, SemanticRelation>> it = semanticRelations.entrySet().iterator(); 
        while(it.hasNext())
        {
            Map.Entry<Integer, SemanticRelation> entry = it.next();
            SemanticRelation sr = entry.getValue();
            
            //We now only tackle Constant of Entity & Type. TODO: consider Literal.
			if(sr.isArg1Constant && !sr.arg1Word.mayType && !sr.arg1Word.mayEnt || sr.isArg2Constant && !sr.arg2Word.mayType && !sr.arg2Word.mayEnt) 
			{
				it.remove();
				continue;
			}
			
			//Type constant will be solved in ScoreAndRanking function.
			if(sr.isArg1Constant && sr.arg1Word.mayEnt) 
			{
				if(!entityDictionary.containsKey(sr.arg1Word)) 
					entityDictionary.put(sr.arg1Word, sr.arg1Word.emList);
				entityPhrasesList.add(sr.arg1Word.emList);
				entityWordList.add(sr.arg1Word);
			}
			if(sr.isArg2Constant && !sr.arg2Word.mayType) 
			{	
				if (!entityDictionary.containsKey(sr.arg2Word)) 
					entityDictionary.put(sr.arg2Word, sr.arg2Word.emList);
				entityPhrasesList.add(sr.arg2Word.emList);
				entityWordList.add(sr.arg2Word);
			}
        }
		
		// 2. collect info of edges(relations).
		for (Integer key : semanticRelations.keySet()) 
		{
			SemanticRelation sr = semanticRelations.get(key);
			predicatePhraseList.add(sr.predicateMappings);
			predicateSrList.add(sr);
			
			// Reduce t when structure enumeration needed.
			if(Globals.evaluationMethod > 1 && !sr.isSteadyEdge)
				t = 5;
		}
		
		// 3. top-k join
		t1 = System.currentTimeMillis();
		if(semanticRelations.size()>0)
			topkJoin(semanticRelations);
		else
			System.out.println("No Valid SemanticRelations.");
		
		qlog.timeTable.put("TopkJoin", (int)(System.currentTimeMillis()-t1));
		qlog.timeTable.put("TripleCheck", (int)tripleCheckTime);
		qlog.timeTable.put("SparqlCheck", (int)sparqlCheckTime);

		Collections.sort(rankedSparqls);		
		// Notice, use addAll because we may have more than one node recognition decision.
		qlog.rankedSparqls.addAll(rankedSparqls); 
		qlog.entityDictionary = entityDictionary;
		
		System.out.println("Check query graph count: " + tripleCheckCallCnt + "\nPass single check: " + sparqlCheckCallCnt + "\nPass final check: " + rankedSparqls.size());
		System.out.println("TopkJoin time=" + qlog.timeTable.get("TopkJoin"));
	}

	public void topkJoin (HashMap<Integer, SemanticRelation> semanticRelations) 
	{
		dfs_entityName(0);
	}
	
	// Each level for a CERTAIN entity
	public void dfs_entityName (int level_i) 
	{
		// All entities ready.
		if (level_i == entityPhrasesList.size()) 
		{
			dfs_predicate(0);
			return;
		}
		
		ArrayList<EntityMapping> list = entityPhrasesList.get(level_i);
		Word w = entityWordList.get(level_i);
		int tcount = 0;
		for(EntityMapping em : list) 
		{
			if (tcount == t || isAnswerFound) break;
			currentEntityMappings.put(w.hashCode(), em);
			dfs_entityName(level_i+1);
			currentEntityMappings.remove(w.hashCode());
			tcount ++;
		}
	}
	
	public void dfs_predicate(int level_i) 
	{
		// All entities & predicates ready, start generate SPARQL.
		if (level_i == predicatePhraseList.size()) 
		{
			scoringAndRanking();
			return;
		}
		
		ArrayList<PredicateMapping> list = predicatePhraseList.get(level_i);
		SemanticRelation sr = predicateSrList.get(level_i);
		if (sr.dependOnSemanticRelation != null) 
		{
			dfs_predicate(level_i+1);
		}
		else 
		{
			int tcount=0;
			for (PredicateMapping pm : list) 
			{
				if (tcount==t || isAnswerFound) break;
				currentPredicateMappings.put(sr.hashCode(), pm);
				dfs_predicate(level_i+1);
				currentPredicateMappings.remove(sr.hashCode());
				tcount++;
				
				// Pruning (If we do not change predicate of firstFalseSr, it will still false, so just return) 
				if(firstFalseSr != null)
				{
					if(firstFalseSr != sr) return;
					else firstFalseSr = null;
				}
			}
			
			// "null" means we drop this edge, this is how we enumerate structure.
			if(Globals.evaluationMethod == 2 && sr.isSteadyEdge == false)
			{
				currentPredicateMappings.put(sr.hashCode(), null);
				dfs_predicate(level_i+1);
				currentPredicateMappings.remove(sr.hashCode());
				tcount++;
			}
		}
	}

	/*
	 * Run this function when all nodes/edges have set value (through currentEntityMappings、currentPredicateMappings)
	 * Generate SPARQL according current ENTs and RELATIONs, then fragment checking
	 * Notice: add embedded type information：
	 * eg, ?who <height> ?how --add-->  ?who <type1> <Person> | ?book <author> <Tom> --add--> ?book <type1> <Book>
	 * Notice: add constant type information：
	 * eg, ask: <YaoMing> <type1> <BasketballPlayer> 
	 * Notice: add embedded triple information：
	 * eg, ?Canadians <residence> <Unitied_State> --add--> ?Canadians <birthPlace> <Canada>
	 * */
	public void scoringAndRanking() 
	{		
		firstFalseSr = null;
		Sparql sparql = new Sparql(semanticRelations);

		// A simple way to judge connectivity (may incorrect when nodes number >= 6)
		//TODO: a standard method to judge CONNECTIVITY
		HashMap<Integer, Integer> count = new HashMap<Integer, Integer>();
		int edgeCnt = 0;
		for (Integer key : semanticRelations.keySet()) 
		{
			SemanticRelation sr = semanticRelations.get(key);
			if(currentPredicateMappings.get(sr.hashCode()) == null)
				continue;
			
			edgeCnt++;
			int v1 = sr.arg1Word.hashCode(), v2 = sr.arg2Word.hashCode();
			if(!count.containsKey(v1))
				count.put(v1, 1);
			else
				count.put(v1, count.get(v1)+1);
			if(!count.containsKey(v2))
				count.put(v2, 1);
			else
				count.put(v2, count.get(v2)+1);
		}
		if(count.size() < qlog.semanticUnitList.size())
			return;
		if(edgeCnt == 0)
			return;
		if(edgeCnt > 1)
		{
			for (Integer key : semanticRelations.keySet()) 
			{
				SemanticRelation sr = semanticRelations.get(key);
				if(currentPredicateMappings.get(sr.hashCode()) == null)
					continue;
				int v1 = sr.arg1Word.hashCode(), v2 = sr.arg2Word.hashCode();
				if(count.get(v1) == 1 && count.get(v2) == 1)
					return;
			}
		}
		
		// Now the graph is connected, start to generate SPARQL.
		HashSet<String> typeSetFlag = new HashSet<String>();
		for (Integer key : semanticRelations.keySet()) 
		{
			SemanticRelation sr = semanticRelations.get(key);
			String sub, obj;
			int subjId = -1, objId = -1;
			int pid;
			double score = 1;
			boolean isSubjObjOrderSameWithSemRltn = true;
			
// argument1
			if(sr.isArg1Constant && (sr.arg1Word.mayEnt || sr.arg1Word.mayType) )	// Constant 
			{
				// For subject, entity has higher priority.
				if(sr.arg1Word.mayEnt)
				{
					EntityMapping em = currentEntityMappings.get(sr.arg1Word.hashCode());
					subjId = em.entityID;
					sub = em.entityName;
					score *= em.score;
				}
				else
				{
					TypeMapping tm = sr.arg1Word.tmList.get(0);
					subjId = Triple.TYPE_ROLE_ID;
					sub = tm.typeName;
					score *= (tm.score*100);	// Generalization. type score: [0,1], entity score: [0,100].
				}
			}
			else // Variable
			{
				subjId = Triple.VAR_ROLE_ID;
				sub = "?" + sr.arg1Word.originalForm;
			}
			// Embedded Type info of argument1(variable type) | eg, ?book <type> <Book>
			// Notice, mayType & mayExtendVariable is mutual-exclusive. (see constantVariableRecognition)
			// Notice, we do NOT consider types of [?who,?where...] now.
			Triple subt = null;
			if (!sr.isArg1Constant && sr.arg1Word.mayType && sr.arg1Word.tmList != null && sr.arg1Word.tmList.size() > 0 && !typeSetFlag.contains(sub)) 
			{
				StringBuilder type = new StringBuilder("");
				for (TypeMapping tm: sr.arg1Word.tmList) 
				{
					Integer tt = tm.typeID;
					if(tt != -1)
						type.append(TypeFragment.typeId2ShortName.get(tt));
					else
						type.append(tm.typeName);
					type.append('|');
				}
				String ttt = type.substring(0, type.length()-1);
				subt = new Triple(subjId, sub, Globals.pd.typePredicateID, Triple.TYPE_ROLE_ID, ttt, null, 10);
				subt.typeSubjectWord = sr.arg1Word;
				
				if(sr.arg1Word.tmList.get(0).prefferdRelation == -1)
					subt = null;
			}
// predicate
			SemanticRelation dep = sr.dependOnSemanticRelation;
			PredicateMapping pm = null; 
			if (dep == null) 
				pm = currentPredicateMappings.get(sr.hashCode());
			else 
				pm = currentPredicateMappings.get(dep.hashCode());
			if(pm == null)
				continue;
			
			pid = pm.pid;
			score *= pm.score;
// argument2
			if(sr.isArg2Constant && (sr.arg2Word.mayEnt || sr.arg2Word.mayType) )
			{
				if(!sr.arg2Word.mayType)
				{
					EntityMapping em = currentEntityMappings.get(sr.arg2Word.hashCode());
					objId = em.entityID;
					obj = em.entityName;
					score *= em.score;
				}
				else
				{
					TypeMapping tm = sr.arg2Word.tmList.get(0);
					objId = Triple.TYPE_ROLE_ID;
					obj = tm.typeName;
					score *= (tm.score*100);
				}
			}
			else 
			{
				objId = Triple.VAR_ROLE_ID;
				obj = "?" + sr.arg2Word.getFullEntityName();
			}
			// Type info of argument2
			Triple objt = null;
			if (sr.arg2Word.tmList != null && sr.arg2Word.tmList.size() > 0 && !typeSetFlag.contains(obj) && !sr.isArg2Constant) 
			{
				StringBuilder type = new StringBuilder("");				
				for (TypeMapping tm : sr.arg2Word.tmList) 
				{
					Integer tt = tm.typeID;
					if(tt != -1)
						type.append(TypeFragment.typeId2ShortName.get(tt));
					else
						type.append(tm.typeName);
					type.append('|');
				}
				String ttt = type.substring(0, type.length()-1);
				objt = new Triple(objId, obj, Globals.pd.typePredicateID, Triple.TYPE_ROLE_ID, ttt, null, 10);
				objt.typeSubjectWord = sr.arg2Word;
				
				if(sr.arg2Word.tmList.get(0).prefferdRelation == -1)
					objt = null;
			}
			
			// Prune.
			if(objId == Triple.TYPE_ROLE_ID && pid != Globals.pd.typePredicateID)
				return;
			
			// Consider orders rely on LITERAL relations | at least one argument has TYPE info 
			if (RelationFragment.isLiteral(pid) && (subt != null || objt != null)) 
			{
				if (sub.startsWith("?") && obj.startsWith("?")) // two variables
				{
					// two variables have both possibility as object literal
					if (subt != null) {
						subt.object += ("|" + "literal_HRZ");
					}
					if (objt != null) {
						objt.object += ("|" + "literal_HRZ");
					}
					
					if (subt==null && objt!=null) 
					{						
						// if object has type, subject has no type, more possible to change sub/obj because literal has no type in general [however maybe have yago:type]
						String temp = sub;
						int tmpId = subjId;
						sub = obj;
						subjId = objId;
						obj = temp;
						objId = tmpId;
						isSubjObjOrderSameWithSemRltn=!isSubjObjOrderSameWithSemRltn;
					}
					
				}
				else if (sub.startsWith("?") && !obj.startsWith("?")) {
					// need change subj/obj order
					if (subt != null) {
						subt.object += ("|" + "literal_HRZ");
					}					
					String temp = sub;
					int tmpId = subjId;
					sub = obj;
					subjId = objId;
					obj = temp;
					objId = tmpId;
					isSubjObjOrderSameWithSemRltn=!isSubjObjOrderSameWithSemRltn;
					//System.out.println("here: "+sub+obj);
					
				}
				else if (obj.startsWith("?") && !sub.startsWith("?")) {
					if (objt != null) {
						objt.object += ("|" + "literal_HRZ");
					}				
				}
			}
			
			Triple t = new Triple(subjId, sub, pid, objId, obj, sr, score,isSubjObjOrderSameWithSemRltn);		
			//System.out.println("triple: "+t+" "+isTripleCompatibleCanSwap(t));
			
			sparql.addTriple(t);
			
			// score of subject/object's type should correlative with the score of triple itself
			if (subt != null) 
			{
				subt.score += t.score*0.2;
				sparql.addTriple(subt);
				typeSetFlag.add(subt.subject); // be cautious to NOT use sub, it may has changed subj/obj order
			}
			if (objt != null) 
			{
				objt.score += t.score*0.2;
				sparql.addTriple(objt);
				typeSetFlag.add(objt.subject);
			}
			
			// add argument' embedded triple, eg, ?canadian	<birthPlace>	<Canada>
			if(!sr.isArg1Constant && sr.arg1Word.mayExtendVariable && sr.arg1Word.embbededTriple != null)
			{
				sparql.addTriple(sr.arg1Word.embbededTriple);
			}
			if(!sr.isArg2Constant && sr.arg2Word.mayExtendVariable && sr.arg2Word.embbededTriple != null)
			{
				sparql.addTriple(sr.arg2Word.embbededTriple);
			}
			
			sparql.adjustTriplesOrder();
		}
		
		// deduplicate
		sparql.deduplicate();
		if(checkedSparqlStrs.contains(sparql.toStringForGStore2()))
			return;
		checkedSparqlStrs.add(sparql.toStringForGStore2());
		
		if (!qlog.MODE_fragment) {
			// Method 1: do NOT check compatibility
			rankedSparqls.add(sparql);
			isAnswerFound = true;
		}
		else {
			// Method 2: check compatibility by FRAGMENT (offline index)
			//1. single-triple check (a quickly prune), allow to swap subject and object. Try to adjust to the best order.
			tripleCheckCallCnt++;
			long t1 = System.currentTimeMillis();
			for (Triple t : sparql.tripleList)				
				if(t.predicateID!=Globals.pd.typePredicateID && !isTripleCompatibleCanSwap(t))
				{
					firstFalseSr = t.semRltn;
					return;
				}
			tripleCheckTime += (System.currentTimeMillis()-t1);	
			
			//2. SPARQL check (consider the interact between all triples), allow to swap subject and object.
			t1 = System.currentTimeMillis();
			sparqlCheckCallCnt++;
			enumerateSubjObjOrders(sparql, new Sparql(sparql.semanticRelations), 0);	
			sparqlCheckTime += (System.currentTimeMillis()-t1);
		}
		
	}
	
	/*
	 * Notice：
	 * typeId=-1 then no data fragment
	 * */
	public static TypeFragment getTypeFragmentByWord(Word word)
	{
		TypeFragment tf = null;
		if(word!=null && word.tmList!=null && word.tmList.size()>0)
		{
			int typeId = word.tmList.get(0).typeID;
			if(typeId != -1)
				tf = TypeFragment.typeFragments.get(typeId);
		}
		return tf;
	}
	
	/*
	 * (Just PRE CHECK [single triple check] in this function, the final check in enumerateSubjObjOrders which utilize more INDEX)
	 * notice: predicate = type cannot entrance this function 
	 * */
	public boolean isTripleCompatibleCanSwap (Triple t) {
		
		if (qlog.s.sentenceType==SentenceType.GeneralQuestion)
		{	
			if (fragmentCompatible2(t.subjId, t.predicateID, t.objId) >
				fragmentCompatible2(t.objId, t.predicateID, t.subjId)) 
				t.swapSubjObjOrder();
				
			if (fragmentCompatible(t.subjId, t.predicateID, t.objId))			
				return true;
			return false;
			
		}
		else
		{	
			//var & var
			if(t.subject.startsWith("?") && t.object.startsWith("?"))
			{
				Word subjWord = t.getSubjectWord(), objWord = t.getObjectWord();
				TypeFragment subjTf = getTypeFragmentByWord(subjWord), objTf = getTypeFragmentByWord(objWord);
				
				//based on whether the two varabile's type fragment's in/out edge contain predicate, calculate whether need change order
				//just vote
				int nowOrderCnt = 0, reverseOrderCnt = 0;
				if(subjTf == null || subjTf.outEdges.contains(t.predicateID))
					nowOrderCnt ++;
				if(objTf == null || objTf.inEdges.contains(t.predicateID))
					nowOrderCnt ++;
				if(subjTf == null || subjTf.inEdges.contains(t.predicateID))
					reverseOrderCnt ++;
				if(objTf == null || objTf.outEdges.contains(t.predicateID))
					reverseOrderCnt ++;
				
				if(nowOrderCnt<2 && reverseOrderCnt<2)
					return false;
				
				else if(nowOrderCnt > reverseOrderCnt)
				{
					// do nothing
				}
				else if(reverseOrderCnt > nowOrderCnt)
				{
					t.swapSubjObjOrder();
				}
				else	//now order and reverse order both passed type fragment checking, need SELECT one
				{
					//rule1: ?inventor <occupation> ?occupation || ... <name> ?name -> more similar string will be put latter
					String p = Globals.pd.getPredicateById(t.predicateID);
					int ed1 = EntityFragment.calEditDistance(subjWord.baseForm, p);
					int ed2 = EntityFragment.calEditDistance(objWord.baseForm, p);
					if(ed1 < ed2)
					{
						t.swapSubjObjOrder();
					}
				}
				return true;
			}
			///ent & ent || var & ent
			else
			{
				boolean flag = false;
				if (fragmentCompatible(t.subjId, t.predicateID, t.objId)) {			
					flag = true;
				}			
				else if (fragmentCompatible(t.objId, t.predicateID, t.subjId)) {			
					t.swapSubjObjOrder();
					flag = true;
				}	
				
				// Var & Ent |  ?city	<type1>	<City> & <Chile_Route_68>	<country>	?city : <country> is invalid for City | Notice: the data often dirty and can not prune correctly.
				if(flag == true && (t.subject.startsWith("?") || t.object.startsWith("?")))
				{
					Word subjWord = t.getSubjectWord(), objWord = t.getObjectWord();
					TypeFragment subjTf = getTypeFragmentByWord(subjWord), objTf = getTypeFragmentByWord(objWord);
					if(subjTf != null)
					{
						if(subjTf.outEdges.contains(t.predicateID))
							flag = true;
						else if(subjTf.inEdges.contains(t.predicateID))
						{
							t.swapSubjObjOrder();
							flag = true;
						}
						else
							flag = false;
					}
					else if(objTf != null)
					{
						if(objTf.inEdges.contains(t.predicateID))
							flag = true;
						else if(objTf.outEdges.contains(t.predicateID))
						{
							t.swapSubjObjOrder();
							flag = true;
						}
						else
							flag = false;
					}
				}
				
				return flag;
			}
		
		}
	}
	
	public boolean isTripleCompatibleNotSwap (Triple t) {
		if (t.predicateID == Globals.pd.typePredicateID) {
			return true;
		}
		else if (fragmentCompatible(t.subjId, t.predicateID, t.objId)) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean fragmentCompatible (int id1, int pid, int id2) {
		EntityFragment ef1 = efd.getEntityFragmentByEid(id1);
		EntityFragment ef2 = efd.getEntityFragmentByEid(id2);
	
		// valid entity MUST has fragment
		if (id1!=Triple.TYPE_ROLE_ID && id1!=Triple.VAR_ROLE_ID && ef1 == null) return false;
		if (id2!=Triple.TYPE_ROLE_ID && id2!=Triple.VAR_ROLE_ID && ef2 == null) return false;
		
		boolean ef1_constant = (ef1==null)?false:true;
		boolean ef2_constant = (ef2==null)?false:true;
		int entityCnt=0,compatibleCnt=0;
		if(ef1_constant) {
			entityCnt++;
			if (ef1.outEdges.contains(pid))
				compatibleCnt++;
//			else	// <e1,p> Ϊ false pair
//			{
//				falseEntPres.add(new Pair(id1,pid));
//			}
		}
		
		if (ef2_constant) {
			entityCnt++;
			if (ef2.inEdges.contains(pid))
				compatibleCnt++;
//			else	// <p,e2> Ϊfalse pair
//			{
//				falsePreEnts.add(new Pair(pid,id2));
//			}
		}
		
		// for SELECT sparql, EXCAT match between predicate and subject and object, ASK sparql can be relaxed
		if (qlog.s.sentenceType==SentenceType.GeneralQuestion)	
			return entityCnt-compatibleCnt<=1;		
		else		
			return entityCnt==compatibleCnt;
		
	}
	
	public int fragmentCompatible2 (int id1, int pid, int id2) {
		EntityFragment ef1 = efd.getEntityFragmentByEid(id1);
		EntityFragment ef2 = efd.getEntityFragmentByEid(id2);	

		int entityCnt=0,compatibleCnt=0;
		if(id1 != Triple.VAR_ROLE_ID && id1 != Triple.TYPE_ROLE_ID) {
			entityCnt++;
			if (ef1!=null && ef1.outEdges.contains(pid))
				compatibleCnt++;
		}
		
		if (id2 != Triple.VAR_ROLE_ID && id2 != Triple.TYPE_ROLE_ID) {
			entityCnt++;
			if (ef2!=null && ef2.inEdges.contains(pid))
				compatibleCnt++;
		}		

		return entityCnt-compatibleCnt;
	}
		
	public boolean checkConstantConsistency (Sparql spql) {
		HashMap<String, String> constants = new HashMap<String, String>();
		for (Triple t : spql.tripleList) {
			if (!t.subject.startsWith("?")) {
				String e = t.getSubjectWord().getFullEntityName();
				if (!constants.containsKey(e))
					constants.put(e, t.subject);
				else {
					if (!constants.get(e).equals(t.subject))
						return false;
				}				
			}
			if (!t.object.startsWith("?")) {
				String e = t.getObjectWord().getFullEntityName();
				if (!constants.containsKey(e))
					constants.put(e, t.object);
				else {
					if (!constants.get(e).equals(t.object))
						return false;
				}
			}
		}
		return true;
	}
	
	public void reviseScoreByTripleOrders(Sparql spq)
	{
		Triple shouldDel = null;
		for(Triple triple: spq.tripleList)
		{
			// eg, ?who <president> <United_States_Navy> need punished (or dropped).
			if(triple.subject.toLowerCase().equals("?who"))
			{
				String rel = Globals.pd.id_2_predicate.get(triple.predicateID);
				if(rel.equals("president") || rel.equals("starring") || rel.equals("producer"))
				{
					spq.score -= triple.score;
					triple.score /= 10;
					spq.score += triple.score;
					if(triple.semRltn!=null && triple.semRltn.isSteadyEdge == false)
						shouldDel = triple;
				}
			}
		}
		if(shouldDel != null)
			spq.delTriple(shouldDel);
	}
	
	// enumerate subject/object order, fragment check
	// Modify score of "ask one triple"
	public boolean enumerateSubjObjOrders (Sparql originalSpq, Sparql currentSpq, int level) 
	{
		if (level == originalSpq.tripleList.size()) 
		{
			if(currentSpq.tripleList.size() == 0)
				return false;
			
			CompatibilityChecker cc = new CompatibilityChecker(efd);
			
			if (qlog.s.sentenceType==SentenceType.GeneralQuestion) //ask where sparql: no need for fragment check
			{
				if(cc.isSparqlCompatible3(currentSpq))	//reward score for "TRUE"
				{
					for(Triple triple: currentSpq.tripleList)
						triple.addScore(triple.getScore());
				}
				rankedSparqls.add(currentSpq.copy());
				return true;
			}
			try 
			{
				sparqlCheckId++;
				if (cc.isSparqlCompatible3(currentSpq)) 
				{
					//eg, ?who <president> <United_States_Navy>
					//When query graph contains circle, we just prune this edge 
					Sparql sparql = currentSpq.copy();
					reviseScoreByTripleOrders(sparql);
					if(!rankedSparqls.contains(sparql))
						rankedSparqls.add(sparql);
					return true;
				}
			} 
			catch (Exception e) {
				System.out.println("[CompatibilityChecker ERROR]"+currentSpq);
				e.printStackTrace();
			}
			return false;
		}
		
		Triple cur_t = originalSpq.tripleList.get(level);
		
		// first try default order
		currentSpq.addTriple(cur_t);
		boolean flag = enumerateSubjObjOrders(originalSpq, currentSpq, level+1);
		currentSpq.removeLastTriple();
		
		// !deprecated: not change triple order for [literal relation]
//		if (RelationFragment.isLiteral(cur_t.predicateID)) return false;
		
		// Enumerate reserve/drop the type info
		if (cur_t.predicateID == Globals.pd.typePredicateID)
		{
			flag = enumerateSubjObjOrders(originalSpq, currentSpq, level+1);
			return flag;
		}
		else
		{		
			// single triple check after swap
			Triple swapped_t = cur_t.copySwap();
			swapped_t.score = swapped_t.score*0.8;
			if (isTripleCompatibleNotSwap(swapped_t))
			{
				currentSpq.addTriple(swapped_t);
				flag = enumerateSubjObjOrders(originalSpq, currentSpq, level+1);
				currentSpq.removeLastTriple();
			}
			return flag;
		}
	}

}
