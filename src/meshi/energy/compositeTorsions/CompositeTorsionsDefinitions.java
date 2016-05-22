package meshi.energy.compositeTorsions;

/** Definitions and constants used throughout the CompositeTorsionsEnergy
 * term.
 * 
 * @author El-ad David Amir
 *
 */
public interface CompositeTorsionsDefinitions {

	/* torsion types */
	int OMG = 0;
	int PHI = 1;
	int PSI = 2;
	int CHI_1 = 3;
	int CHI_2 = 4;
	int CHI_3 = 5;
	int CHI_4 = 6;
	int TOTAL_TORSION_ANGLES = 7;
	
	int UNIDENTIFIED_TORSION_TYPE = -1;
	
	/* number of sidechain torsions for each residue type */
	int NUM_SIDECHAIN_TORSIONS[] = {
		/* ALA */ 0,
		/* CYS */ 1,
		/* ASP */ 2,
		/* GLU */ 3,
		/* PHE */ 2,
		/* GLY */ 0,
		/* HIS */ 2,
		/* ILE */ 2,
		/* LYS */ 4,
		/* LEU */ 2,
		/* MET */ 3,
		/* ASN */ 2,
		/* PRO */ 2,
		/* GLN */ 3,
		/* ARG */ 4,
		/* SER */ 1,
		/* THR */ 1,
		/* VAL */ 1,
		/* TRP */ 2,
		/* TYR */ 2,
		/* PREPRO */ 0	};
	
	/* the Pre-Proline amino acid type (used in propensity) */
	int PREPRO  = 20;

	/* the OMNI amino acid type (used in propensity) */
	int OMNI  = 21;

	/* secondary structure types */
	int HELIX = 1;
	int SHEET = 2;
	int COIL  = 3;
	int ALL   = 9;
	
	/* splined polynomials types */
	int POLYNOMIAL_PHI_PSI       = 0;
	int POLYNOMIAL_PHI_PSI_CHI_1 = 1;
  	int POLYNOMIAL_CHI_1_CHI_2   = 2;
	int POLYNOMIAL_CHI_1         = 3;
	int POLYNOMIAL_CHI_1_CHI_3   = 4;
	int POLYNOMIAL_CHI_1_CHI_4   = 5;
	
	/* splined polynomials torsion angles */
	int POLYNOMIAL_PHI_PSI_TORSIONS[]       = { PHI, PSI };
	int POLYNOMIAL_PHI_PSI_CHI_1_TORSIONS[] = { PHI, PSI, CHI_1 };
	int POLYNOMIAL_CHI_1_CHI_2_TORSIONS[]   = { CHI_1, CHI_2 };
	int POLYNOMIAL_CHI_1_TORSIONS[]         = { CHI_1 };
	int POLYNOMIAL_CHI_1_CHI_3_TORSIONS[]   = { CHI_1, CHI_3 };
	int POLYNOMIAL_CHI_1_CHI_4_TORSIONS[]   = { CHI_1, CHI_4 };
}

