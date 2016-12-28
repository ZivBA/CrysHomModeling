package meshi.energy.compositeTorsions.smoothRotamerLibrary;

// --Commented out by Inspection START (16:34 31/10/16):
//public class SmoothRotamerLibraryEnergyCreator
//	extends EnergyCreator
//	implements KeyWords, MeshiPotential {
//
//	public SmoothRotamerLibraryEnergyCreator(double weight) {
//	super(weight);
//    }
//
//    public SmoothRotamerLibraryEnergyCreator() {
//		super( 1.0 );
//	}
//
//	public AbstractEnergy createEnergyTerm(Protein protein,
//			DistanceMatrix distanceMatrix, CommandList commands) {
//		/* retrieve parameters */
//		String srlplFileName =
//			parametersDirectory(commands)+"/"+COMPOSITE_TORSIONS_PARAMETERS;
//		SmoothRotamerLibraryParametersList srlpl =
//			new SmoothRotamerLibraryParametersList(srlplFileName);
//
//		/* create residue torsions list for protein */
//		ResidueTorsionsList rtl = (new ResidueTorsionsList(protein, distanceMatrix )).filterCompleteResidues();
//
//		/* return energy */
//		return new SmoothRotamerLibraryEnergy(
//				rtl, distanceMatrix, srlpl, weight(), "smooth" );
//	}
//
//}
// --Commented out by Inspection STOP (16:34 31/10/16)
