package meshi.applications.prediction.analysis;

import meshi.util.KeyWords;
import meshi.util.filters.Filter;

import java.util.StringTokenizer;

public class ValueLineFilter implements Filter, KeyWords {
    public boolean accept(Object obj) {
	String line = (String) obj;
	StringTokenizer tokenizer = new StringTokenizer(line);
	if (!tokenizer.hasMoreTokens()) return false;
	String token = tokenizer.nextToken();
	if (! token.equals(MESHILOG_KEY.key)) return false;
	if (!tokenizer.hasMoreTokens()) return false;
	token = tokenizer.nextToken();
	    return token.equals(VALUE_KEY.key);
    }
}
