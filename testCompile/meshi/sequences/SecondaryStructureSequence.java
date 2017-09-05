package meshi.sequences;

public class SecondaryStructureSequence  extends Sequence {
    private static final SecondaryStructureSequenceCharFilter secondaryStructureCharFilter =  new SecondaryStructureSequenceCharFilter();
    public SecondaryStructureSequence(String sequence, String comment) {
	super(sequence, comment, secondaryStructureCharFilter);
    }
    
        private static class SecondaryStructureSequenceCharFilter extends SequenceCharFilter {
	public boolean accept(Object obj) {
	    Character c = (Character) obj;
		return "HEC".indexOf(c) >= 0;
	}
    }
}
