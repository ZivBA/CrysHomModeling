package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;

import java.io.*;
import java.util.Properties;

/**
 * Created by zivben on 22/03/16.
 */
public class RunParameters extends Properties{
	private static final String SFCHECK_PATH = "SFCheck Executable", SCWRLEXE = "SCWRL Executable", PDBSRC = "PDB Source File";
	private static final String HETATM = "Process Het-Atm",MAPSRC = "Map Source File", DEBUG="Debug Flag", THREADS = "Max threads ratio";
	private boolean debug;

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.setProperty(DEBUG,String.valueOf(debug));
		this.debug = debug;
	}

	private File SCWRLexe;
	private File SFChkexe;
	private File PDBsrc;
	private File MAPsrc;
	private char[] chainsToStrip;
	private char chainToProcess;
	private char[] symmetricHomologues;
	private boolean hetAtmProcess;
	private Double threadLimit;

	SimpleProtein sourceProt;

	public RunParameters() throws FileNotFoundException {
		// load the properties file:
		try {
			File configFile = new File("config.xml");
			InputStream inputStream = new FileInputStream(configFile);

			this.loadFromXML(inputStream);

			try {
				SCWRLexe = new File(this.getProperty(SCWRLEXE));
			} catch (Exception e) {
				System.err.println("Error opening SCWRL exe, probably missing file.");
			}
			try {
				SFChkexe = new File(this.getProperty(SFCHECK_PATH));
			} catch (Exception e) {
				System.err.println("Error opening SFCheck exe, probably missing file.");
			}

			hetAtmProcess = Boolean.parseBoolean(this.getProperty(HETATM));
			threadLimit = Double.valueOf(this.getProperty(THREADS));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}


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

	public String setPDBsrc(File PDBsrc, RunParameters params) {

		this.PDBsrc = PDBsrc;
		try {
			sourceProt = new SimpleProtein(PDBsrc,params);
		} catch (IOException e) {
			return e.getMessage();
		}

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

	public char[] getChainsToStrip() {
		return chainsToStrip;
	}

	public void setChainsToStrip(char[] chainsToStrip) {
		this.chainsToStrip = chainsToStrip;
	}

	public char getChainToProcess() {
		return chainToProcess;
	}

	public void setChainToProcess(char chainToProcess) {
		this.chainToProcess = chainToProcess;
	}

	public char[] getSymmetricHomologues() {
		return symmetricHomologues;
	}

	public void setSymmetricHomologues(char[] symmetricHomologues) {
		this.symmetricHomologues = symmetricHomologues;
	}

	public boolean isHetAtmProcess() {
		return hetAtmProcess;
	}

	public void setHetAtmProcess(boolean hetAtmProcess) {
		this.hetAtmProcess = hetAtmProcess;
		this.setProperty(HETATM, String.valueOf(hetAtmProcess));
	}

	public Double getThreadLimit() {
		return threadLimit;
	}

	public void setThreadLimit(Double threadLimit) {
		this.threadLimit = threadLimit;
		this.setProperty(THREADS, String.valueOf(threadLimit));
	}

	public SimpleProtein getProtein() {
		return sourceProt;
	}
}
