package meshi.optimizers;


// --Commented out by Inspection START (16:34 31/10/16):
//class Straighten implements Residues, AtomTypes, KeyWords {
//
//    private static final DecimalFormat fmt2 = new DecimalFormat("0.###");
//
//    public static void straighten(CommandList commands, Protein protein , int r1 , int r2 , int [][] resList , int id) throws Exception {
//
//    // Moving the movable CHUNK
//    SmartOverlap.moveChunk(protein, r1, r2, resList);
//    protein.atoms().noOXTFilter().print(new MeshiWriter("out_" + id + "_1.pdb"));
//
//	// puting into Rot1
//	protein.defrost();
//	DistanceMatrix dm = new DistanceMatrix(protein.atoms(),  5.5, 2.0, 4);
//	DunbrackLib lib = new DunbrackLib(commands, 1.0 , 2);
//	double[][] pp = RotamericTools.putIntoRot1(protein, dm, lib);
//
//	// Defrosting the right part
//	protein.freeze();
//    for (int c=r1 ; c<=r2 ; c++)
//        protein.residue(c).defrost();
//
//	// The energy and minimization
//	dm = new DistanceMatrix(protein.atoms(),  5.5, 2.0, 4);
//	HydrogenBondsCreator hbc = new HydrogenBondsCreator(0.001);
//	EnergyCreator[] energyCreators = {
//	new BondCreator(),
//	new AngleCreator(),
//	new PlaneCreator(),
//	new OutOfPlaneCreator(),
//	new SoftExcludedVolCreator(100.0,2),
//	new EVRot1BBCreator(5,pp,1000),
//	new SolvateRot1Creator(1,pp,1000),
//	new FlatRamachCreator(25.0),
//	new LinearRgCreator(50.0),
//	new TorsionValCreator(100.0),
//	hbc,
//	new HydrogenBondsPairsCreator(0.25,hbc),
//	new HBondsPunishOHNAngleCreator(10.0,hbc),
//	new HbondsPunishHOCAngleCreator(10.0,hbc),
//	new HydrogenBondsPlaneCreator(2.0)
//	};
//	TotalEnergy energy = new TotalEnergy(protein, dm, energyCreators, commands);
//	Minimizer minimizer = new LBFGS(energy,commands);
//	System.out.println(energy.reportHeader());
//	System.out.println(minimizer.minimize());
//    protein.atoms().noOXTFilter().print(new MeshiWriter("out_" + id + "_2.pdb"));
//
//
//	// Showing The results.
//	AbstractEnergy term = energy.getEnergyTerm(new SoftExcludedVol());
//	double ev = term.evaluate();
//	protein.defrost();
//	dm = new DistanceMatrix(protein.atoms(),  5.5, 2.0, 4);
//	energy = new TotalEnergy(protein, dm, energyCreators, commands);
//	energy.evaluate();
//	term = energy.getEnergyTerm(new SolvateRot1Energy());
//	double sol = term.evaluate();
//	System.out.println("\n\n666666 " + id + " " + fmt2.format(ev) + " " + fmt2.format(sol) + "\n\n");
//	}
//
//}
// --Commented out by Inspection STOP (16:34 31/10/16)
