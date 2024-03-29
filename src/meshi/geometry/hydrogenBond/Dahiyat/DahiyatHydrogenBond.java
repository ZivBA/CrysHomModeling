package meshi.geometry.hydrogenBond.Dahiyat;
import meshi.geometry.Distance;
import meshi.geometry.DistanceMatrix;
import meshi.geometry.hydrogenBond.AbstractHydrogenBond;
import meshi.molecularElements.Atom;
import meshi.molecularElements.AtomList;
import meshi.util.mathTools.HB_Sigma;

/** 
 * A specific hydrogen bond description following: Dahiyat et al. Protein Sci. 1996 May;5(5):895-903.
 * 
 * The hydrogen bond value has the following dependence:
 * hbVal = sigmoidDis(R_donor-acceptor)*sigmoidAng(angle1)*sigmoidAng(angle2)
 *
 * Where Rd-a is the distance between the donnor and acceptor, and sigmoidDis(Rd-a) is the sidmoid function
 * described in meshi.util.mathTools.Sigma.java  . The parameters for this sigmoid are given in the 
 * constructor (p1,p2,end,valAtp1,valAtp2).
 *
 * The two angles in the HB are made of 4 atoms are defined as follows:
 *
 *   a1~~~~~~a2
 *             .
 *              .
 *               a3~~~~~~a4
 *
 * ...  -  the HB
 * ~~~  -  covelant bonding
 * Angle 1 - between atoms 1,2,3.
 * Angle 2 - between atoms 2,3,4.
 * There could be two cases:
 * 1) The hydrogen atom in the HB is not known explicitly (NoH). In this case a2 and a3 are the polar atoms, 
 * and a1 and a4 are the heavy atoms to which they are attached (base atoms).
 * 2) The hydrogen atom in the HB is known explicitly (WithH). Either a2 or a3 must be a hydrogen. The other atom 
 * COVELANTLY bonded to the hydrogen must be the polar atom. 
 * 
 * The angular score of each angle is a sigmoidAng that equals 0.0 bellow a certain threshold (sigmoidBegins) and 1.0 
 * above a certain threshold (sigmoidEnds). Between the thresholds it raises smoothly. The HB angular score is the 
 * product of the two sigmoidAns's of angle 1 and 2.
 * The sigmoid thresholds are obtained as constants from the interface "DahiyatAngleParametersInterface":
 * sigmoidBeginsWithH,sigmoidEndsWithH - The transition angles (in degrees) for the HB sigmoid when the hydrogen
 * in the HB is defined in MESHI. 
 * sigmoidBeginsNoH,sigmoidEndsNoH - The same as above, only for HB sigmoids where the hydrogen in the HB is present.

 *
 * Note: 
 * 1) The constructor assume that the atom list it get as a parameter has the donor and acceptor in the first 
 * two places (see documentation of mother class). The third place in the list is the atom that is covalently bonded
 * to the first atom. The forth place in the list is the atom that is covalently bonded to the second atom.
 * 2) The sigmoids of angle1 and 2 has the same form.
 *
 **/
public class DahiyatHydrogenBond extends AbstractHydrogenBond {

	// Parameters
	private HB_Sigma sigmaDis;
	private HB_Sigma sigmaAng1;
	private HB_Sigma sigmaAng2;

	// Variables used to calculate the angle sigmoids product 
	// The zeroDerivative field is 'true' if the angles are in their smooth regime, and this happens most of 
	// the time. Some calculations can be skipped by using this field.
	private double	cosAng;
	private double	sigmoidDerivative;	
	private double	hbAngScore;    
	private boolean	zeroDerivative;    
	private double	DhbAngScoreDx1; 
	private double	DhbAngScoreDy1; 
	private double	DhbAngScoreDz1; 
	private double	DhbAngScoreDx2; 
	private double	DhbAngScoreDy2; 
	private double	DhbAngScoreDz2; 
	private double	DhbAngScoreDx3; 
	private double	DhbAngScoreDy3; 
	private double	DhbAngScoreDz3; 
	private double	DhbAngScoreDx4; 
	private double	DhbAngScoreDy4; 
	private double	DhbAngScoreDz4; 
	private double	sigmCosAng1;
	private boolean derivativeAng1Zero;
	private double	DsigmCosAng1Dx1;
	private double	DsigmCosAng1Dy1;
	private double	DsigmCosAng1Dz1;
	private double	DsigmCosAng1Dx2;
	private double	DsigmCosAng1Dy2;
	private double	DsigmCosAng1Dz2;
	private double	DsigmCosAng1Dx3;
	private double	DsigmCosAng1Dy3;
	private double	DsigmCosAng1Dz3;
	private double	sigmCosAng2;
	private boolean derivativeAng2Zero;
	private double	DsigmCosAng2Dx2;
	private double	DsigmCosAng2Dy2;
	private double	DsigmCosAng2Dz2;
	private double	DsigmCosAng2Dx3;
	private double	DsigmCosAng2Dy3;
	private double	DsigmCosAng2Dz3;
	private double	DsigmCosAng2Dx4;
	private double	DsigmCosAng2Dy4;
	private double	DsigmCosAng2Dz4;

	// Variables used by the distance sigmoid
	private double	hbDisScore;
	private double	DhbDisScoreDx1;
	private double	DhbDisScoreDy1; 
	private double	DhbDisScoreDz1; 
	private double	DhbDisScoreDx2; 
	private double	DhbDisScoreDy2; 
	private double	DhbDisScoreDz2; 
	private double	DhbDisScoreDx3; 
	private double	DhbDisScoreDy3; 
	private double	DhbDisScoreDz3; 
	private double	DhbDisScoreDx4; 
	private double	DhbDisScoreDy4; 
	private double	DhbDisScoreDz4; 

	// These distances are used in the calculations of the angles and distance sigmoids. 
	// The numbering of the atoms (1 to 4) is as describe in the top of the class.
	private Distance disAng1atoms12;
	private Distance disAng1atoms32;
	private Distance disAng2atoms23;
	private Distance disAng2atoms43;
	private Distance disDonorAcceptor;
	
	// The 4 atoms in atom list a referenced by these fields, according to the numbering of the 
	// atoms (1 to 4) that is described at the top of the class.
	private Atom a1;
	private Atom a2;
	private Atom a3;
	private Atom a4;
		
	// These fields tell how to convert from the 1..4 atom numbering to the initial atom list numbering
	private int a1toAtomList;
	private int a2toAtomList;
	private int a3toAtomList;
	private int a4toAtomList;
		
		
    DahiyatHydrogenBond() {
    	throw new RuntimeException("\nERROR: without parameters the hydrogen bonds cannot be formed.\n");
    }

    public DahiyatHydrogenBond(DistanceMatrix dm, AtomList atomList, HB_Sigma sigmaDis, DahiyatParametersInterface angleParameters) {
    	super(dm,atomList);
    	this.sigmaDis = sigmaDis;

    	// Finding the conversion between the atomList order and the 1..4 notation of atoms at the top.
 		atomListTo1_4numbering();
 		
        
 		// Preparing the angular sigmoids. Note that because cosine is a decreasing function in the interval [0,180] degrees,
 		// but we want the angle sigmoids to actually go up in that interval. For this reason we switch the values of "ends" 
 		// and "begins" in the following if clauses.
 		double start,p1,p2,end;

 		if (a2.isHydrogen) {// There is explicit hydrogen here in angle123
			start = 1.0 + Math.cos(angleParameters.sigmoidEndsWithH()*Math.PI/180.0);
			end = 1.0 + Math.cos(angleParameters.sigmoidBeginsWithH()*Math.PI/180.0);
			p1 = start + (end-start)*0.1;
			p2 = end - (end-start)*0.1;
			sigmaAng1 = new HB_Sigma(start,p1,p2,end,0.95,0.05);
		}
		else {  // No explicit hydrogen
			start = 1.0 + Math.cos(angleParameters.sigmoidEndsNoH()*Math.PI/180.0);
			end = 1.0 + Math.cos(angleParameters.sigmoidBeginsNoH()*Math.PI/180.0);
			p1 = start + (end-start)*0.1;
			p2 = end - (end-start)*0.1;
			sigmaAng1 = new HB_Sigma(start,p1,p2,end,0.95,0.05);
		}


 		if (a3.isHydrogen) {// There is explicit hydrogen here in angle234
			start = 1.0 + Math.cos(angleParameters.sigmoidEndsWithH()*Math.PI/180.0);
			end = 1.0 + Math.cos(angleParameters.sigmoidBeginsWithH()*Math.PI/180.0);
			p1 = start + (end-start)*0.1;
			p2 = end - (end-start)*0.1;
			sigmaAng2 = new HB_Sigma(start,p1,p2,end,0.95,0.05);
		}
		else {  // No explicit hydrogen
			start = 1.0 + Math.cos(angleParameters.sigmoidEndsNoH()*Math.PI/180.0);
			end = 1.0 + Math.cos(angleParameters.sigmoidBeginsNoH()*Math.PI/180.0);
			p1 = start + (end-start)*0.1;
			p2 = end - (end-start)*0.1;
			sigmaAng2 = new HB_Sigma(start,p1,p2,end,0.95,0.05);
		}
 		
 		updateHBvalueAndDerivatives();
    }

    /**
     * This method activates all the code for updating the Dahiyat H-bond.
     **/
    protected final void updateHBvalueAndDerivatives() {
		// Assigning distance variables
		disAng1atoms12 = dm.distance(a1,a2); 
		disAng1atoms32 = dm.distance(a3,a2); 	
		disAng2atoms23 = dm.distance(a2,a3); 
		disAng2atoms43 = dm.distance(a4,a3); 	
		disDonorAcceptor = dm.distance(atomList.atomAt(0),atomList.atomAt(1));
    	
    	updateAngProduct1234();
    	updateDis();
    	hbVal = hbDisScore*hbAngScore;
		derivatives[a1toAtomList][0] = hbDisScore*DhbAngScoreDx1 + hbAngScore*DhbDisScoreDx1;
		derivatives[a1toAtomList][1] = hbDisScore*DhbAngScoreDy1 + hbAngScore*DhbDisScoreDy1;
		derivatives[a1toAtomList][2] = hbDisScore*DhbAngScoreDz1 + hbAngScore*DhbDisScoreDz1;
		derivatives[a2toAtomList][0] = hbDisScore*DhbAngScoreDx2 + hbAngScore*DhbDisScoreDx2;
		derivatives[a2toAtomList][1] = hbDisScore*DhbAngScoreDy2 + hbAngScore*DhbDisScoreDy2;
		derivatives[a2toAtomList][2] = hbDisScore*DhbAngScoreDz2 + hbAngScore*DhbDisScoreDz2;
		derivatives[a3toAtomList][0] = hbDisScore*DhbAngScoreDx3 + hbAngScore*DhbDisScoreDx3;
		derivatives[a3toAtomList][1] = hbDisScore*DhbAngScoreDy3 + hbAngScore*DhbDisScoreDy3;
		derivatives[a3toAtomList][2] = hbDisScore*DhbAngScoreDz3 + hbAngScore*DhbDisScoreDz3;
		derivatives[a4toAtomList][0] = hbDisScore*DhbAngScoreDx4 + hbAngScore*DhbDisScoreDx4;
		derivatives[a4toAtomList][1] = hbDisScore*DhbAngScoreDy4 + hbAngScore*DhbDisScoreDy4;
		derivatives[a4toAtomList][2] = hbDisScore*DhbAngScoreDz4 + hbAngScore*DhbDisScoreDz4;
    }
    
	/**
	* This method update the distance sigmoid and all the relevent derivatives. 
	* The numbering of the atoms (1 to 4) is as describe in the top of the class.
	**/   
	private void updateDis() {
    	hbDisScore = sigmaDis.s(disDonorAcceptor.distance());
		double hbDisScoreDerivative = sigmaDis.s_tag(disDonorAcceptor.distance());
    	if (!a2.isHydrogen && !a3.isHydrogen) {  // case 1:   base---O...O---base    or    base---O...N---base 
			DhbDisScoreDx1=0; 
			DhbDisScoreDy1=0; 
			DhbDisScoreDz1=0; 
			DhbDisScoreDx2= hbDisScoreDerivative *disDonorAcceptor.dDistanceDx();
			DhbDisScoreDy2= hbDisScoreDerivative *disDonorAcceptor.dDistanceDy();
			DhbDisScoreDz2= hbDisScoreDerivative *disDonorAcceptor.dDistanceDz();
			DhbDisScoreDx3=-DhbDisScoreDx2; 
			DhbDisScoreDy3=-DhbDisScoreDy2; 
			DhbDisScoreDz3=-DhbDisScoreDz2; 
			DhbDisScoreDx4=0; 
			DhbDisScoreDy4=0; 
			DhbDisScoreDz4=0; 
    	}
    	else if  (!a2.isHydrogen && a3.isHydrogen) { // case 2:   base---O...H---N
			DhbDisScoreDx1=0; 
			DhbDisScoreDy1=0; 
			DhbDisScoreDz1=0; 
			DhbDisScoreDx2= hbDisScoreDerivative *disDonorAcceptor.dDistanceDx();
			DhbDisScoreDy2= hbDisScoreDerivative *disDonorAcceptor.dDistanceDy();
			DhbDisScoreDz2= hbDisScoreDerivative *disDonorAcceptor.dDistanceDz();
			DhbDisScoreDx3=0; 
			DhbDisScoreDy3=0; 
			DhbDisScoreDz3=0; 
			DhbDisScoreDx4=-DhbDisScoreDx2; 
			DhbDisScoreDy4=-DhbDisScoreDy2; 
			DhbDisScoreDz4=-DhbDisScoreDz2;     	
    	} 
    	else if (a2.isHydrogen && !a3.isHydrogen) { // case 3:   N---H...O---base
			DhbDisScoreDx1= hbDisScoreDerivative *disDonorAcceptor.dDistanceDx();
			DhbDisScoreDy1= hbDisScoreDerivative *disDonorAcceptor.dDistanceDy();
			DhbDisScoreDz1= hbDisScoreDerivative *disDonorAcceptor.dDistanceDz();
			DhbDisScoreDx2=0; 
			DhbDisScoreDy2=0; 
			DhbDisScoreDz2=0; 
			DhbDisScoreDx3=-DhbDisScoreDx1; 
			DhbDisScoreDy3=-DhbDisScoreDy1; 
			DhbDisScoreDz3=-DhbDisScoreDz1; 
			DhbDisScoreDx4=0; 
			DhbDisScoreDy4=0; 
			DhbDisScoreDz4=0;    	
    	}
    	else if (a2.isHydrogen && a3.isHydrogen) { // case 4:   N---H...H---N
			DhbDisScoreDx1= hbDisScoreDerivative *disDonorAcceptor.dDistanceDx();
			DhbDisScoreDy1= hbDisScoreDerivative *disDonorAcceptor.dDistanceDy();
			DhbDisScoreDz1= hbDisScoreDerivative *disDonorAcceptor.dDistanceDz();
			DhbDisScoreDx2=0; 
			DhbDisScoreDy2=0; 
			DhbDisScoreDz2=0; 
			DhbDisScoreDx3=0; 
			DhbDisScoreDy3=0; 
			DhbDisScoreDz3=0; 
			DhbDisScoreDx4=-DhbDisScoreDx1; 
			DhbDisScoreDy4=-DhbDisScoreDy1; 
			DhbDisScoreDz4=-DhbDisScoreDz1;     	    	
    	}
    	else
    		throw new RuntimeException ("If this line is reached, something is definitely wrong");	
	}
    
    
	/**
	* This method update the product: sigmoidAng(angle1)*sigmoidAng(angle2)
	* and all the relevent derivatives. The numbering of the atoms (1 to 4) is as describe in the top
	* of the class.
	**/   
	private void updateAngProduct1234() {
		
		updateAng123();
		updateAng234();
		
		hbAngScore = sigmCosAng1 * sigmCosAng2;
		if (derivativeAng1Zero && derivativeAng2Zero) {
			zeroDerivative = true;
			DhbAngScoreDx1 = DhbAngScoreDy1 = DhbAngScoreDz1 =
			DhbAngScoreDx2 = DhbAngScoreDy2 = DhbAngScoreDz2 =
			DhbAngScoreDx3 = DhbAngScoreDy3 = DhbAngScoreDz3 =
			DhbAngScoreDx4 = DhbAngScoreDy4 = DhbAngScoreDz4 = 0.0;
		}
		else {
			zeroDerivative = false;
			DhbAngScoreDx1 = DsigmCosAng1Dx1*sigmCosAng2; 
			DhbAngScoreDy1 = DsigmCosAng1Dy1*sigmCosAng2; 
			DhbAngScoreDz1 = DsigmCosAng1Dz1*sigmCosAng2; 
			DhbAngScoreDx2 = DsigmCosAng1Dx2*sigmCosAng2 + DsigmCosAng2Dx2*sigmCosAng1; 
			DhbAngScoreDy2 = DsigmCosAng1Dy2*sigmCosAng2 + DsigmCosAng2Dy2*sigmCosAng1; 
			DhbAngScoreDz2 = DsigmCosAng1Dz2*sigmCosAng2 + DsigmCosAng2Dz2*sigmCosAng1; 
			DhbAngScoreDx3 = DsigmCosAng1Dx3*sigmCosAng2 + DsigmCosAng2Dx3*sigmCosAng1; 
			DhbAngScoreDy3 = DsigmCosAng1Dy3*sigmCosAng2 + DsigmCosAng2Dy3*sigmCosAng1; 
			DhbAngScoreDz3 = DsigmCosAng1Dz3*sigmCosAng2 + DsigmCosAng2Dz3*sigmCosAng1; 
			DhbAngScoreDx4 = DsigmCosAng2Dx4*sigmCosAng1; 
			DhbAngScoreDy4 = DsigmCosAng2Dy4*sigmCosAng1; 
			DhbAngScoreDz4 = DsigmCosAng2Dz4*sigmCosAng1;
		}
	}

	// Auxilary method to 'updateAngProduct1234'
	private void updateAng123() {
		cosAng = disAng1atoms12.dDistanceDx()*disAng1atoms32.dDistanceDx() + 
						disAng1atoms12.dDistanceDy()*disAng1atoms32.dDistanceDy() + 
						disAng1atoms12.dDistanceDz()*disAng1atoms32.dDistanceDz();
		if (Double.isNaN(cosAng)) {
			System.out.println("Warning: Overlapping atoms detected during hydrogen bond list update.\n" +
					a1 + "\n" + a2 + "\n" +a3 + "\n" +a4);
			sigmCosAng1 = 1.0;
			sigmoidDerivative = 0.0;
		}
		else {
			sigmCosAng1 = sigmaAng1.s(1.0+cosAng);
			sigmoidDerivative = sigmaAng1.s_tag(1.0+cosAng);
		}
		if (sigmoidDerivative != 0.0) {
			derivativeAng1Zero = false;
			DsigmCosAng1Dx1 = sigmoidDerivative * disAng1atoms12.invDistance() * (disAng1atoms32.dDistanceDx() - cosAng*disAng1atoms12.dDistanceDx());
			DsigmCosAng1Dy1 = sigmoidDerivative * disAng1atoms12.invDistance() * (disAng1atoms32.dDistanceDy() - cosAng*disAng1atoms12.dDistanceDy());
			DsigmCosAng1Dz1 = sigmoidDerivative * disAng1atoms12.invDistance() * (disAng1atoms32.dDistanceDz() - cosAng*disAng1atoms12.dDistanceDz());
			DsigmCosAng1Dx3 = sigmoidDerivative * disAng1atoms32.invDistance() * (disAng1atoms12.dDistanceDx() - cosAng*disAng1atoms32.dDistanceDx());
			DsigmCosAng1Dy3 = sigmoidDerivative * disAng1atoms32.invDistance() * (disAng1atoms12.dDistanceDy() - cosAng*disAng1atoms32.dDistanceDy());
			DsigmCosAng1Dz3 = sigmoidDerivative * disAng1atoms32.invDistance() * (disAng1atoms12.dDistanceDz() - cosAng*disAng1atoms32.dDistanceDz());
			DsigmCosAng1Dx2 = -DsigmCosAng1Dx1 - DsigmCosAng1Dx3;
			DsigmCosAng1Dy2 = -DsigmCosAng1Dy1 - DsigmCosAng1Dy3;
			DsigmCosAng1Dz2 = -DsigmCosAng1Dz1 - DsigmCosAng1Dz3;
		}
		else {
			derivativeAng1Zero = true;
			DsigmCosAng1Dx1 = DsigmCosAng1Dy1 = DsigmCosAng1Dz1 = DsigmCosAng1Dx3 = DsigmCosAng1Dy3 =
			DsigmCosAng1Dz3 = DsigmCosAng1Dx2 = DsigmCosAng1Dy2 = DsigmCosAng1Dz2 = 0.0;
		}			
	}

	// Auxilary method to 'updateAngProduct1234'
	private void updateAng234() {
		cosAng = disAng2atoms23.dDistanceDx()*disAng2atoms43.dDistanceDx() + disAng2atoms23.dDistanceDy()*disAng2atoms43.dDistanceDy() + disAng2atoms23.dDistanceDz()*disAng2atoms43.dDistanceDz();
		if (Double.isNaN(cosAng)) {
			System.out.println("Warning: Overlapping atoms detected during hydrogen bond list update.\n" +
					a1 + "\n" + a2 + "\n" +a3 + "\n" +a4);
			sigmCosAng2 = 1.0;
			sigmoidDerivative = 0.0;
		}
		else {
			sigmCosAng2 = sigmaAng2.s(1.0+cosAng);
			sigmoidDerivative = sigmaAng2.s_tag(1.0+cosAng);
		}
		if (sigmoidDerivative != 0.0) {
			derivativeAng2Zero = false;
			DsigmCosAng2Dx2 = sigmoidDerivative * disAng2atoms23.invDistance() * (disAng2atoms43.dDistanceDx() - cosAng*disAng2atoms23.dDistanceDx());
			DsigmCosAng2Dy2 = sigmoidDerivative * disAng2atoms23.invDistance() * (disAng2atoms43.dDistanceDy() - cosAng*disAng2atoms23.dDistanceDy());
			DsigmCosAng2Dz2 = sigmoidDerivative * disAng2atoms23.invDistance() * (disAng2atoms43.dDistanceDz() - cosAng*disAng2atoms23.dDistanceDz());
			DsigmCosAng2Dx4 = sigmoidDerivative * disAng2atoms43.invDistance() * (disAng2atoms23.dDistanceDx() - cosAng*disAng2atoms43.dDistanceDx());
			DsigmCosAng2Dy4 = sigmoidDerivative * disAng2atoms43.invDistance() * (disAng2atoms23.dDistanceDy() - cosAng*disAng2atoms43.dDistanceDy());
			DsigmCosAng2Dz4 = sigmoidDerivative * disAng2atoms43.invDistance() * (disAng2atoms23.dDistanceDz() - cosAng*disAng2atoms43.dDistanceDz());
			DsigmCosAng2Dx3 = -DsigmCosAng2Dx2 - DsigmCosAng2Dx4;
			DsigmCosAng2Dy3 = -DsigmCosAng2Dy2 - DsigmCosAng2Dy4;
			DsigmCosAng2Dz3 = -DsigmCosAng2Dz2 - DsigmCosAng2Dz4;
		}
		else {
			derivativeAng2Zero = true;
			DsigmCosAng2Dx4 = DsigmCosAng2Dy4 = DsigmCosAng2Dz4 = DsigmCosAng2Dx3 = DsigmCosAng2Dy3 =
			DsigmCosAng2Dz3 = DsigmCosAng2Dx2 = DsigmCosAng2Dy2 = DsigmCosAng2Dz2 = 0.0;
		}			
	}

	private void atomListTo1_4numbering() {
    	if (!atomList.atomAt(2).isHydrogen && !atomList.atomAt(3).isHydrogen) {  // case 1:   base---O...O---base    or    base---O...N---base 
			a1 = atomList.atomAt(2);
			a2 = atomList.atomAt(0);
			a3 = atomList.atomAt(1);
			a4 = atomList.atomAt(3);
			a1toAtomList=2;
			a2toAtomList=0;
			a3toAtomList=1;
			a4toAtomList=3;		
    	}
    	else if  (!atomList.atomAt(2).isHydrogen && atomList.atomAt(3).isHydrogen) { // case 2:   base---O...H---N
			a1 = atomList.atomAt(2);
			a2 = atomList.atomAt(0);
			a3 = atomList.atomAt(3);
			a4 = atomList.atomAt(1);
			a1toAtomList=2;
			a2toAtomList=0;
			a3toAtomList=3;
			a4toAtomList=1;		
    	} 
    	else if (atomList.atomAt(2).isHydrogen && !atomList.atomAt(3).isHydrogen) { // case 3:   N---H...O---base
			a1 = atomList.atomAt(0);
			a2 = atomList.atomAt(2);
			a3 = atomList.atomAt(1);
			a4 = atomList.atomAt(3);
			a1toAtomList=0;
			a2toAtomList=2;
			a3toAtomList=1;
			a4toAtomList=3;					   	
    	}
    	else if (atomList.atomAt(2).isHydrogen && atomList.atomAt(3).isHydrogen) { // case 4:   N---H...H---N
			a1 = atomList.atomAt(0);
			a2 = atomList.atomAt(2);
			a3 = atomList.atomAt(3);
			a4 = atomList.atomAt(1);
			a1toAtomList=0;
			a2toAtomList=2;
			a3toAtomList=3;
			a4toAtomList=1;					
    	}
    	else
    		throw new RuntimeException ("If this line is reached, something is definitely wrong");	
	}
	
	public boolean zeroDerivative() { return zeroDerivative; }
}


  
