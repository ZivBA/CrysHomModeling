package meshi.energy.compositeTorsions.smoothRotamerLibrary;

import meshi.energy.compositeTorsions.ResidueTorsions;

/** Smooth rotamer library parameters for amino acids with no
 * sidechain torsion angles: ALA, GLY.
 * 
 * @author El-ad David Amir
 *
 */
public class SmoothRotamerLibraryParametersChi0 extends
		SmoothRotamerLibraryParameters {

	public SmoothRotamerLibraryParametersChi0(
			int residueType) {
		super( residueType );
		
		polynomials = null;
	}
	
	protected boolean legalResidueType() {
		return (getResidueType() == ALA || getResidueType() == GLY);
	}
	
	public double evaluate(int derivVar, ResidueTorsions resTorsions) {
		/* no sidechain, evaluation is always zero */
		return 0;
	}

}
