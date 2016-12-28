package meshi.geometry;
import meshi.molecularElements.Atom;
import meshi.util.MeshiException;
import meshi.util.Updateable;

/**
 *
 *--------------------------- Torsion -----------------------------------
 * Addapted from Ron Elber's MOIL (ephi).
 * Below is the MOIL documentation
 *
 *	calculate dihedral energies and forces according
 *      to T.schlick, J.comput.chem, vol 10, 7 (1989) 
 *      with local variant (no delta funcion involved)
 *
 *      V = sum [ k(n)(1 + cos(n*phi + delta)]   (1=< n <= 3)
 *      delta is either 0 or pi, so the formula become
 *      less complicated upon expansion.
 *
 *      As of version 1.41 Nir Kalisman changed the derivation of the torsion so now it is 
 *      stable also at +-PI
 **/
	
public class Torsion implements Updateable {
    public final Atom atom1, atom2, atom3, atom4;
    private Atom atm1,atm2,atm3,atm4;
    private Angle ANGLE1,ANGLE2;
    private String torsionName = "";
    private int torsionCode = -1;
    private int torsionResNum = -9999;
    private String torsionResName = "";
    private final Distance distance1;
	private final Distance distance2;
	private final Distance distance3;
    private double dCosTorsionDx1;
	private double dCosTorsionDy1;
	private double dCosTorsionDz1;
    private double dCosTorsionDx2;
	private double dCosTorsionDy2;
	private double dCosTorsionDz2;
    private double dCosTorsionDx3;
	private double dCosTorsionDy3;
	private double dCosTorsionDz3;
    private double dCosTorsionDx4;
	private double dCosTorsionDy4;
	private double dCosTorsionDz4;
    double cosTorsion;
    private double dTorsionDx1;
	private double dTorsionDy1;
	private double dTorsionDz1;
    private double dTorsionDx2;
	private double dTorsionDy2;
	private double dTorsionDz2;
    private double dTorsionDx3;
	private double dTorsionDy3;
	private double dTorsionDz3;
    private double dTorsionDx4;
	private double dTorsionDy4;
	private double dTorsionDz4;
    private double torsion;
    private double ax;
	private double ay;
	private double az;
	private double bx;
	private double by;
	private double bz;
	private double cx;
	private double cy;
	private double cz;
    private double ab;
	private double bc;
	private double bb;
	private final DistanceMatrix distanceMatrix;
    private int numberOfUpdates = 0;

    public Torsion(Angle angle1, Angle angle2, DistanceMatrix distanceMatrix) {
	ANGLE1 = angle1;
	ANGLE2 = angle2;
	atm1 = null;
	atm2 = null;
	atm3 = null;
	atm4 = null;
	this.distanceMatrix = distanceMatrix;
	if (ANGLE1.atom2 == ANGLE2.atom1) {
	    //   1-2-3
	    //     1-2-3
	    // or 
	    //    1-2-3
	    //  3-2-1
	    type1();
	}
	else if (ANGLE1.atom2 == ANGLE2.atom3) {
	    //    1-2-3
	    //  1-2-3
	    //or 
	    //    1-2-3
	    //      3-2-1
	    type2();
	}
	else if (ANGLE1.atom2 == ANGLE2.atom2) { 
	    // improper torsion
	    type3();
	}
	
	if ((atm1 == null) || (atm1==atm4))
	    throw new MeshiException("Failed to create a torsion from\n"+
				     ANGLE1+"\n"+ANGLE2);
	atom1 = atm1;
	atom2 = atm2;
	atom3 = atm3;
	atom4 = atm4;
	distance1 = distanceMatrix.distance(atom1, atom2);
	distance2 = distanceMatrix.distance(atom3, atom2);
	distance3 = distanceMatrix.distance(atom3, atom4);


	update();
	assignName();
    }

    /**
     * This constructor should be used only by DisposableAngle.
     **/
    Torsion(Atom atom1, Atom atom2, Atom atom3, Atom atom4,
            Distance distance1, Distance distance2, Distance distance3){
      	this.atom1 = atom1;
        this.atom2 = atom2;
        this.atom3 = atom3;
        this.atom4 = atom3;
        this.distance1 = distance1;
        this.distance2 = distance2;
        this.distance3 = distance3;        
        distanceMatrix = null;
        if ((! distance1.getClass().toString().equals("class meshi.geometry.DistanceMirror")) &
	    (! distance1.getClass().toString().equals("class meshi.geometry.Distance"))&
	    (! distance1.getClass().toString().equals("class meshi.geometry.FastDistance")))
	    throw new RuntimeException(distance1.getClass().toString()+
				       "cannot be used in Torsion\n"+
				       "Only Distance or DistanceMirror may be used\n"+
				       atom1+"\n"+
				       atom2+"\n"+
				       atom3+"\n"+
				       atom4+"\n");
	if ((! distance2.getClass().toString().equals("class meshi.geometry.DistanceMirror")) &
	    (! distance2.getClass().toString().equals("class meshi.geometry.Distance"))&
	    (! distance2.getClass().toString().equals("class meshi.geometry.FastDistance")))
	    throw new RuntimeException(distance1.getClass().toString()+
				       "cannot be used in Torsion\n"+
				       "Only Distance or DistanceMirror may be used");
	if ((! distance3.getClass().toString().equals("class meshi.geometry.DistanceMirror")) &
	    (! distance3.getClass().toString().equals("class meshi.geometry.Distance"))&
	    (! distance3.getClass().toString().equals("class meshi.geometry.FastDistance")))
	    throw new RuntimeException(distance1.getClass().toString()+
				       "cannot be used in Torsion\n"+
				       "Only Distance or DistanceMirror may be used");
	update();
	}

    private void type1() {
	if (ANGLE1.atom3 == ANGLE2.atom2) { 
	    //1-2-3
	    //  1-2-3
	    atm1 = ANGLE1.atom1;
	    atm2 = ANGLE1.atom2;
	    atm3 = ANGLE1.atom3;
	    atm4 = ANGLE2.atom3;
	}
	else if (ANGLE1.atom1 == ANGLE2.atom2) { 
	    //    1-2-3
	    //  3-2-1
	    atm1 = ANGLE1.atom3;
	    atm2 = ANGLE2.atom1;
	    atm3 = ANGLE2.atom2;
	    atm4 = ANGLE2.atom3;
	}
    }
    private void type2() {
	if (ANGLE1.atom1 == ANGLE2.atom2) { 
	    //    1-2-3
	    //  1-2-3
	    atm1 = ANGLE2.atom1;
	    atm2 = ANGLE2.atom2;
	    atm3 = ANGLE2.atom3;
	    atm4 = ANGLE1.atom3;

	    atm1 = ANGLE1.atom3;
	    atm2 = ANGLE2.atom3;
	    atm3 = ANGLE2.atom2;
	    atm4 = ANGLE2.atom1;
	}
	else if (ANGLE1.atom3 == ANGLE2.atom2) { 
	    //    1-2-3
	    //      3-2-1
	    atm1 = ANGLE1.atom1;
	    atm2 = ANGLE1.atom2;
	    atm3 = ANGLE1.atom3;
	    atm4 = ANGLE2.atom1;
	}
    }
    private void type3() {
	if (ANGLE1.atom1 == ANGLE2.atom1) {
	    //       /3
	    //    1-2
	    //    1-2
	    //       \3
	    atm1 = ANGLE1.atom1;
	    atm2 = ANGLE1.atom3;
	    atm3 = ANGLE1.atom2;
	    atm4 = ANGLE2.atom3;
	}
	else if (ANGLE1.atom3 == ANGLE2.atom3) {
	    //    1\
		//      2-3
		//      2-3
		//    1/
	    atm1 = ANGLE1.atom3;
	    atm2 = ANGLE1.atom1;
	    atm3 = ANGLE1.atom2;
	    atm4 = ANGLE2.atom1;
	}
	else if (ANGLE1.atom3 == ANGLE2.atom1) {
		//    1\
		//      2-3
		//      2-1
		//    3/
	    atm1 = ANGLE1.atom3;
	    atm2 = ANGLE1.atom1;
	    atm3 = ANGLE1.atom2;
	    atm4 = ANGLE2.atom3;
	}
	else if (ANGLE1.atom1 == ANGLE2.atom3) {
		//       /3
		//    1-2
		//    3-2
		//       \1
	    atm1 = ANGLE1.atom1;
	    atm2 = ANGLE1.atom3;
	    atm3 = ANGLE1.atom2;
	    atm4 = ANGLE2.atom1;
	}
    }

    public String toString (){ 
	String prop;
	if (proper()) prop = "proper";
	else prop = "improper";
	return "\nTorsion "+prop+" "+torsionName + "-" + torsionResNum + 
	    "\n\tatom1: "+atom1+" "+
	    "\n\tatom2: "+atom2+" "+
	    "\n\tatom3: "+atom3+" "+
	    "\n\tatom4: "+atom4+" "+
	    "\n\tvalue: "+ Angle.rad2deg(torsion);
    }
    
    public double cosTorsion() {return cosTorsion;}
    public double dCosTorsionDx1() { return dCosTorsionDx1;}
    public double dCosTorsionDy1() { return dCosTorsionDy1;}
    public double dCosTorsionDz1() { return dCosTorsionDz1;}
    public double dCosTorsionDx2() { return dCosTorsionDx2;}
    public double dCosTorsionDy2() { return dCosTorsionDy2;}
    public double dCosTorsionDz2() { return dCosTorsionDz2;}
    public double dCosTorsionDx3() { return dCosTorsionDx3;}
    public double dCosTorsionDy3() { return dCosTorsionDy3;}
    public double dCosTorsionDz3() { return dCosTorsionDz3;}
    public double dCosTorsionDx4() { return dCosTorsionDx4;}
    public double dCosTorsionDy4() { return dCosTorsionDy4;}
    public double dCosTorsionDz4() { return dCosTorsionDz4;}
    public double torsion() {return torsion;}
    public double dTorsionDx1() { return dTorsionDx1;}
    public double dTorsionDy1() { return dTorsionDy1;}
    public double dTorsionDz1() { return dTorsionDz1;}
    public double dTorsionDx2() { return dTorsionDx2;}
    public double dTorsionDy2() { return dTorsionDy2;}
    public double dTorsionDz2() { return dTorsionDz2;}
    public double dTorsionDx3() { return dTorsionDx3;}
    public double dTorsionDy3() { return dTorsionDy3;}
    public double dTorsionDz3() { return dTorsionDz3;}
    public double dTorsionDx4() { return dTorsionDx4;}
    public double dTorsionDy4() { return dTorsionDy4;}
    public double dTorsionDz4() { return dTorsionDz4;}

    private void updateCosine() {
	ax = -1*distance1.dx();
	ay = -1*distance1.dy();
	az = -1*distance1.dz();

	bx = distance2.dx();
	by = distance2.dy();
	bz = distance2.dz();

	cx = -1*distance3.dx();
	cy = -1*distance3.dy();
	cz = -1*distance3.dz();

	// dot products of bond vectors
	ab = dot(ax,ay,az,bx,by,bz);
	bc = dot(bx,by,bz,cx,cy,cz);
	    double ac = dot(ax, ay, az, cx, cy, cz);
	    double aa = dot(ax, ay, az, ax, ay, az);
	bb = dot(bx,by,bz,bx,by,bz);
	    double cc = dot(cx, cy, cz, cx, cy, cz);
	// calculate cosTorsion	
	    double uu = (aa * bb) - (ab * ab);
	    double vv = (bb * cc) - (bc * bc);
	    double uv = (ab * bc) - (ac * bb);
	    double den = 1 / Math.sqrt(uu * vv);

	cosTorsion = uv * den;
	if (cosTorsion<-1.0)
		cosTorsion = -1.0;
	if (cosTorsion>1.0)
		cosTorsion = 1.0;		
	// derivatives
	    double co1 = 0.5 * cosTorsion * den;
	
	    double a0x = -bc * bx + bb * cx;
	    double a0y = -bc * by + bb * cy;
	    double a0z = -bc * bz + bb * cz;
	
	    double b0x = ab * cx + bc * ax - 2. * ac * bx;
	    double b0y = ab * cy + bc * ay - 2. * ac * by;
	    double b0z = ab * cz + bc * az - 2. * ac * bz;
	
	    double c0x = ab * bx - bb * ax;
	    double c0y = ab * by - bb * ay;
	    double c0z = ab * bz - bb * az;
	
	    double uu2 = 2 * uu;
	    double vv2 = 2 * vv;
	
	    double a1x = uu2 * (-cc * bx + bc * cx);
	    double a1y = uu2 * (-cc * by + bc * cy);
	    double a1z = uu2 * (-cc * bz + bc * cz);
	
	    double b1x = uu2 * (bb * cx - bc * bx);
	    double b1y = uu2 * (bb * cy - bc * by);
	    double b1z = uu2 * (bb * cz - bc * bz);
	
	    double a2x = -vv2 * (bb * ax - ab * bx);
	    double a2y = -vv2 * (bb * ay - ab * by);
	    double a2z = -vv2 * (bb * az - ab * bz);
	
	    double b2x = vv2 * (aa * bx - ab * ax);
	    double b2y = vv2 * (aa * by - ab * ay);
	    double b2z = vv2 * (aa * bz - ab * az);

	 dCosTorsionDx1 = (a0x - a2x * co1)* den;
	 dCosTorsionDy1 = (a0y - a2y * co1)* den;
	 dCosTorsionDz1 = (a0z - a2z * co1)* den;

	 dCosTorsionDx2 = (-a0x - b0x - (a1x - a2x - b2x)* co1)* den;
	 dCosTorsionDy2 = (-a0y - b0y - (a1y - a2y - b2y)* co1)* den;
	 dCosTorsionDz2 = (-a0z - b0z - (a1z - a2z - b2z)* co1)* den;

	 dCosTorsionDx3 = (b0x - c0x - (-a1x - b1x + b2x)* co1)* den;
	 dCosTorsionDy3 = (b0y - c0y - (-a1y - b1y + b2y)* co1)* den;
	 dCosTorsionDz3 = (b0z - c0z - (-a1z - b1z + b2z)* co1)* den;

	 dCosTorsionDx4 = (c0x - b1x * co1)* den;
	 dCosTorsionDy4 = (c0y - b1y * co1)* den;
	 dCosTorsionDz4 = (c0z - b1z * co1)* den;
    }

    
   public void update(int numberOfUpdates) {
	if (numberOfUpdates == this.numberOfUpdates+1) {
	    update();
	    this.numberOfUpdates++;
	}
	else if (numberOfUpdates != this.numberOfUpdates) 
	    throw new RuntimeException("Something weird with Torsion.update(int numberOfUpdates)\n"+
				       "numberOfUpdates = "+numberOfUpdates+" this.numberOfUpdates = "+this.numberOfUpdates);
    }
    
    /**
     * Calculate the arc-cosine function. 
     * This method simply calls Math.acos, extending classes however may 
     * use approximations in order to save time.
     **/
    double acos(double cos) {
	return Math.acos(cos);
    }
    private void update() {
	updateCosine();
	    double ux = ay * bz - az * by;
	    double uy = az * bx - ax * bz;
	    double uz = ax * by - ay * bx;
	
	    double vx = by * cz - bz * cy;
	    double vy = bz * cx - bx * cz;
	    double vz = bx * cy - by * cx;
	
	    double dx1 = uy * vz - uz * vy;
	    double dy1 = uz * vx - ux * vz;
	    double dz1 = ux * vy - uy * vx;
	    double sinSign = dot(dx1, dy1, dz1, bx, by, bz);
	torsion = acos(cosTorsion);
	//torsion = Math.acos(cosTorsion);
	if(sinSign < 0) torsion = -1*torsion;
	
	/* This part was written by Nir (nirka@cs.bgu.ac.il) on 6.11.2005 because of
	   severe instabilities near Torsion = 0,PI,-PI */
		// Calculating the cross products
	    double cross123x = ay * bz - az * by;
	    double cross123y = az * bx - ax * bz;
	    double cross123z = ax * by - ay * bx;
	
	    double cross234x = cy * bz - cz * by;
	    double cross234y = cz * bx - cx * bz;
	    double cross234z = cx * by - cy * bx;
	
	    double normCross123squared = cross123x * cross123x + cross123y * cross123y + cross123z * cross123z;
	    double normCross234squared = cross234x * cross234x + cross234y * cross234y + cross234z * cross234z;
	
	    double tmpSQRT = -distance2.distance();
	    double invBB = 1.0 / bb;
	    double factor1 = tmpSQRT / normCross123squared;
	    double factor4 = tmpSQRT / normCross234squared;
	    double aux1 = -factor1 * (1 + ab * invBB);
	    double aux2 = factor4 * bc * invBB;
	    double aux3 = factor1 * ab * invBB;
	    double aux4 = -factor4 * (1 + bc * invBB);
		
		dTorsionDx1 = factor1 * cross123x;
		dTorsionDy1 = factor1 * cross123y;
		dTorsionDz1 = factor1 * cross123z;

		dTorsionDx2 = cross123x * aux1 + cross234x * aux2;
		dTorsionDy2 = cross123y * aux1 + cross234y * aux2;
		dTorsionDz2 = cross123z * aux1 + cross234z * aux2;

		dTorsionDx3 = cross123x * aux3 + cross234x * aux4;
		dTorsionDy3 = cross123y * aux3 + cross234y * aux4;
		dTorsionDz3 = cross123z * aux3 + cross234z * aux4;

		dTorsionDx4 = factor4 * cross234x;
		dTorsionDy4 = factor4 * cross234y;
		dTorsionDz4 = factor4 * cross234z;
	/* End of the new part */
    }

    public boolean proper() {
 	return ANGLE1.proper(ANGLE2);
    }
    public boolean equivalent(Torsion other) {
	if (! proper()) {
	    if (other.proper()) return false;
		return atom3 == other.atom3;
	}
	else { // proper
		return ((atom2 == other.atom2) & (atom3 == other.atom3)) |
				((atom3 == other.atom2) & (atom2 == other.atom3));
	}
    }
    private static double dot(double d1, double d2, double d3,
                              double e1, double e2, double e3) {
	return d1*e1 + d2*e2 + d3*e3;
    }
    
    public boolean frozen() {
	return (atom1.frozen() & atom2.frozen() & atom3.frozen() & atom4.frozen());
    }

    public void freeze() {
	   atom1.freeze();
	   atom2.freeze();
	   atom3.freeze();
	   atom4.freeze();
    }


    
    public String getTorsionName() {return torsionName;}
    public int getTorsionCode() {return torsionCode;}
    public int getTorsionResNum() {return torsionResNum;}
    public String getTorsionResName() {return torsionResName;}
    /**
     * Assigns a meaningful name to a torsion.
     *This method assigns (if possible):
     *- name (phi, psi, chi1 etc.) to the torsion object.
     *- number corresponding to this name.
     *- the number in the chain of the residue to which this torsion belong
     *- the name of the residue to which this torsion belong 
     *
     *The names to numbers conversion is:
     *PHI - 0
     *PSI - 1
     *OMG - 2 (Omega - on the C(n)-N(n+1) bond - very close to 180 (trans) or 0 (cis)) 
     *CHI1 - 3 (CHI-1 torsion on the CA-CB bond)
     *CHI2 - 4 
     *CHI3 - 5 
     *CHI4 - 6 
     *CHI5 - 7 (Not used any more)
     *ALPHA - 8 (torsion made by four consecutive Ca's)
     *OOP - 9 (Out of plain torsion) 
     *
     *proximal - (-1) - A torsion with no biological name that could also be between two successive
     *residues. These torsions are mainly relevent ro the plane energies. The residue names and 
     *the residue numbers of the proximal torsions are left undefined, and only the torsion code
     *and torsion anme are updated.  
     **/

    private void assignName() {
        if (Math.abs(atom4.residueNumber() - atom1.residueNumber()) < 2) {
           torsionName = "proximal"; 
           torsionCode = -1;
        }
    	int resNum = atom1.residueNumber();
    	String str,str1,str2,str3,str4;
     	str = atom2.residueName();                        
    	if ((atom1.name().compareTo("CA") == 0) &&                 // ALPHA
    		((atom2.name().compareTo("CA") == 0) && (atom2.residueNumber() == (resNum+1))) &&	
    		((atom3.name().compareTo("CA") == 0) && (atom3.residueNumber() == (resNum+2))) &&	
     		((atom4.name().compareTo("CA") == 0) && (atom4.residueNumber() == (resNum+3)))) {
     			torsionName = "ALPHA";
     			torsionCode = 8;
     			torsionResNum = resNum+1;
     			torsionResName = atom2.residueName();
     	}
    	if ((atom1.name().compareTo("C") == 0) &&                 // PHI
	    ((atom2.name().compareTo("N") == 0) && (atom2.residueNumber() == (resNum+1))) &&	
	    ((atom3.name().compareTo("CA") == 0) && (atom3.residueNumber() == (resNum+1))) &&	
	    ((atom4.name().compareTo("C") == 0) && (atom4.residueNumber() == (resNum+1)))) {
	    torsionName = "PHI";
	    torsionCode = 0;
	    torsionResNum = resNum+1;
	    torsionResName = atom2.residueName();
     	}	
    	if ((atom1.name().compareTo("N") == 0) &&                 // PSI
    		((atom2.name().compareTo("CA") == 0) && (atom2.residueNumber() == resNum)) &&	
    		((atom3.name().compareTo("C") == 0) && (atom3.residueNumber() == resNum)) &&	
     		((atom4.name().compareTo("N") == 0) && (atom4.residueNumber() == (resNum+1)))) {
     			torsionName = "PSI";
     			torsionCode = 1;
     			torsionResNum = resNum;
     			torsionResName = atom2.residueName();
     	}	     	
    	if ((atom1.name().compareTo("CA") == 0) &&                 // OMEGA
    		((atom2.name().compareTo("C") == 0) && (atom2.residueNumber() == resNum)) &&	
    		((atom3.name().compareTo("N") == 0) && (atom3.residueNumber() == (resNum+1))) &&	
     		((atom4.name().compareTo("CA") == 0) && (atom4.residueNumber() == (resNum+1)))) {
     			torsionName = "OMG";
     			torsionCode = 2;
     			torsionResNum = resNum;
     			torsionResName = atom2.residueName();
     	}
     	
     	// CHI-1
     	str1="N";
     	str2="CA";
     	str3="CB";
     	str4="";  
    	if (str.compareTo("CYS") == 0)
    	str4 = "SG";
    	else if (str.compareTo("ASP") == 0)
    	str4 = "CG";
    	else if (str.compareTo("GLU") == 0)
    	str4 = "CG";
    	else if (str.compareTo("PHE") == 0)
    	str4 = "CG";
    	else if (str.compareTo("HIS") == 0)
    	str4 = "CG";
    	else if (str.compareTo("ILE") == 0)
    	str4 = "CG1";
    	else if (str.compareTo("LYS") == 0)
    	str4 = "CG";
    	else if (str.compareTo("LEU") == 0)
    	str4 = "CG";
    	else if (str.compareTo("MET") == 0)
    	str4 = "CG";
    	else if (str.compareTo("ASN") == 0)
    	str4 = "CG";
    	else if (str.compareTo("PRO") == 0)
    	str4 = "CG";
    	else if (str.compareTo("GLN") == 0)
    	str4 = "CG";
    	else if (str.compareTo("ARG") == 0)
    	str4 = "CG";
    	else if (str.compareTo("SER") == 0)
    	str4 = "OG";
    	else if (str.compareTo("THR") == 0)
    	str4 = "OG1";
    	else if (str.compareTo("VAL") == 0)
    	str4 = "CG1";
    	else if (str.compareTo("TRP") == 0)
    	str4 = "CG";
    	else if (str.compareTo("TYR") == 0)
    	str4 = "CG";
    	if ((atom1.name().compareTo(str1) == 0) &&             
    		((atom2.name().compareTo(str2) == 0) && (atom2.residueNumber() == resNum)) &&	
    		((atom3.name().compareTo(str3) == 0) && (atom3.residueNumber() == resNum)) &&	
     		((atom4.name().compareTo(str4) == 0) && (atom4.residueNumber() == resNum))) {
     			torsionName = "CHI1";
     			torsionCode = 3;
     			torsionResNum = resNum;
     			torsionResName = atom2.residueName();
     	}

	/***************************************************************************************/     	
	// CHI - 2
	str1="CA";
	str2="CB";
     	str3="CG";
     	str4="";  
     	if (str.compareTo("LEU") == 0)
            str4 = "CD1";
    	else if (str.compareTo("ILE") == 0) {
    		str3 = "CG1";
            str4 = "CD1";
        }
    	else if (str.compareTo("MET") == 0)
            str4 = "SD";
    	else if (str.compareTo("PRO") == 0)
            str4 = "CD";
    	else if (str.compareTo("ASP") == 0)
            str4 = "OD1";
    	else if (str.compareTo("ASN") == 0)
            str4 = "OD1";
    	else if (str.compareTo("GLU") == 0)
            str4 = "CD";
    	else if (str.compareTo("GLN") == 0)
            str4 = "CD"; 
    	else if (str.compareTo("LYS") == 0)
            str4 = "CD";
    	else if (str.compareTo("ARG") == 0)
            str4 = "CD";
		else if (str.compareTo("HIS") == 0)
            str4 = "ND1";
    	else if (str.compareTo("PHE") == 0)
            str4 = "CD1";
    	else if (str.compareTo("TYR") == 0)
            str4 = "CD1";
    	else if (str.compareTo("TRP") == 0)
            str4 = "CD1";
    			  
    	if ((atom1.name().compareTo(str1) == 0) &&             
    		((atom2.name().compareTo(str2) == 0) && (atom2.residueNumber() == resNum)) &&	
    		((atom3.name().compareTo(str3) == 0) && (atom3.residueNumber() == resNum)) &&	
     		((atom4.name().compareTo(str4) == 0) && (atom4.residueNumber() == resNum))) {
            torsionName = "CHI2";
            torsionCode = 4;
            torsionResNum = resNum;
            torsionResName = atom2.residueName();

        }
	/***************************************************************************************/     	
	// CHI - 3
     	str1="CB";
     	str2="CG";
     	str3="CD";
     	str4="";                                       
    	if (str.compareTo("MET") == 0) {
    		str3 = "SD";
            str4 = "CE";
        }
    	else if (str.compareTo("GLU") == 0)
            str4 = "OE1";
    	else if (str.compareTo("GLN") == 0)
            str4 = "OE1";
    	else if (str.compareTo("LYS") == 0)
            str4 = "CE";
    	else if (str.compareTo("ARG") == 0)
            str4 = "NE";
    	if ((atom1.name().compareTo(str1) == 0) &&             
    		((atom2.name().compareTo(str2) == 0) && (atom2.residueNumber() == resNum)) &&	
    		((atom3.name().compareTo(str3) == 0) && (atom3.residueNumber() == resNum)) &&	
     		((atom4.name().compareTo(str4) == 0) && (atom4.residueNumber() == resNum))) {
            torsionName = "CHI3";
            torsionCode = 5;
            torsionResNum = resNum;
            torsionResName = atom2.residueName();
        }
        /***************************************************************************************/    	
	// CHI - 4
     	str1="CG";
     	str2="CD";
     	str3="";
     	str4="";                                       
    	if (str.compareTo("LYS") == 0) {
    		str3 = "CE";
            str4 = "NZ";
        }
    	else if (str.compareTo("ARG") == 0) {
    		str3 = "NE";
            str4 = "CZ";
        }
    	if ((atom1.name().compareTo(str1) == 0) &&             
    		((atom2.name().compareTo(str2) == 0) && (atom2.residueNumber() == resNum)) &&	
    		((atom3.name().compareTo(str3) == 0) && (atom3.residueNumber() == resNum)) &&	
     		((atom4.name().compareTo(str4) == 0) && (atom4.residueNumber() == resNum))) {
            torsionName = "CHI4";
            torsionCode = 6;
            torsionResNum = resNum;
            torsionResName = atom2.residueName();
        }
        /***************************************************************************************/
	// OOP - 9 Out Of Plain
	if ((atom1.name().compareTo("N") == 0) &&                 // OOP
	    ((atom2.name().compareTo("CB") == 0) && (atom2.residueNumber() == (resNum))) &&	
	    ((atom3.name().compareTo("CA") == 0) && (atom3.residueNumber() == (resNum))) &&	
	    ((atom4.name().compareTo("C") == 0) && (atom4.residueNumber() == (resNum)))) {
	    torsionName = "OOP";
	    torsionCode = 9;
	    torsionResNum = resNum;
	    torsionResName = atom1.residueName();
	}
	if ((str.compareTo("ILE") == 0) && 
	    (atom1.name().compareTo("CA") == 0) &&             
	    ((atom2.name().compareTo("CG2") == 0) && (atom2.residueNumber() == resNum)) &&	
	    ((atom3.name().compareTo("CB") == 0) && (atom3.residueNumber() == resNum)) &&	
	    ((atom4.name().compareTo("CG1") == 0) && (atom4.residueNumber() == resNum))) {
	    torsionName = "OOP";
	    torsionCode = 9;
	    torsionResNum = resNum;
	    torsionResName = atom1.residueName();
	}             		      	
	if ((str.compareTo("THR") == 0) && 
	    (atom1.name().compareTo("CA") == 0) &&             
	    ((atom2.name().compareTo("CG2") == 0) && (atom2.residueNumber() == resNum)) &&	
	    ((atom3.name().compareTo("CB") == 0) && (atom3.residueNumber() == resNum)) &&	
	    ((atom4.name().compareTo("OG1") == 0) && (atom4.residueNumber() == resNum))) {
	    torsionName = "OOP";
	    torsionCode = 9;
	    torsionResNum = resNum;
	    torsionResName = atom1.residueName();
	}             		      	
	if ((str.compareTo("VAL") == 0) && 
	    (atom1.name().compareTo("CA") == 0) &&             
	    ((atom2.name().compareTo("CG1") == 0) && (atom2.residueNumber() == resNum)) &&	
	    ((atom3.name().compareTo("CB") == 0) && (atom3.residueNumber() == resNum)) &&	
	    ((atom4.name().compareTo("CG2") == 0) && (atom4.residueNumber() == resNum))) {
	    torsionName = "OOP";
	    torsionCode = 9;
	    torsionResNum = resNum;
	    torsionResName = atom1.residueName();
	}             		      	
	if ((str.compareTo("LEU") == 0) && 
	    (atom1.name().compareTo("CB") == 0) &&             
	    ((atom2.name().compareTo("CD1") == 0) && (atom2.residueNumber() == resNum)) &&	
	    ((atom3.name().compareTo("CG") == 0) && (atom3.residueNumber() == resNum)) &&	
	    ((atom4.name().compareTo("CD2") == 0) && (atom4.residueNumber() == resNum))) {
	    torsionName = "OOP";
	    torsionCode = 9;
	    torsionResNum = resNum;
	    torsionResName = atom1.residueName();
	}             		      	
    }

    public String name() {return torsionName;}
}
