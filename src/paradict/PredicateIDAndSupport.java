package paradict;

public class PredicateIDAndSupport implements Comparable<PredicateIDAndSupport> {
	public int predicateID;
	public int support;
	public double[] wordSelectivity = null;	// wordSelectivity helps PATTY patterns ranking more accurate.
	
	public PredicateIDAndSupport(int _pid, int _support, double[] _slct) {
		predicateID = _pid;
		support = _support;
		wordSelectivity = _slct;
	}

	public int compareTo(PredicateIDAndSupport o) {
		return o.support - this.support;
	}

	// only use for predicate itself and handwriting paraphrase
	public static double[] genSlct(int size) {
		double[] ret = new double[size];
		for (int i=0;i<size;i++) ret[i] = 1.0;
		return ret;
	}
}
