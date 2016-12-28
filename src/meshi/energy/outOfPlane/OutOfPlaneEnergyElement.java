package meshi.energy.outOfPlane;
import meshi.energy.EnergyElement;
import meshi.energy.Parameters;
import meshi.geometry.Angle;
import meshi.geometry.Torsion;
import meshi.molecularElements.Atom;
import meshi.molecularElements.AtomList;

public class OutOfPlaneEnergyElement extends EnergyElement {
	private final Atom atom1;
	private final Atom atom2;
	private final Atom atom3;
	private final Atom atom4;
    private final double target;
	private double force;
	private double force2;
	private double forceSmooth;
	private double constSmooth;
    private double targetMinusBUFF;
	private double targetPlusBUFF;
	private double targetPlusPI;
	private double targetPlusPIMinusBUFF;
	private double targetPlusPIPlusBUFF;
	private double targetPlus2PIMinusBUFF;
    private final Torsion torsion;
	
	public OutOfPlaneEnergyElement(Torsion torsion, Parameters parameters, double weight) {
		double weight1 = weight;
	atom1 = torsion.atom1;
	atom2 = torsion.atom2;
	atom3 = torsion.atom3;
	atom4 = torsion.atom4;
	setAtoms();
	this.torsion = torsion;
	target = ((OutOfPlaneParameters) parameters).target;
	/*
	The variable BUFF allow a buffer zone around the target, where the energy is flat. Outside
	this zone the energy rise quadratically. BUFF also control the size of the zone where the
	two parabolas are fused into derivability at target+PI.
	*/
		double BUFF = 0.12;
		if (((target- BUFF)<-Math.PI) || ((target+ BUFF)>0))
	   throw new RuntimeException("Currently, the functional format of OutOfPlaneEnergyElement can only\n" +
	   "support targets that are smaller than -BUFF radians and larger than -PI+buff. If this pose a \n" + 
	   "problem adjust the functional form in the evaluate() method\n");
	force = ((OutOfPlaneParameters) parameters).force*weight;
	force2 = ((OutOfPlaneParameters) parameters).force2*weight;	    
	targetMinusBUFF = target- BUFF;
	targetPlusBUFF= target+ BUFF;
	targetPlusPI = target+Math.PI;
	targetPlusPIMinusBUFF = target+Math.PI- BUFF;
	targetPlusPIPlusBUFF = target+Math.PI+ BUFF;
	targetPlus2PIMinusBUFF = target+2*Math.PI- BUFF;
	forceSmooth = -force*(Math.PI-2* BUFF)/ BUFF;
	constSmooth = force*(Math.PI-2* BUFF)*(Math.PI- BUFF);
	updateFrozen();
    }

    protected void setAtoms() {
	atoms = new AtomList();
	atoms.add(atom1);
	atoms.add(atom2);
	atoms.add(atom3);
	atoms.add(atom4);
    }

    public double evaluate() {
	double dEdTorsion, e;
	double torsionValue, dTorsion;
	double energy = 0;

	if (frozen()) return 0;
	torsionValue = torsion.torsion();

        if ((torsionValue>targetMinusBUFF) && (torsionValue<targetPlusBUFF)) {
           dEdTorsion = 0.0;
        }
        else if (torsionValue<=targetMinusBUFF) {
           dTorsion = torsionValue - targetMinusBUFF;
           energy += force* dTorsion * dTorsion;
           dEdTorsion = force2 * dTorsion;
        }
        else if ((torsionValue>=targetPlusBUFF) && (torsionValue<targetPlusPIMinusBUFF)) {
           dTorsion = torsionValue - targetPlusBUFF;
           energy += force* dTorsion * dTorsion;
           dEdTorsion = force2 * dTorsion;           
        }
        else if (torsionValue>targetPlusPIPlusBUFF) {
           dTorsion = torsionValue - targetPlus2PIMinusBUFF;
           energy += force* dTorsion * dTorsion;
           dEdTorsion = force2 * dTorsion;
        }
        else if ((torsionValue>=targetPlusPIMinusBUFF) && (torsionValue<=targetPlusPIPlusBUFF)) {
           dTorsion = torsionValue - targetPlusPI;
           energy += forceSmooth * dTorsion * dTorsion + constSmooth;
           dEdTorsion = 2 * forceSmooth * dTorsion;                      
        }
        else {
           throw new RuntimeException("If the run got here, there is a bug in the code.");
        }
	if (! atom1.frozen()) {
	    atom1.addToFx(-1*dEdTorsion*torsion.dTorsionDx1());  
	    atom1.addToFy(-1*dEdTorsion*torsion.dTorsionDy1());    
	    atom1.addToFz(-1*dEdTorsion*torsion.dTorsionDz1());
	}
	if (! atom2.frozen()) {
	    atom2.addToFx(-1*dEdTorsion*torsion.dTorsionDx2());   
	    atom2.addToFy(-1*dEdTorsion*torsion.dTorsionDy2());    
	    atom2.addToFz(-1*dEdTorsion*torsion.dTorsionDz2());    
	}
	if (! atom3.frozen()) {
	    atom3.addToFx(-1*dEdTorsion*torsion.dTorsionDx3()); 
	    atom3.addToFy(-1*dEdTorsion*torsion.dTorsionDy3());    
	    atom3.addToFz(-1*dEdTorsion*torsion.dTorsionDz3());    
	}
	if (! atom4.frozen()) {
	    atom4.addToFx(-1*dEdTorsion*torsion.dTorsionDx4()); 
	    atom4.addToFy(-1*dEdTorsion*torsion.dTorsionDy4());    
	    atom4.addToFz(-1*dEdTorsion*torsion.dTorsionDz4());    
	}
	return energy;
    }

    public String toString() {
	return ("OutOfPlaneEnergyElement "+torsion.name()+" target = "+Angle.rad2deg(target)+
		" force = "+dFormatSrt.f(force)+" torsion = "+
		dFormatSrt.f(Angle.rad2deg(torsion.torsion()))+" energy = "+dFormatSrt.f(evaluate())+"\n"+
		atom1.verbose()+"\n"+atom2.verbose()+"\n"+atom3.verbose()+"\n"+atom4.verbose());
    }
}
