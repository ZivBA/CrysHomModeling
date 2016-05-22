package meshi.util.crossLinking;

import meshi.parameters.Residues;

/**
 * Cut after Arg and Lys but only if they are not followed by Pro.
 * 
 * @author Nir
 *
 */
public class Trypsin extends Peptidase implements Residues {

	public Trypsin() {}

	@Override
	boolean cutC(String seq, int residueNumber) {
		if (residueNumber==seq.length()) { // Cterm
			return true;
		}	
		if (seq.charAt(residueNumber) == 'P') {// No cutting before Proline
			return false;
		}
		return (seq.charAt(residueNumber - 1) == 'R') ||
				(seq.charAt(residueNumber - 1) == 'K');
	}

	@Override
	boolean cutN(String seq, int residueNumber) {
		if (residueNumber==1) // Nterm
			return true; 
		if (seq.charAt(residueNumber-1) == 'P') {// No cutting before Proline
			return false;
		}
		return (seq.charAt(residueNumber - 2) == 'R') ||
				(seq.charAt(residueNumber - 2) == 'K');
	}

}
