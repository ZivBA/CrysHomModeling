package meshi.energy.alphaTorsion;

// --Commented out by Inspection START (16:34 31/10/16):
//public class AlphaTorsionCreator extends EnergyCreator implements KeyWords {
//    public AlphaTorsionCreator(double weight) {
//	super(weight);
//    }
//    public AlphaTorsionCreator() {
//	super(ALPHA_TORSION_ENERGY);
//    }
//    public AbstractEnergy createEnergyTerm(Protein protein, DistanceMatrix distanceMatrix,
//					  CommandList commands) {
//
//	if (parametersList== null) {
//	    parametersList = new AlphaTorsionParametersList(parametersDirectory(commands)+
//						     "/"+ALPHA_TORSION_PARAMETERS);
//	}
//	AngleList angleList = AngleList.getCaAngles(protein,distanceMatrix).namedFilter();
//	TorsionList torsionList = new TorsionList(angleList, distanceMatrix);
//	TorsionList relevantTorsionList = (TorsionList)torsionList.filter(new HaveParametersFilter(parametersList),
//									 new TorsionList());
//
//	return new AlphaTorsionEnergy(relevantTorsionList, distanceMatrix, (AlphaTorsionParametersList) parametersList, weight());
//    }
//}
// --Commented out by Inspection STOP (16:34 31/10/16)
