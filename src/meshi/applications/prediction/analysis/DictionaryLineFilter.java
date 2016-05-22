package meshi.applications.prediction.analysis;
import java.util.StringTokenizer;

public class DictionaryLineFilter extends MeshiLogLineFilter {
    public boolean accept(Object obj) {
	StringTokenizer tokenizer = acceptLine((String) obj);
	if (tokenizer == null) return false;

	if (!tokenizer.hasMoreTokens()) return false;
	String token = tokenizer.nextToken();
	    return token.equals(DICTIONARY_KEY.key);
    }
}
