package meshi.applications.prediction.analysis;

import meshi.util.KeyWords;
import meshi.util.filters.Filter;

import java.util.StringTokenizer;

public class MeshiLogLineFilter implements Filter, KeyWords {
    static StringTokenizer acceptLine(String line) {
	StringTokenizer tokenizer = new StringTokenizer(line);
	if (! tokenizer.hasMoreTokens()) return null;
	String token = tokenizer.nextToken();
	if (! token.equals(MESHILOG_KEY.key)) return null;
	return  tokenizer;
    }
    
    public boolean accept(Object obj) {
	String line = (String) obj;
	StringTokenizer tokenizer = acceptLine(line);
	    return tokenizer != null;
    }
}
