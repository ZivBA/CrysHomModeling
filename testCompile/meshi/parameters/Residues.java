package meshi.parameters;
import meshi.molecularElements.Residue;

public interface Residues extends AtomTypes {
    //Termini
    int ALA = Residue.addName("ALA","A",ACA);
    int CYS = Residue.addName("CYS","C",CCA);
    int ASP = Residue.addName("ASP","D",DCA);
    int GLU = Residue.addName("GLU","E",ECA);
    int PHE = Residue.addName("PHE","F",FCA);
    int GLY = Residue.addName("GLY","G",GCA);
    int HIS = Residue.addName("HIS","H",HCA);
    int ILE = Residue.addName("ILE","I",ICA);
    int LYS = Residue.addName("LYS","K",KCA);
    int LEU = Residue.addName("LEU","L",LCA);
    int MET = Residue.addName("MET","M",MCA);
    int ASN = Residue.addName("ASN","N",NCA);
    int PRO = Residue.addName("PRO","P",PCA);
    int GLN = Residue.addName("GLN","Q",QCA);
    int ARG = Residue.addName("ARG","R",RCA);
    int SER = Residue.addName("SER","S",SCA);
    int THR = Residue.addName("THR","T",TCA);
    int VAL = Residue.addName("VAL","V",VCA);
    int TRP = Residue.addName("TRP","W",WCA);
    int TYR = Residue.addName("TYR","Y",YCA);
    int UNK = Residue.addName("UNK","X",-1,"DONE");
    int SINGLE = 0;
    int NTER = 1;
    int NORMAL = 2;
    int CTER = 3;
    int ADD_ATOMS_AND_FREEZE = 0;
    int ADD_BACKBONE_AND_FREEZE = 4;
    int ADD_ATOMS = 3;
    int DO_NOT_ADD_ATOMS = 1;
    int ADD_HYDROGENS_AND_FREEZE = 2;
    int ADD_NOT_SET = -1;
}
 
  
