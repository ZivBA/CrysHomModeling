package meshi.sequences;

import meshi.sequences.aligner.DpMatrix;
import meshi.sequences.aligner.Identity;
import meshi.util.filters.Filter;

import java.util.Iterator;

public class SequenceAlignment extends Alignment{
    private Double score = null;
    public SequenceAlignment() {
	super(new IsAlignmentColumn());
    }

    public SequenceAlignment(ResidueAlignment residueAlignment) {
	this();
	for (Iterator resColumns = residueAlignment.iterator(); resColumns.hasNext();)
	    add(new SequenceAlignmentColumn((ResidueAlignmentColumn) resColumns.next()));
	comments.add(residueAlignment.comments);
    }
    protected SequenceAlignment(Sequence sequence1, Sequence sequence2) {
	this();
	SequenceList sequenceList = new SequenceList();
	sequenceList.add(sequence1);
	sequenceList.add(sequence2);
	SequenceAlignment temp = new SequenceAlignment(sequenceList);
	add(temp);
	comments.add(temp.comments);
    }
			
    private SequenceAlignment(SequenceList sequenceList) {
	this();
	int numberOfSequences = sequenceList.size();
	boolean done = false;

	int length = ((Sequence) sequenceList.elementAt(0)).size();
	for (int i = 0; i < numberOfSequences; i++) {
	    Sequence sequence = (Sequence) sequenceList.elementAt(i);
	    if (sequence.size() != length) {
		System.out.println("A problem with sequences:");
		sequenceList.print();
		throw new RuntimeException("All sequences must have the same Length");
	    }
	    comments.add(sequence.comment());
	}
	    
	for (int iPosition = 0; iPosition < length; iPosition++) {
	    SequenceAlignmentColumn column = new SequenceAlignmentColumn(numberOfSequences);
	    for (int iSequence = 0; iSequence < numberOfSequences; iSequence++) {
		Sequence sequence = (Sequence) sequenceList.elementAt(iSequence);
		column.add(iSequence, sequence.cell(iPosition));
	    }
	    add(column);
	}
    } 
    
    private static class IsAlignmentColumn implements Filter {
	public boolean accept(Object obj) {
	    return (obj instanceof SequenceAlignmentColumn);
	}
    }



    public String toString() {
	SequenceList sequenceList = new SequenceList(this);
	String out = "";
	for (Iterator sequences = sequenceList.iterator(); sequences.hasNext();)
	    out+=""+sequences.next();
	return out;
    }

    public boolean isExactMach() {
	for (Iterator columns = iterator(); columns.hasNext();)
	    if (! ((SequenceAlignmentColumn) columns.next()).isExactMach()) return false;
	return true;
    }
                   
    boolean isExactMachWithGaps() {
	for (Iterator columns = iterator(); columns.hasNext();)
	    if (! ((SequenceAlignmentColumn) columns.next()).isExactMachWithGaps()) return false;
	return true;
    }

    public static SequenceAlignment identityAlignment(Sequence sequence1, Sequence sequence2) {
	DpMatrix matrix = new DpMatrix(sequence1, sequence2, new Identity(-0.2));
	    return matrix.backTrack();
    }
	
    public double score() {
	if (score == null) throw new RuntimeException("Alignment without score");
	return score.doubleValue();
    }

    public void setScore(double score) {
	this.score = score;
    }
}
