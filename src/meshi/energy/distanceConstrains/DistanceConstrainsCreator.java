package meshi.energy.distanceConstrains;

// --Commented out by Inspection START (16:34 31/10/16):
//public class DistanceConstrainsCreator extends EnergyCreator  implements KeyWords {
//    public DistanceConstrainsCreator() {
//	super(DISTANCE_CONSTRAINS_ENERGY);
//    }
//
//    public AbstractEnergy createEnergyTerm(Protein protein, CommandList commands) {
//	return createEnergyTerm(protein, null, commands);
//    }
//
//    public AbstractEnergy createEnergyTerm(Protein protein, DistanceMatrix distanceMatrix,
//					  CommandList commands) {
//	Command command = commands.firstWordFilter(key).secondWord(INPUT_FILE);
//	String distanceConstrainsFile = command.thirdWord();
//	if (parametersList== null)
//		parametersList = new DistanceConstrainParametersList(distanceConstrainsFile);
//	return new DistanceConstrainsEnergy(protein, (DistanceConstrainParametersList) parametersList, weight());
//    }
//}
// --Commented out by Inspection STOP (16:34 31/10/16)
