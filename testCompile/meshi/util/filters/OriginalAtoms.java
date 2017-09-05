package meshi.util.filters;

// --Commented out by Inspection START (16:34 31/10/16):
//public class OriginalAtoms implements Filter {
//    private final Atom[] atomsArray;
//    private final int size;
//    public OriginalAtoms(AtomList atomList) {
//	size = atomList.size();
//	atomsArray = new Atom[size];
//	for (int i = 0 ; i < size; i++) {
//	    atomsArray[i] = atomList.atomAt(i);
//	}
//    }
//
//    public boolean accept(Object obj) {
//	Atom atom = (Atom) obj;
//	if (atom == null) throw new RuntimeException("(atom == null)");
//	if (atomsArray == null) throw new RuntimeException("(atomsArray == null)");
//	for (int i = 0 ; i < size; i++) {
//	    String name = atom.name();
//	    Atom iAtom =  atomsArray[i];
//
//	    if (name.equals(iAtom.name()) &
//		(atom.residueNumber() == iAtom.residueNumber()) &
//		atom.residueName().equals(iAtom.residueName())) return true;
//	}
//	return false;
//	}
//}
// --Commented out by Inspection STOP (16:34 31/10/16)
