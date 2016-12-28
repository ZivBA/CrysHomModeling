package meshi.energy.plane;
import meshi.energy.EnergyElement;
import meshi.energy.Parameters;
import meshi.geometry.Angle;
import meshi.geometry.Torsion;
import meshi.molecularElements.Atom;
import meshi.molecularElements.AtomList;

//---------------------------------------------------------------------
public class PlaneEnergyElement extends EnergyElement {
	private final Atom atom1;
	private final Atom atom2;
	private final Atom atom3;
	private final Atom atom4;
    private final double force;
	private final double force2;
    private final int trans;
    private final Torsion torsion;
	
	public PlaneEnergyElement(Torsion torsion, Parameters parameters, double weight) {
		double weight1 = weight;
	atom1 = torsion.atom1;
	atom2 = torsion.atom2;
	atom3 = torsion.atom3;
	atom4 = torsion.atom4;
	this.torsion = torsion;
	setAtoms();
	updateFrozen();
	trans = ((PlaneParameters) parameters).trans;
	force = ((PlaneParameters) parameters).force*weight;
	force2 = ((PlaneParameters) parameters).force2*weight;	    
    }
    protected void setAtoms() {
	atoms = new AtomList();
	atoms.add(atom1);
	atoms.add(atom2);
	atoms.add(atom3);
	atoms.add(atom4);
    }
	

    public double evaluate() {
	double dEdCosTorsion;
	double cosTorsion;
	double ctpo; // Cos Torsion Plus One
	double ctpo2; // (Cos Torsion Plus One)^2 
	double invCtpo2pb;
	double omct; // One Minus Cos Torsion
	double omct2;
	double invOmct2pb;
	double energy = 0;

	if (frozen()) return 0;
	cosTorsion = torsion.cosTorsion();
	//System.out.println("xxxxxxx "+torsion+" "+cosTorsion);
	    double BETA = 0.05;
	    if (trans == PlaneParameters.TRANS) {
	    ctpo = cosTorsion + 1; // Cos Torsion Plus One
	    ctpo2 = ctpo*ctpo;
	    invCtpo2pb = 1/(ctpo2+ BETA);
	    energy = force * (ctpo2 + ctpo2*invCtpo2pb);
	    dEdCosTorsion = force2*ctpo*(1 + invCtpo2pb - ctpo2*invCtpo2pb*invCtpo2pb);
	}
	else if (trans == PlaneParameters.CIS) {
	    omct = 1 - cosTorsion; // One Minus Cos Torsion
	    omct2 = omct*omct;
	    invOmct2pb = 1/(omct2+ BETA);
	    energy = force * (omct2 + omct2*invOmct2pb);
	    dEdCosTorsion = -1*force2*omct*(1 + invOmct2pb - omct2*invOmct2pb*invOmct2pb);
	}
	else {
	    energy = force*(1 - cosTorsion*cosTorsion);
	    dEdCosTorsion = -1*force2*cosTorsion;
	}
	if (! atom1.frozen()) {
	    atom1.addToFx(-1*dEdCosTorsion*torsion.dCosTorsionDx1());  
	    atom1.addToFy(-1*dEdCosTorsion*torsion.dCosTorsionDy1());    
	    atom1.addToFz(-1*dEdCosTorsion*torsion.dCosTorsionDz1());   
	} 
	if (! atom2.frozen()) {
	    atom2.addToFx(-1*dEdCosTorsion*torsion.dCosTorsionDx2());   
	    atom2.addToFy(-1*dEdCosTorsion*torsion.dCosTorsionDy2());    
	    atom2.addToFz(-1*dEdCosTorsion*torsion.dCosTorsionDz2());    
	} 
	if (! atom3.frozen()) {
	    atom3.addToFx(-1*dEdCosTorsion*torsion.dCosTorsionDx3()); 
	    atom3.addToFy(-1*dEdCosTorsion*torsion.dCosTorsionDy3());    
	    atom3.addToFz(-1*dEdCosTorsion*torsion.dCosTorsionDz3());    
	} 
	if (! atom4.frozen()) {
	    atom4.addToFx(-1*dEdCosTorsion*torsion.dCosTorsionDx4()); 
	    atom4.addToFy(-1*dEdCosTorsion*torsion.dCosTorsionDy4());    
	    atom4.addToFz(-1*dEdCosTorsion*torsion.dCosTorsionDz4());   
	}
	return energy;
    }

    public String toString() {
	String conf = ((trans == PlaneParameters.TRANS)?"trans":"cis");
	String prop = ((torsion.proper())?"proper":"improper");
	return ("PlaneEnergyElement "+torsion.name()+" "+conf+" "+prop+"\n"+
		"force = "+dFormatSrt.f(force)+" torsion = "+
		dFormatSrt.f(Angle.rad2deg(torsion.torsion()))+" energy = "+dFormatSrt.f(evaluate())+"\n"+
		atom1.verbose()+"\n"+atom2.verbose()+"\n"+atom3.verbose()+"\n"+atom4.verbose());
    }
}	
