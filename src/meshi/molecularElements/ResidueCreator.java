package meshi.molecularElements;
public interface ResidueCreator {
    Residue create(AtomList atoms, int residueNumber, int mode);
    Residue create(String name, int residueNumber, int mode, double x, double y, double z);
}
