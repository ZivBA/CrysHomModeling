package meshi.parameters;
import meshi.molecularElements.Atom;

public interface AtomTypes {
    //Termini
    int TRN = Atom.addType("TRN"); // Charged Nitrogen at tne N-terminus
    int TRC = Atom.addType("TRC"); // Carboxyl Carbon at the C-terminus
    int TRO = Atom.addType("TRO"); // Charged Oxygen at tne C-terminus
    //Ala
    int AH  = Atom.addType("AH");
    int AN  = Atom.addType("AN");
    int ACA = Atom.addType("ACA");
    int AC  = Atom.addType("AC");
    int AO  = Atom.addType("AO");
    int ACB = Atom.addType("ACB");

    //Cys
    int CH  = Atom.addType("CH");
    int CN  = Atom.addType("CN");
    int CCA = Atom.addType("CCA");
    int CC  = Atom.addType("CC");
    int CO  = Atom.addType("CO");
    int CCB = Atom.addType("CCB");
    int CSG = Atom.addType("CSG");

    //Asp
    int DH  = Atom.addType("DH");
    int DN  = Atom.addType("DN");
    int DCA = Atom.addType("DCA");
    int DC  = Atom.addType("DC");
    int DO  = Atom.addType("DO");
    int DCB = Atom.addType("DCB");
    int DCG = Atom.addType("DCG");
    int DOD = Atom.addType("DOD");

    //Glu
    int EH  = Atom.addType("EH");
    int EN  = Atom.addType("EN");
    int ECA = Atom.addType("ECA");
    int EC  = Atom.addType("EC");
    int EO  = Atom.addType("EO");
    int ECB = Atom.addType("ECB");
    int ECG = Atom.addType("ECG");
    int ECD = Atom.addType("ECD");
    int EOE = Atom.addType("EOE");

    //Phe
    int FH  = Atom.addType("FH");
    int FN  = Atom.addType("FN");
    int FCA = Atom.addType("FCA");
    int FC  = Atom.addType("FC");
    int FO  = Atom.addType("FO");
    int FCB = Atom.addType("FCB");
    int FCG = Atom.addType("FCG");
    int FCD = Atom.addType("FCD");
    int FCE = Atom.addType("FCE");
    int FCZ = Atom.addType("FCZ");

    //Gly
    int GH  = Atom.addType("GH");
    int GN  = Atom.addType("GN");
    int GCA = Atom.addType("GCA");
    int GC  = Atom.addType("GC");
    int GO  = Atom.addType("GO");

    //His
    int HH  = Atom.addType("HH");
    int HN  = Atom.addType("HN");
    int HCA = Atom.addType("HCA");
    int HC  = Atom.addType("HC");
    int HO  = Atom.addType("HO");
    int HCB = Atom.addType("HCB");
    int HCG = Atom.addType("HCG");
    int HCD = Atom.addType("HCD");
    int HND = Atom.addType("HND");
    int HHD = Atom.addType("HHD");
    int HCE = Atom.addType("HCE");
    int HNE = Atom.addType("HNE");
    int HHE = Atom.addType("HHE");

   //Ile
   int IH  = Atom.addType("IH");
    int IN  = Atom.addType("IN");
    int ICA = Atom.addType("ICA");
    int IC  = Atom.addType("IC");
    int IO  = Atom.addType("IO");
    int ICB = Atom.addType("ICB");
    int ICG1 = Atom.addType("ICG1");
    int ICG2 = Atom.addType("ICG2");
    int ICD = Atom.addType("ICD");

    //Lys
    int KH  = Atom.addType("KH");
    int KN  = Atom.addType("KN");
    int KCA = Atom.addType("KCA");
    int KC  = Atom.addType("KC");
    int KO  = Atom.addType("KO");
    int KCB = Atom.addType("KCB");
    int KCG = Atom.addType("KCG");
    int KCD = Atom.addType("KCD");
    int KCE = Atom.addType("KCE");
    int KNZ = Atom.addType("KNZ");

    //Leu
    int LH  = Atom.addType("LH");
    int LN  = Atom.addType("LN");
    int LCA = Atom.addType("LCA");
    int LC  = Atom.addType("LC");
    int LO  = Atom.addType("LO");
    int LCB = Atom.addType("LCB");
    int LCG = Atom.addType("LCG");
    int LCD1 = Atom.addType("LCD1");
    int LCD2 = Atom.addType("LCD2");

    //Met
    int MH  = Atom.addType("MH");
    int MN  = Atom.addType("MN");
    int MCA = Atom.addType("MCA");
    int MC  = Atom.addType("MC");
    int MO  = Atom.addType("MO");
    int MCB = Atom.addType("MCB");
    int MCG = Atom.addType("MCG");
    int MSD = Atom.addType("MSD");
    int MCE = Atom.addType("MCE");

    //Asn
    int NH  = Atom.addType("NH");
    int NN  = Atom.addType("NN");
    int NCA = Atom.addType("NCA");
    int NC  = Atom.addType("NC");
    int NO  = Atom.addType("NO");
    int NCB = Atom.addType("NCB");
    int NCG = Atom.addType("NCG");
    int NOD = Atom.addType("NOD");
    int NND = Atom.addType("NND");
    int NHD1 = Atom.addType("NHD1");
    int NHD2 = Atom.addType("NHD2");
    //Pro
    int PN  = Atom.addType("PN");
    int PCA = Atom.addType("PCA");
    int PC  = Atom.addType("PC");
    int PO  = Atom.addType("PO");
    int PCB = Atom.addType("PCB");
    int PCG = Atom.addType("PCG");
    int PCD = Atom.addType("PCD");

    //Gln
    int QH  = Atom.addType("QH");
    int QN  = Atom.addType("QN");
    int QCA = Atom.addType("QCA");
    int QC  = Atom.addType("QC");
    int QO  = Atom.addType("QO");
    int QCB = Atom.addType("QCB");
    int QCG = Atom.addType("QCG");
    int QCD = Atom.addType("QCD");
    int QOE = Atom.addType("QOE");
    int QNE = Atom.addType("QNE");
    int QHE1 = Atom.addType("QHE1");
    int QHE2 = Atom.addType("QHE2");

    //Arg
    int RH  = Atom.addType("RH");
    int RN  = Atom.addType("RN");
    int RCA = Atom.addType("RCA");
    int RC  = Atom.addType("RC");
    int RO  = Atom.addType("RO");
    int RCB = Atom.addType("RCB");
    int RCG = Atom.addType("RCG");
    int RCD = Atom.addType("RCD");
    int RNE = Atom.addType("RNE");
    int RHE = Atom.addType("RHE");
    int RCZ = Atom.addType("RCZ");
    int RNH = Atom.addType("RNH");

    //Ser
    int SH  = Atom.addType("SH");
    int SN  = Atom.addType("SN");
    int SCA = Atom.addType("SCA");
    int SC  = Atom.addType("SC");
    int SO  = Atom.addType("SO");
    int SCB = Atom.addType("SCB");
    int SOG = Atom.addType("SOG");

    //Thr
    int TH  = Atom.addType("TH");
    int TN  = Atom.addType("TN");
    int TCA = Atom.addType("TCA");
    int TC  = Atom.addType("TC");
    int TO  = Atom.addType("TO");
    int TCB = Atom.addType("TCB");
    int TCG = Atom.addType("TCG");
    int TOG = Atom.addType("TOG");

    //Val
    int VH  = Atom.addType("VH");
    int VN  = Atom.addType("VN");
    int VCA = Atom.addType("VCA");
    int VC  = Atom.addType("VC");
    int VO  = Atom.addType("VO");
    int VCB = Atom.addType("VCB");
    int VCG1 = Atom.addType("VCG1");
    int VCG2 = Atom.addType("VCG2");

    //Trp
    int WH  = Atom.addType("WH");
    int WN  = Atom.addType("WN");
    int WCA = Atom.addType("WCA");
    int WC  = Atom.addType("WC");
    int WO  = Atom.addType("WO");
    int WCB = Atom.addType("WCB");
    int WCG = Atom.addType("WCG");
    int WCD1 = Atom.addType("WCD1");
    int WCD2 = Atom.addType("WCD2");
    int WCE2 = Atom.addType("WCE2");
    int WCE3 = Atom.addType("WCE3");
    int WNE = Atom.addType("WNE");
    int WHE = Atom.addType("WHE");
    int WCZ2 = Atom.addType("WCZ2");
    int WCZ3 = Atom.addType("WCZ3");
    int WCH2 = Atom.addType("WCH2");
    //Tyr
    int YH  = Atom.addType("YH");
    int YN  = Atom.addType("YN");
    int YCA = Atom.addType("YCA");
    int YC  = Atom.addType("YC");
    int YO  = Atom.addType("YO");
    int YCB = Atom.addType("YCB");
    int YCG = Atom.addType("YCG");
    int YCD = Atom.addType("YCD");
    int YCE = Atom.addType("YCE");
    int YCZ = Atom.addType("YCZ");
    int YOH = Atom.addType("YOH","DONE");
    //
    int[] BB_HYDROGENS = {AH, CH, DH, EH, FH, GH, HH, IH, KH, LH, MH, NH, QH, RH, SH, TH, VH, WH, YH};
    int[] BB_OXYGENS = {AO, CO, DO, EO, FO, GO, HO, IO, KO, LO, MO, NO, PO, QO, RO, SO, TO, VO, WO, YO};
    int[] BB_NITROGENS = {AN, CN, DN, EN, FN, GN, HN, IN, KN, LN, MN, NN, PN, QN, RN, SN, TN, VN, WN,YN, PN, TRN};
    int[] BB_CARBONS = {AC, CC, DC, EC, FC, GC, HC, IC, KC, LC, MC, NC, PC, QC, RC, SC, TC, VC, WC,YC, PC, TRC};}
    
