package meshi.sequences;
import meshi.util.Attributable;
import meshi.util.AttributesRack;
import meshi.util.MeshiAttribute;

public class SequenceAlignmentCell extends AlignmentCell implements Attributable, MeshiAttribute {
    public static final char GAP_CHAR = '-';
    private static final Character GAP = GAP_CHAR;
    private final AttributesRack attributes = new AttributesRack();
    public static final char WILDCARD_CHAR = 'X';
    private static final char WILDCARD = WILDCARD_CHAR;

    public SequenceAlignmentCell(char c, int number) {
	super(c, number);
    }
    public SequenceAlignmentCell(int number) {
	super(GAP, number);
    }
    public SequenceAlignmentCell() {
	super(GAP, -1);
    }
 	
    public char getChar() {return (Character) obj;}
    public String getCharAsString() {
	char[] cc = new char[1];
	cc[0] = getChar();
	return new String(cc);
    }

    public boolean gap() {return (obj.equals(GAP) || obj.equals(WILDCARD));}
    public boolean wildcard() {return obj.equals(WILDCARD);}
    public boolean equals(Object obj) {
	SequenceAlignmentCell other = (SequenceAlignmentCell) obj;
	return (getChar() == other.getChar());
    }

    public String toString() {
	return ""+getChar()+" "+number;
    }

    public void addAttribute(MeshiAttribute attribute) {
	attributes.addAttribute(attribute);
    }
    public MeshiAttribute getAttribute(int key) {
	return attributes.getAttribute(key);
    }
    public MeshiAttribute getAttribute() {
	return getAttribute(key());
    }
    public int key() {return SEQUENCE_ALIGNMENT_COLUMN_ATTRIBUTE;}
}
    
