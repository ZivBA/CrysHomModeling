package meshi.sequences;
/**
 * A container for an ordered set of coresponding protein elements. 
 * Each of the elements comes form a different protein.
 **/
public class AlignmentColumn{
    final AlignmentCell[] cells;

 
    /**
     * Utility constructor for the sub-classes.
     **/
    AlignmentColumn(int numberOfRows){
	cells = new AlignmentCell[numberOfRows];
    }

    AlignmentColumn(AlignmentCell cell0, AlignmentCell cell1){
	cells = new AlignmentCell[2];
	cells[0] = cell0;
	cells[1] = cell1;
    }
    
    public void add(int index, AlignmentCell cell) {
	cells[index] = cell;
    }

    AlignmentCell cell0() {return cell(0);}
    AlignmentCell cell1() {return cell(1);}

    public AlignmentCell cell(int index) {
	if (cells[index] == null) throw new RuntimeException("empty cell");
	return cells[index];
    }

    public boolean hasGap() {
	int length = cells.length;
	    for (AlignmentCell cell : cells)
		    if (cell.gap())
			    return true;
	return false;
    }
 
   public boolean allGaps() {
	int length = cells.length;
	for (int i = 0; i < length; i++){
	    if (! cells[i].gap()) return false;
	}
	return true;
    }

    public String toString() {
	String out = "";
	    for (AlignmentCell cell : cells) {
		    out += cell.obj + "_" + cell.number + "\t";
	    }
	return out;
    }

    public int size() {return cells.length;}
}
