package meshi.applications.loopBuilding.applications;

import meshi.applications.minimizeProtein.ExtendedAtomsProtein;
import meshi.energy.EnergyCreator;
import meshi.energy.TotalEnergy;
import meshi.energy.angle.AngleCreator;
import meshi.energy.bond.BondCreator;
import meshi.energy.outOfPlane.OutOfPlaneCreator;
import meshi.energy.plane.PlaneCreator;
import meshi.geometry.DistanceMatrix;
import meshi.geometry.rotamers.DunbrackLib;
import meshi.molecularElements.Atom;
import meshi.molecularElements.AtomList;
import meshi.molecularElements.Protein;
import meshi.molecularElements.Residue;
import meshi.molecularElements.residuesExtendedAtoms.ResidueExtendedAtoms;
import meshi.optimizers.LBFGS;
import meshi.optimizers.LineSearchException;
import meshi.optimizers.Minimizer;
import meshi.optimizers.MinimizerException;
import meshi.parameters.AtomTypes;
import meshi.parameters.Residues;
import meshi.util.CommandList;
import meshi.util.MeshiProgram;
import meshi.util.file.File2StringArray;
import meshi.util.file.MeshiWriter;
import programs.SCMOD;

public class SimpleHomologyModeling extends MeshiProgram implements Residues, AtomTypes {

	private static CommandList commands; 

	private static String alignmentFileName = null;  

	public static void main(String[] args) throws MinimizerException, LineSearchException{
		init(args); 
		
		Protein template = null; 
		Protein query = null;
		int firstResInTemplate = -1;
		int firstResInQuery = -1;
		String queryAlignment = "";
		String templateAlignment = "";
		String output="";
		int resNumCounterQ=0;
		int resNumCounterT=0;
		boolean[] completedResidue; 


		
		// Reading the alignment file
		String[] alignmentStrings = File2StringArray.f2a(alignmentFileName);
		System.out.println("Reading template: " + alignmentStrings[0].trim());
		template = new ExtendedAtomsProtein(alignmentStrings[0].trim(),DO_NOT_ADD_ATOMS);
		firstResInQuery = new Integer(alignmentStrings[1]);
		System.out.println("Alignment in query starts at: " + firstResInQuery);
		firstResInTemplate = new Integer(alignmentStrings[2]);
		System.out.println("Alignment in template starts at: " + firstResInTemplate);
		queryAlignment = alignmentStrings[3].trim();
		System.out.println("QUERY " + queryAlignment);
		System.out.println("      " + alignmentStrings[4]);
		templateAlignment = alignmentStrings[5].trim();
		System.out.println("TEMPL " + templateAlignment);
		output = alignmentStrings[6].trim();
		System.out.println("output to: " + output);
		completedResidue = new boolean[queryAlignment.length()];
		for (int c=0; c<queryAlignment.length() ; c++)
			completedResidue[c] = true;

    	// Making the segmod file
		System.out.println("Cut under line for SEGMOD alignment file.\n***********************************************************");
		String queryAlignmentSegmod = "";
		String templateAlignmentSegmod = "";
		for (int c=0; c<queryAlignment.length() ; c++) {
			if (!((queryAlignment.charAt(c)=='-') & (templateAlignment.charAt(c)=='-'))) {
				queryAlignmentSegmod += queryAlignment.charAt(c);
				templateAlignmentSegmod += templateAlignment.charAt(c);
			}
		}
		String segmodString = queryAlignmentSegmod.length() + " " + "Template_" + alignmentFileName + " XXX\n";
    	for (int qCounter=0; qCounter<queryAlignmentSegmod.length() ; qCounter++) {
    		segmodString += " " + templateAlignmentSegmod.charAt(qCounter);
    		if ((qCounter % 30) == 29) {
    			segmodString += "\n";
    		}
    	}
    	if ((queryAlignmentSegmod.length() % 30) != 0) {
    		segmodString += "\n";
    	}
    	segmodString += queryAlignmentSegmod.length() + " " + "SEGMOD_" + alignmentFileName + " YYY\n";
    	for (int qCounter=0; qCounter<queryAlignmentSegmod.length() ; qCounter++) {
    		segmodString += " " + queryAlignmentSegmod.charAt(qCounter);
    		if ((qCounter % 30) == 29) {
    			segmodString += "\n";
    		}
    	}
    	if ((queryAlignmentSegmod.length() % 30) != 0) {
    		segmodString += "\n";
    	}
		System.out.println(segmodString);
		System.out.println("***********************************************************");
		
		
		
		// Building the protein instance of the trivial homology
		AtomList CAslist = new AtomList();
		resNumCounterQ = firstResInQuery-1;
		resNumCounterT = firstResInTemplate-1;
		for (int position=0; position<queryAlignment.length() ; position++) {
			if (queryAlignment.charAt(position)!='-') 
				resNumCounterQ++;
			if (templateAlignment.charAt(position)!='-') 
				resNumCounterT++;		
			if (queryAlignment.charAt(position)!='-') {
				if ((templateAlignment.charAt(position)!='-') && (template.residue(resNumCounterT)!= null) && (template.residue(resNumCounterT).ca()!= null)) {
					if (Residue.nameOneLetter(template.residue(resNumCounterT).type).charAt(0)!=templateAlignment.charAt(position)) {
						throw new RuntimeException("Mismatch between template structure and sequence");
					}
					else {
						CAslist.add(new Atom(0,0,0,"CA",
								Residue.one2three(queryAlignment.charAt(position)),
								resNumCounterQ,-1));
					}
				}
			}
		}
		query = new Protein(CAslist, new ResidueExtendedAtoms(ADD_ATOMS));
				 
		// Assigning the coordinates to the trivial homology
		resNumCounterQ = firstResInQuery-1;
		resNumCounterT = firstResInTemplate-1;
		for (int position=0; position<queryAlignment.length() ; position++)  {	
			if (queryAlignment.charAt(position)!='-') 
				resNumCounterQ++;
			if (templateAlignment.charAt(position)!='-') 
				resNumCounterT++;		
			if ((queryAlignment.charAt(position)!='-') && (templateAlignment.charAt(position)!='-') &&
				(template.residue(resNumCounterT)!= null) && (template.residue(resNumCounterT).ca()!= null)) {
				for (int atomCounter=0 ; atomCounter<query.residue(resNumCounterQ).atoms().size() ; atomCounter++) {
					if (query.residue(resNumCounterQ).atoms().atomAt(atomCounter).name().equals("CA")) {
						System.out.println(resNumCounterQ + " " + queryAlignment.charAt(position) + " " + resNumCounterT + " " + templateAlignment.charAt(position));
					}
					Atom atomInQuery = query.residue(resNumCounterQ).atoms().atomAt(atomCounter);
					Atom atomInTemplate = template.residue(resNumCounterT).atoms().getAtom(atomInQuery.name());
					if (atomInTemplate!=null) {
						atomInQuery.setXYZ(atomInTemplate.x(), atomInTemplate.y(), atomInTemplate.z());
						atomInQuery.freeze();
					}
					else {
						if (!atomInQuery.isHydrogen) // We are missing a heavy atom in the template
							completedResidue[position] = false;
						atomInQuery.defrost();
					}
				}
			}
		}

		// Brining new atoms close to their bonded atoms
		for (int atomCounter=0 ; atomCounter<query.atoms().size() ; atomCounter++) {
			Atom atom = query.atoms().atomAt(atomCounter);
			if (!atom.frozen())
				atom.setXYZ(atom.bonded().atomAt(0).x()+Math.random(), atom.bonded().atomAt(0).y()+Math.random(), atom.bonded().atomAt(0).z()+Math.random());
		}


		// First Minimization - This will mainly set the hydrogens right.
		EnergyCreator[] energyCreators = {  
				new BondCreator(),
				new AngleCreator(),
				new PlaneCreator(),
				new OutOfPlaneCreator()
		};
		DistanceMatrix distanceMatrix = new DistanceMatrix(query.atoms(), 5.5,  2.0,  4);  
		TotalEnergy energy = new TotalEnergy(query, distanceMatrix, energyCreators, commands);
		Minimizer minimizer = new LBFGS(energy, 0.05 , 100000 , 100 );
		try {
			System.out.println(minimizer.minimize());
		}
		catch (Exception e) {
			System.out.println(e + "\n\n\nAn error in the minimization. Continueing...");
		}
		
		// Freezing only residues whose sidechains are determined from the template
		resNumCounterQ = 0;
		for (int position=0; position<queryAlignment.length() ; position++) 
			if (queryAlignment.charAt(position)!='-') {
				resNumCounterQ++;
				if (query.residue(resNumCounterQ)!=null) {
					if ((templateAlignment.charAt(position)==queryAlignment.charAt(position)) &&
							completedResidue[position]) { 
						//System.out.println("Freezing: " + resNumCounterQ + " " + templateAlignment.charAt(position));
						query.residue(resNumCounterQ).atoms().freeze();
					}
					else {
						//System.out.println("Defrost: " + resNumCounterQ + " " + templateAlignment.charAt(position));
						query.residue(resNumCounterQ).atoms().defrost();
					}
				}
			}
		
		// Running SCMOD
		SCMOD.scmod(commands, new DunbrackLib(commands,1.0,50), query, 2);
		query.atoms().setChain("R");
		// Writing to file
		try {
			query.atoms().noOXTFilter().filter(new AtomList.NonHydrogen()).print(new MeshiWriter(output));
		}
		catch (Exception e) {
			System.out.print("\nThere was a problem writing the output:\n" + e + "\n\nContinueing...\n\n");
		}		
	}


	/** ================================= init =========================================
	 *
	 *A static function for parsing of the command line arguments and assigning the 
	 *variables commandsFileName, modelFileName and randomNumberSeed with the right inputs. Note that this
	 *static method is using parsing functions such as getOrderedArguments that are defined in MeshiProgram
	 *that MinimizeProtein inherits.
	 **/

	private static void init(String[] args) {

		/**** NOTE *** the next two lines. Because of a BUG in the Java VM, the 
		 * interfaces "Residues" and "AtomTypes" are not loaded automatically when MinimizeProtein initialize. 
		 * For this purpose these two lines are crucial wherever these two interfaces are implemented. The user might 
		 * rightfully feel that these two lines are "black magic" programming, but happily to our knowledge this is 
		 * the only bizarre phenomenon we are aware of in meshi.
		 **/
		int zvl = ALA; // force the reading of "meshi.parameters.Residues"
		zvl = ACA;// force the reading of "meshi.parameters.AtomTypes"


		String errorMessage = ("\n                  ******************\n"+
				"Usage java -Xmx600m SimpleHomologyModeling <commands file name> <alignment file name> \n"+
		"                    ******************\n");

		if (getFlag("-debug",args)) tableSet("debug", Boolean.TRUE);
		String commandsFileName = getOrderedArgument(args);
		if (commandsFileName == null) throw new RuntimeException(errorMessage);
		System.out.println("# commandsFileName = "+commandsFileName);

		commands = new CommandList(commandsFileName);

		alignmentFileName = getOrderedArgument(args);
		if (alignmentFileName == null) throw new RuntimeException(errorMessage);
		System.out.println("# Alignment file name is "+alignmentFileName);

		initRandom(777);
	}	

} // Of SimpleHomologyModeling


