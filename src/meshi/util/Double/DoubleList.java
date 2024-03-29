package meshi.util.Double;

import meshi.util.MeshiList;
import meshi.util.filters.Filter;
import meshi.util.string.StringList;

import java.util.Iterator;

// A list of Double elements
// #1 variables
// #2 constructors
// #3 methods
//  #3a double shortcuts
//  #3b house keeping
public class DoubleList extends MeshiList {
    // #2 constructors
    public DoubleList() { // empty list
	super(new IsDouble());
    }
    public DoubleList(int size) { // list of zeros
	this();
	for (int i = 0;i < size; i++)
	    addDouble(0);
    }
    // convert a string to a list of Doubles 
    public DoubleList(String aStringWithDoubleNumbers){ 
	this(); // create an empty list
	String doubleString;
	Iterator SI;
	// convert the string to an iterator to a list of strings. 
	SI = (new StringList(aStringWithDoubleNumbers,
			     StringList.standardSeparators())).iterator();
        // convert each string to a Double and add it 
	while ((doubleString = (String) SI.next()) != null)
	    add(new Double(doubleString.trim()));
    }

    //#3a double shortcuts 
    private void addDouble(double d) {
	add(d);
    }
    public double doubleAt(int index) {
	return (Double) elementAt(index);
    }

    static class IsDouble implements Filter {
	public boolean accept(Object obj) {
		return (obj instanceof Double);
	}
    }
    public boolean sortable() {return true;}
}
