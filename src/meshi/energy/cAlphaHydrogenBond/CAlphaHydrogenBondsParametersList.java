package meshi.energy.cAlphaHydrogenBond;

import meshi.energy.Parameters;
import meshi.energy.ParametersList;

import java.util.StringTokenizer;

public class CAlphaHydrogenBondsParametersList extends ParametersList {

	public CAlphaHydrogenBondsParametersList(String parametersFileName){
		super(parametersFileName, false);
	}

	public Parameters createParameters(String line) {
			return new CAlphaHydrogenBondsParameters(new StringTokenizer(line));
	}
        
    public Parameters parameters(Object obj) {
	return null;
    }
}
