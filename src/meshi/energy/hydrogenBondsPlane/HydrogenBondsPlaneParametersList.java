
package meshi.energy.hydrogenBondsPlane;

import meshi.energy.Parameters;
import meshi.energy.ParametersList;


public class HydrogenBondsPlaneParametersList  extends ParametersList {

	public HydrogenBondsPlaneParametersList(String parametersFileName){
		super(parametersFileName, false);                                           
	}

        public Parameters createParameters(String line) {
		return new HydrogenBondsPlaneParameters();
	}

   public Parameters parameters(Object obj) {
    	return null;
    }

}
