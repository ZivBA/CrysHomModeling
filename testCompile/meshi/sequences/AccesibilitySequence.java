package meshi.sequences;

public class AccesibilitySequence  extends Sequence {
    private static final AccesibilitySequenceCharFilter AccesibilityCharFilter =  new AccesibilitySequenceCharFilter();
    public AccesibilitySequence(String sequence, String comment) {
	super(sequence, comment, AccesibilityCharFilter);
    }
    
        private static class AccesibilitySequenceCharFilter extends SequenceCharFilter {
	public boolean accept(Object obj) {
	    Character c = (Character) obj;
		return "AB".indexOf(c) >= 0;
	}
    }
}
