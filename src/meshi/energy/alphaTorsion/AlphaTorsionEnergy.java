package meshi.energy.alphaTorsion;
import meshi.energy.EnergyElement;
import meshi.energy.Parameters;
import meshi.energy.SimpleEnergyTerm;
import meshi.geometry.DistanceMatrix;
import meshi.geometry.Torsion;
import meshi.geometry.TorsionList;

// --Commented out by Inspection START (16:38 31/10/16):
///**
// *This energy limits the alpha torsion (4 consecutive CAs) to be in a specific range.
// *It is also secondary structure sensitive, and the range depends on the residue SS.
// *It operates currently only on HELIX,SHEET secondary structure states, since the COIL,ALL
// *states practically don't have any limitation on the alpha torsion.
// *
// *Important Note: This energy term must be accompanied by an ALPHA-angle energy term. This is
// *because it has a non-continous point at torsion values of -Pi or Pi , and also when
// *one of the 2 angles that make up the torsion is close to 0 or Pi. These discontinuites should
// *not affect normal operation if the ALPHA-angle term is working. On very rare starting condition
// *these problems might never the less be encountered.
// **/
//
//
//public class AlphaTorsionEnergy extends SimpleEnergyTerm {
//
//	public AlphaTorsionEnergy() {}
//
//    public AlphaTorsionEnergy(TorsionList torsionList, DistanceMatrix distanceMatrix,
//		       AlphaTorsionParametersList parametersList, double weight) {
//	super(toArray(distanceMatrix, torsionList), parametersList, weight);
//	    TorsionList torsionList1 = torsionList;
//	    DistanceMatrix distanceMatrix1 = distanceMatrix;
//	createElementsList(torsionList);
//	comment = "alphaTorsion";
//    }
//
//
//
//
//    public EnergyElement createElement(Object baseElement, Parameters parameters) {
//	return new AlphaTorsionEnergyElement((Torsion)baseElement,
//	                                    (AlphaTorsionParameters) parameters, weight);
//    }
//
//    public void handleMissingParameters(Object obj) {}
//
//
//}
// --Commented out by Inspection STOP (16:38 31/10/16)
