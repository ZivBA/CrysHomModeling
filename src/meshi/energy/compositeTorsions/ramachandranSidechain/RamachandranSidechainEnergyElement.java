package meshi.energy.compositeTorsions.ramachandranSidechain;

import meshi.energy.EnergyElement;
import meshi.energy.compositeTorsions.CompositeTorsionsDefinitions;
import meshi.energy.compositeTorsions.ResidueTorsions;
import meshi.parameters.Residues;

/** Encapsulation of RamachandranSidechain energy value for a single residue.
 * Much like the parameters for this energy function, each residue type
 * has its own class of energy element.
 * @author El-ad David Amir
 *
 */
public abstract class RamachandranSidechainEnergyElement
	extends EnergyElement
	implements Residues, CompositeTorsionsDefinitions {

	final ResidueTorsions residueTorsions;
	final RamachandranSidechainParameters rsp;
	final double weight;

	RamachandranSidechainEnergyElement(
			ResidueTorsions residueTorsions,
			RamachandranSidechainParameters rsp,
			double weight) {
		this.residueTorsions = residueTorsions;
		this.rsp = rsp;
		this.weight = weight;
		
		if( !legalResidueType() )
			throw new RuntimeException( "energy element residue type mismatch" );
		
		setAtoms();
		updateFrozen();
	}

	protected void setAtoms() {
		int[] interestingTorsions = {PHI,PSI,CHI_1,CHI_2,CHI_3,CHI_4};
		atoms = residueTorsions.getAtoms(interestingTorsions);		
	}

	/** Reports energy values. Currently switched off. */
	void monitor() {
	}
	
	/** verifies residue type is a legal residue types for class. */
	protected abstract boolean legalResidueType();
	
}
