package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.ScoringGeneralHelpers;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

/**
 * Created by zivben on 22/03/16.
 */
public class RunParameters extends Properties {
	static final String SFCHECK_PATH = "SFCheck Executable", SCWRLEXE = "SCWRL Executable", PDBSRC = "PDB Source File",
			SWISSPROTpath = "SWISS-Prot DB Source File";
	static final String HETATM = "Process Het-Atm";
	static final String MAPSRC = "Map Source File";
	static final String DEBUG = "Debug Flag";
	private static final String THREADS = "Max threads ratio";
	static final String DEFAULT = "Save Defaults";
	private static final String CHAIN2PROC = "Chain To Process";
	private static final String HOMCHAIN = "Homologue Chains";
	private static final String CHAIN2STRIP = "Chains to Strip";
	static final String FULL_FASTA = "thread SWISSPROT";
	static final String FASTA_SEQ = "target fasta sequence";
	static final String FASTA_FILE = "target fasta file";
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
	
	private SimpleProtein sourceProt;
	private boolean fullFastaThreading;
	private File SWISSProt;
	private File FASTAFile;
	private String FASTASeq;
	private boolean silent;
	
	
	public int getNumOfBetterThreads() {
		return numOfBetterThreads;
	}
	
	public void setNumOfBetterThreads(int numOfBetterThreads) {
		this.numOfBetterThreads = numOfBetterThreads;
	}
	
	private int numOfBetterThreads;
	private CustomOutputStream customOut;
	
	public RunParameters(String arg) throws FileNotFoundException {
		String curPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		File configFile;
		if (arg == null){
			configFile = new File(curPath.substring(0, curPath.lastIndexOf(File.separatorChar)) + "/config.xml");
		} else{
			configFile = new File(arg);
		}
		// try to load the properties file, if a current one exists.
		try {
			
			
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
			}
			try {
				setPDBsrc(new File(this.getProperty(PDBSRC)));
			} catch (Exception e) {
				System.err.println("Error opening PDB source, probably missing file.");
			}
			try {
				setMAPsrc(new File(this.getProperty(MAPSRC)));
			} catch (Exception e) {
				System.err.println("Error opening MAP source, probably missing file.");
			}
			try {
				setFASTASeq(this.getProperty(FASTA_SEQ));
			} catch (Exception e) {
				System.err.println("Error reading fasta SEQ from xml.");
			}
			try {
				setFASTAFile(new File(this.getProperty(FASTA_FILE)));
			} catch (Exception e) {
				System.err.println("Error opening fasta file.");
			}
			
			try {
				setSWISSProt(new File(this.getProperty(SWISSPROTpath)));
				
			} catch (Exception e){
				System.err.println("Error opening SWISSProt DB file, probably missing file.");
			}
			
			setHetAtmProcess(Boolean.parseBoolean(this.getProperty(HETATM)));
			setThreadLimit(Integer.valueOf(this.getProperty(THREADS)));
			setDebug(Boolean.parseBoolean(this.getProperty(DEBUG)));
			setSaveDefaults(Boolean.parseBoolean(this.getProperty(DEFAULT)));
			setThreadingSelection(Boolean.parseBoolean(this.getProperty(FULL_FASTA)));
			
			
			try {
				List<String> fileContent = Files.readAllLines(FASTAFile.toPath());
				this.FASTASeq = String.join("", fileContent);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			char[] tempchainstostrip = this.getProperty(CHAIN2STRIP).toCharArray();
			Character[] tempchainstostrip2 = new Character[tempchainstostrip.length];
			for (int i = 0; i < tempchainstostrip.length; i++) {
				tempchainstostrip2[i] = tempchainstostrip[i];
			}
			setChainsToStrip(tempchainstostrip2);
			
			setChainToProcess(this.getProperty(CHAIN2PROC).toCharArray()[0]);
			
			char[] symmHomologues = this.getProperty(HOMCHAIN).toCharArray();
			Character[] symmHomologues2 = new Character[symmHomologues.length];
			for (int i = 0; i < symmHomologues.length; i++) {
				symmHomologues2[i] = symmHomologues[i];
			}
			setSymmetricHomologues(symmHomologues2);
			
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		
	}
	
	public boolean isSaveDefaults() {
		return saveDefaults;
	}
	
	public void setSaveDefaults(boolean saveDefaults) {
		this.setProperty(DEFAULT, String.valueOf(saveDefaults));
		this.saveDefaults = saveDefaults;
	}
	
	public void setDebug(boolean debug) {
		this.setProperty(DEBUG, String.valueOf(debug));
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
		ScoringGeneralHelpers.SCWRL_PATH = SCWRLexe.getAbsolutePath();
		this.setProperty(SCWRLEXE, SCWRLexe.getAbsolutePath());
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
		this.setProperty(MAPSRC, MAPsrc.getAbsolutePath());
	}
	
	public Character[] getChainsToStrip() {
		return chainsToStrip;
	}
	
	public void setChainsToStrip(Character[] chainsToStrip) {
		this.chainsToStrip = chainsToStrip;
		String str = "";
		
		for (Character c : chainsToStrip)
			str += c.toString();
		this.setProperty(CHAIN2STRIP, str);
	}
	
	public char getChainToProcess() {
		return chainToProcess;
	}
	
	public void setChainToProcess(char chainToProcess) {
		this.chainToProcess = chainToProcess;
		this.setProperty(CHAIN2PROC, String.valueOf(chainToProcess));
	}
	
	public Character[] getSymmetricHomologues() {
		return symmetricHomologues;
	}
	
	public void setSymmetricHomologues(Character[] symmetricHomologues) {
		this.symmetricHomologues = symmetricHomologues;
		String str = "";
		
		for (Character c : symmetricHomologues)
			str += c.toString();
		this.setProperty(HOMCHAIN, str);
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
	
	public void setThreadingSelection(boolean threadingSelection) {
		this.fullFastaThreading = threadingSelection;
		this.setProperty(FULL_FASTA, String.valueOf(fullFastaThreading));
	}
	
	public boolean getFullFasta() {
		return fullFastaThreading;
	}
	
	public void setCustomOutputStream(CustomOutputStream logger) {
		this.customOut = logger;
	}
	
	public CustomOutputStream getCustomOut() {
		return customOut;
	}
	
	public void setSWISSProt(File SWISSProt) {
		this.SWISSProt = SWISSProt;
		this.setProperty(SWISSPROTpath, SWISSProt.getAbsolutePath());
	}
	public File getSWISSProt() {
		return SWISSProt;
	}
	
	public void setFASTAFile(File fastFile) {
		this.FASTAFile = fastFile;
		this.setProperty(FASTA_FILE, FASTAFile.getAbsolutePath());
	}
	
	public void setFASTASeq(String seq) {
		this.FASTASeq = seq;
		this.setProperty(FASTA_SEQ, FASTASeq);
		
	}
	
	public void setSilent(boolean silent) {
		this.silent = silent;
	}
	
	public String getFastaSeq() {
		return FASTASeq;
	}
	
	public String getFastaFile() {
		return FASTAFile.getAbsolutePath();
	}
}
