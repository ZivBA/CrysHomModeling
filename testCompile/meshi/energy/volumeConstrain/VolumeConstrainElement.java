package meshi.energy.volumeConstrain;
import meshi.energy.EnergyElement;
import meshi.geometry.Distance;
import meshi.molecularElements.Atom;
import meshi.molecularElements.AtomList;

// --Commented out by Inspection START (16:38 31/10/16):
//public class VolumeConstrainElement extends EnergyElement {
//    private Distance distance;
//    private double radius;
//	private double weight;
//    private Atom atom;
//    public VolumeConstrainElement() {}
//    public VolumeConstrainElement(Atom center, Atom atom, double radius, double weight) {
//	distance = new Distance(center,atom);
//	this.radius = radius;
//	this.weight = weight;
//	this.atom = atom;
//	setAtoms();
//    }
//
//    protected void setAtoms() {
//	atoms = new AtomList();
//	atoms.add(atom);
//    }
//
//    public double evaluateAtoms() {return 0.0;}
//
//    public double evaluate() {
//	double energy = 0;
//	double d,force;
//
//	distance.update();
//	d = distance.distance() - radius;
//	if (d > 0) {
//	    // that is the atom is out of the sphere
//	    energy = weight * d*d;
//	// The force is the negative of the derivative.
//	    // Note that the distance derivatives are reported for the first point,
//	    // and we use the second (the atom).
//	    force = 2.0 * weight * d;
//	    atom.addToFx(force * distance.dDistanceDx());
//	    atom.addToFy(force * distance.dDistanceDy());
//	    atom.addToFz(force * distance.dDistanceDz());
//	}
//	return energy;
//    }
//
//}
// --Commented out by Inspection STOP (16:38 31/10/16)
    
