package alignment;

public interface Position {
	
	/*
	 * The penalty involving a gap opening against this position
	 */
	double gapOpeningScore();
	
	/*
	 * The penalty involving aligning this position to a gap
	 */
	double gapAligningScore();
	
	/*
	 * A string for printing out the position in the alignment
	 */
	String string();

	/*
	 * A string for printing out a gap
	 */
	String gapString();

	
}
