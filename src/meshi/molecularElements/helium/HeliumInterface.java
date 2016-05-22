package meshi.molecularElements.helium;
import meshi.molecularElements.Atom;
import meshi.molecularElements.Residue;

public interface HeliumInterface {
    int HE = Atom.addType("HE", "DONE");
    int HEL = Residue.addName("HEL","H",-1);
    String LENNARD_JONES_PARAMETERS = "meshi/parameters/helium/lennardJones.dat";
}
    
