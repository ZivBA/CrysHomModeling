package meshi.util.string;

import meshi.util.MeshiException;
import meshi.util.filters.Filter;

import java.util.Iterator;

public abstract class StringFilter  implements Filter{
    private final StringList keys;
    StringFilter(String key) {
	this(new StringList(key));
    }
    StringFilter(StringList keys) {
	this.keys = keys;
    }
    public boolean accept(Object obj) {
	if (obj instanceof String) {
	    Iterator keysIterator = keys.iterator();
	    String key;
	    while ((key = (String) keysIterator.next()) != null)
		if (accept((String) obj,key)) return true;
	    return false;
	}
        throw new MeshiException("Tried to StringFilter:\n"+
                              obj+" of class: "+obj.getClass());
    }
    protected abstract boolean accept(String string, String key);
}
