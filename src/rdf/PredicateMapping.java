package rdf;

public class PredicateMapping implements Comparable<PredicateMapping> {
	public int pid = -1;
	public double score = 0;
	public String parapharase = null;
	
	public PredicateMapping (int pid, double sco, String para) {
		this.pid = pid;
		score = sco;
		parapharase = para;
	}
	
	// In descending order: big --> small
	public int compareTo(PredicateMapping o) {
		double diff = this.score - o.score;
		if (diff > 0) return -1;
		else if (diff < 0) return 1;
		else return 0;
	}
	
	@Override
	public String toString() {
		String ret = "";
		ret = "<"+pid+" : "+parapharase+" : "+score+">";
		return ret;
	}
}
