package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;

import java.io.*;
import java.util.Properties;

/**
 * Created by zivben on 22/03/16.
 */
public class RunParameters extends Properties{
	public static final String SFCHECK_PATH = "SFCheck Executable", SCWRLEXE = "SCWRL Executable", PDBSRC = "PDB Source File";
	public static final String HETATM = "Process Het-Atm",MAPSRC = "Map Source File", DEBUG="Debug Flag", THREADS = "Max threads ratio",
			DEFAULT="Save Defaults";
	private File SCWRLexe;

	private File SFChkexe;

	private File PDBsrc;

	private File MAPsrc;
	private Character[] chainsToStrip;
	private char chainToProcess;
	private Character[] symmetricHomologues;

	private boolean debug;
	private boolean hetAtmProcess;
	private boolean saveDefaults;

	private File scwrlOutputFolder;
	private int threadLimit;

	SimpleProtein sourceProt;

	public RunParameters() throws FileNotFoundException {

		// try to load the properties file, if a current one exists.
		try {
			File configFile = new File("config.xml");
			InputStream inputStream = new FileInputStream(configFile);

			this.loadFromXML(inputStream);
			inputStream.close();



			try {
				setSCWRLexe(new File(this.getProperty(SCWRLEXE)));
			} catch (Exception e) {
				System.err.println("Error opening SCWRL exe, probably missing file.");
			}
			try {
				setSFChkexe(new File(this.getProperty(SFCHECK_PATH)));
			} catch (Exception e) {
				System.err.println("Error opening SFCheck exe, probably missing file.");
			} try {
				setPDBsrc(new File(this.getProperty(PDBSRC)));
			} catch (Exception e){
				System.err.println("Error opening PDB source, probably missing file.");
			}

			setHetAtmProcess(Boolean.parseBoolean(this.getProperty(HETATM)));
			setThreadLimit(Integer.valueOf(this.getProperty(THREADS)));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}


	}
	public boolean isSaveDefaults() {
		return saveDefaults;
	}

	public void setSaveDefaults(boolean saveDefaults) {
		this.setProperty(DEFAULT,String.valueOf(saveDefaults));
		this.saveDefaults = saveDefaults;
	}

	public void setDebug(boolean debug) {
		this.setProperty(DEBUG,String.valueOf(debug));
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public File getSCWRLexe() {
		return SCWRLexe;
	}

	public void setSCWRLexe(File SCWRLexe) {
		this.SCWRLexe = SCWRLexe;
		this.setProperty(SCWRLEXE,SCWRLexe.getAbsolutePath());
	}

	public File getSFChkexe() {
		return SFChkexe;
	}

	public void setSFChkexe(File SFChkexe) {
		this.SFChkexe = SFChkexe;
		this.setProperty(SFCHECK_PATH, SFChkexe.getAbsolutePath());
	}

	public File getPDBsrc() {
		return PDBsrc;
	}

	public String setPDBsrc(File PDBsrc) throws Exception {

		this.PDBsrc = PDBsrc;

		sourceProt = new SimpleProtein(PDBsrc, this);

		this.setProperty(PDBSRC, PDBsrc.getAbsolutePath());

		return "succesfully read " + PDBsrc.getName();
	}

	public File getMAPsrc() {
		return MAPsrc;
	}

	public void setMAPsrc(File MAPsrc) {
		this.MAPsrc = MAPsrc;
		this.setProperty(MAPSRC,MAPsrc.getAbsolutePath());
	}

	public Character[] getChainsToStrip() {
		return chainsToStrip;
	}

	public void setChainsToStrip(Character[] chainsToStrip) {
		this.chainsToStrip = chainsToStrip;
	}

	public char getChainToProcess() {
		return chainToProcess;
	}

	public void setChainToProcess(char chainToProcess) {
		this.chainToProcess = chainToProcess;
	}

	public Character[] getSymmetricHomologues() {
		return symmetricHomologues;
	}

	public void setSymmetricHomologues(Character[] symmetricHomologues) {
		this.symmetricHomologues = symmetricHomologues;
	}

	public boolean isHetAtmProcess() {
		return hetAtmProcess;
	}

	public void setHetAtmProcess(boolean hetAtmProcess) {
		this.hetAtmProcess = hetAtmProcess;
		this.setProperty(HETATM, String.valueOf(hetAtmProcess));
	}

	public int getThreadLimit() {
		return threadLimit;
	}

	public void setThreadLimit(int threadLimit) {
		this.threadLimit = threadLimit;
		this.setProperty(THREADS, String.valueOf(threadLimit));
	}

	public SimpleProtein getProtein() {
		return sourceProt;
	}

	public void setScwrlOutputFolder(File scwrlOutputFolder) {
		this.scwrlOutputFolder = scwrlOutputFolder;
	}

	public File getScwrlOutputFolder() {
		return scwrlOutputFolder;
	}
}
