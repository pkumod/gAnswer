package lcn;

public class EntityNameAndScore implements Comparable<EntityNameAndScore> {
	public int entityID;
	public String entityName;
	public double score;
	
	public EntityNameAndScore(int id, String n, double s) {
		entityID = id;
		entityName = n;
		score = s;		
	}
	
	@Override
	public String toString() {
		return entityID + ":<" + entityName + ">\t" + score;
	}

	public int compareTo(EntityNameAndScore o) {
		if(this.score < o.score) {
			return 1;
		}
		else if (this.score > o.score) {
			return -1;
		}
		else {
			return 0;
		}
	}

}
