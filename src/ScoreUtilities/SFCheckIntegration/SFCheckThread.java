package ScoreUtilities.SFCheckIntegration;

import ModellingTool.MainMenu;
import ModellingTool.RunParameters;
import ModellingUtilities.molecularElements.SimpleProtein;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ScoreUtilities.ScoringGeneralHelpers.PDB_EXTENSION;
import static ScoreUtilities.ScoringGeneralHelpers.makeSubFolderAt;

/**
 * SFCheck worker thread instance.
 * create these when you want to run SFCheck against some PDB file, then pass the worker to an execution pool for processing.
 */
public class SFCheckThread extends SwingWorker<String[],Void>  {
	private static AtomicLong tempFolderCounter = new AtomicLong(0);
	private static byte[] sfcheckProgram;
	private RunParameters params;
	private File SFCheckExe;
	private File outputFolder;
	private File output;
	private File trueOutput;
	private File protToProcess;
	private boolean fakeRun = false;
	private String[] res;
	private boolean cifNotSF = false;

	/**
	 * constructor - gets the PDB file to check and the run parameters object.
	 */
	public SFCheckThread(File tempProt, RunParameters params) throws IOException {
		outputFolder = makeSubFolderAt(params.getScwrlOutputFolder().getParentFile(), "sfcheck_LogFiles");
		outputFolder = makeSubFolderAt(outputFolder, String.valueOf(params.getChainToProcess()));
		
		SFCheckExe = params.getSFChkexe();
		protToProcess = tempProt;
		this.params = params;
		output = new File(outputFolder.getAbsolutePath() + File.separator + tempProt.getName().replaceFirst(PDB_EXTENSION + "+$","_"));
		trueOutput = new File(output.getAbsolutePath() + "sfcheck.log");
		if (trueOutput.exists()){
			fakeRun = true;
			return;
		}
		
		SimpleProtein srcProt = new SimpleProtein(params.getPDBsrc(),params);
		SimpleProtein newProt = new SimpleProtein(tempProt,params);
		newProt.replaceTempValue(srcProt);
		newProt.writePDB(tempProt);


	}
	
	public static void setSfcheckProgram(Path sfChkexe) throws IOException {
		sfcheckProgram = Files.readAllBytes(sfChkexe);
	}

	@Override
	/**
	 * execution method - returns a String array of the results - first line is the protein name (which position was changed to which residue)
	 * second line is the result - Correlation Coefficient.
	 */
	protected String[] doInBackground() throws Exception {

		String result = null;
		String re1="(\\s+)";	// White Space 1
		String re2="(Correlation)";	// Word 1
		String re3="( )";	// White Space 2
		String re4="(factor)";	// Word 2
		String re5="(\\s+)";	// White Space 3
		String re6="(:)";	// Any Single Character 1

		Pattern p = Pattern.compile(re1+re2+re3+re4+re5+re6,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher("");
		
		/**
		 * fake runs are runs for which an SFCheck result already exists. this is sometimes used when debugging and re-running on the same files
		 * without changing the PDB but only recalculating ZScores and such.
		 * only read the result file without actually running the SFCheck binary.
		 */
		if (fakeRun){

			try (
					BufferedReader reader = Files.newBufferedReader(trueOutput.toPath(), Charset.defaultCharset());
					LineNumberReader lineReader = new LineNumberReader(reader)
			){
				String line;
				while ((line = lineReader.readLine()) != null) {
					m.reset(line); //reset the input
					if (m.find()) {
						result = line.substring(24);
						break;
					}
					if (line.equals("ERROR: it is CIFile of coordinates, not SF")) {
						cifNotSF = true;
					}
				}
				reader.close();
			}
			catch (IOException ex){
				ex.printStackTrace();
			}
			if (cifNotSF) {
				return new String[]{"ERROR: it is CIFile of coordinates, not SF"};
				
			}
			return new String[]{protToProcess.getName(),result};
		}
		
		
		File tempDir = new File(
				SFCheckExe.getParent() + File.separatorChar + protToProcess.getName() + "_tempFolder" + tempFolderCounter.incrementAndGet());
		try {
			tempDir.mkdir();
		}catch (Exception e){
			throw e;
		}
		
		Path tempSFcheck = (new File(tempDir.getAbsolutePath()+File.separator+"sfcheck")).toPath();
		Files.write(tempSFcheck,sfcheckProgram);
		tempSFcheck.toFile().setExecutable(true);
		do {
			int counter = 100;
			for (int i = 0; i < 1000; i++) {
				counter++;
			}
		}
		while (!Arrays.equals(Files.readAllBytes(tempSFcheck), sfcheckProgram));
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(tempSFcheck.toAbsolutePath() +
					" -f " + params.getMAPsrc() +
					" -m " + protToProcess.getAbsolutePath() +
					" -po " + output.getAbsolutePath(), null, tempDir.getAbsoluteFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		assert process != null;
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader er = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line;


		try {
			while ((line = br.readLine()) != null) {
				m.reset(line);
				if (m.find()){
					result = line.substring(24);
				}
			}
			while ((line = er.readLine()) != null) {
				System.out.println("SFCheck error: ");
				System.out.println(line);
			}
		} catch (IOException e){
			System.out.println("SFCheck error: ");
			System.out.println(e.getMessage());
		}

		File fobFile = new File(output+"sfcheck_fob.dat");
		File phFile = new File(output+"sfcheck_ph.dat");
		try{
			fobFile.delete();
			phFile.delete();
			protToProcess.delete();
		}catch (Exception e){
			e.printStackTrace();
		}
		for (File childFile : tempDir.listFiles()){
			try{
				childFile.delete();
			} catch (Exception e){
				throw e;
			}
		}
		tempDir.delete();
		res = new String[]{protToProcess.getName(), result};
		return res;

	}
	
	@Override
	/**
	 * when done, add the result to the SFCheck resultset collection and advance the progress counter.
	 */
	protected void done(){
		try{
			
			MainMenu.MapCorrResultSet.add(get());
				setProgress(100);
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}