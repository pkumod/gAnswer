package rdf;

import java.util.ArrayList;

public class NodeSelectedWithScore implements Comparable<NodeSelectedWithScore>
{
	public ArrayList<Integer> selected;
	int size; //split key to st and ed
	public double score = 0;
	
	public NodeSelectedWithScore(ArrayList<Integer> a, double b)
	{
		selected = a;
		score = b;
	}
	
	// In descending order: big --> small
	public int compareTo(NodeSelectedWithScore o) {
		double diff = this.score - o.score;
		if (diff > 0) return -1;
		else if (diff < 0) return 1;
		else return 0;
	}
}