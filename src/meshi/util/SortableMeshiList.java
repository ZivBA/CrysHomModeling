package meshi.util;

import meshi.util.filters.Filter;

import java.util.Arrays;
import java.util.Comparator;

public class SortableMeshiList extends MeshiList {
    protected int lastSorted;

    protected SortableMeshiList() {
	super();
    }
    public SortableMeshiList(int capacity) {
    	super(capacity);
    }

    protected SortableMeshiList(Filter filter) {
	super(filter);
	lastSorted = -1;
    }

     protected SortableMeshiList(Filter filter, int capacity) {
	super(filter, capacity);
	lastSorted = -1;
    }

    public boolean sorted() {
	return (lastSorted == modCount);
    }

    public void sort() {
	trim();
	Arrays.sort(internalArray);
	modCount++;
	lastSorted = modCount;
    }
    
	@SuppressWarnings("unchecked")
	private void sort(Comparator comparator) {
	    trim();
	    Arrays.sort(internalArray, comparator);
	    modCount++;
	    lastSorted = modCount;
    }

    public boolean contains(Object obj) {
	if (lastSorted == modCount) {
	    return (binarySearch(obj) >= 0);
	    }
	else 
	    for(int i = 0; i < size; i++)
		if (obj.equals(internalArray[i])) return true;
	return false;
    }

    protected int binarySearch(Object obj) {
	    return Arrays.binarySearch(internalArray, obj);
   }    

   protected MeshiList extractLowest(Comparator comparator, int n, MeshiList newList) {
	   if (newList.size() != 0) throw new RuntimeException("newList must be empty");
	   if (n > size()) 
		   throw new RuntimeException("Cannot extract "+n+" element from a list of size "+size());
	   sort(comparator);
	   for (int i = 0; i < n; i++) 
		   newList.add(elementAt(i));
           return newList;
   }
}


