package ModellingTool;

import ModellingUtilities.molecularElements.ProteinActions;
import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.ScoringGeneralHelpers;
import ScoreUtilities.scwrlIntegration.SCWRLactions;
import ScoreUtilities.scwrlIntegration.SCWRLrunner;
import UtilExceptions.MissingChainID;
import UtilExceptions.SfCheckResultError;
import alignment.RvalAlignerCluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ScoreUtilities.ScoringGeneralHelpers.crysSVMmatrix;
import static ScoreUtilities.ScoringGeneralHelpers.csvToMatrix;

/**
 * Created by zivben on 09/04/16.
 */
class CRYS_Score {
	private final RunParameters params;
	private final ArrayList<File> toDelete = new ArrayList<>();
	private RvalAlignerCluster alignThread;
	private SimpleProtein myProt;
	private char requestedChain;
	private File fastaFile;
	private String fastaSequence;
	private int expectedTotalNumberOfFiles;
	/**
	 * Constructor for CRYS_Score main program object.
	 * Creates the SimpleProtein object and sets the requested chains to process according to Params.
	 * also calculates the expected number of files to process for progress tracking.
	 *
	 * @param params a RunParameters object used to track and pass around the program parameters.
	 */
	CRYS_Score(RunParameters params) {
		this.params = params;
		try {
			this.myProt = new SimpleProtein(params.getPDBsrc(), params);
			requestedChain = params.getChainToProcess();
			expectedTotalNumberOfFiles = myProt.getChain(requestedChain).getLength() * 20;
			//			TODO find a way to read CIF files to map object to allow for multithreading of SFCHECK
			
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println("Problem processing PDB file");
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	public SimpleProtein getMyProt() {
		return myProt;
	}
	
	/**
	 * Getter for AminoAcid distribution (currently not used)
	 *
	 * @return 2D array of the acid distribution in the protein.
	 * @throws InvalidPropertiesFormatException when the protein file cannot be processed for some reason.
	 */
	public int[][] getAcidDist() throws InvalidPropertiesFormatException {
		return (myProt.acidDist != null ? myProt.acidDist : myProt.calcAcidDist());
	}
	
	/**
	 * Combinatorical and main part of the program -
	 * Processes "myProt" protein object - strips all amino acids and converts them to ALA.
	 * then iterates over each position, placing each AA in that position and generates a new PDB file.
	 * Generates a SCWRLrunner object for each such permutation. to be executed later.
	 *
	 * @return List of Lists of SCWRLrunner objects. topmost list is of chains, each lower list is for
	 * SCWRL future executions per PDB permutation.
	 * @throws IOException    If there's a problem reading some files or writing to disk.
	 * @throws MissingChainID if there is no Chain ID to use for reference.
	 */
	List<List<SCWRLrunner>> iterateAndGenScwrl() throws IOException, MissingChainID {
		
		List<List<SCWRLrunner>> taskList = new LinkedList<>();
		// For faster executions when changing calculation parameters, if the raw data exists, only go through the calculation part of the program.
		if (!checkExistingCSVs()) {
			
			System.out.println("Starting protein scoring, saving original positions.");
			myProt.saveOriginalPositions();
			
			System.out.println("Stripping all amino acid resiudes and setting to ALA for selected chains.");
			ProteinActions.stripAndAllALAToObject(myProt, params);
			System.out.println("Iterating all acid permutations and creating SCWRL input files");
			System.out.flush();
			File scwrlOutput = ProteinActions.iterateAcids(myProt, params);
			params.setScwrlOutputFolder(scwrlOutput);
			File chainSubFolders[] = scwrlOutput.listFiles();
			assert chainSubFolders != null;
			for (File chain : chainSubFolders) {
				if (chain.isDirectory()) {
					if (chain.getAbsolutePath().endsWith(File.separator + requestedChain) || requestedChain == '\0') {
						taskList.add(SCWRLactions.genSCWRLforFolder(chain));
						if (!params.isDebug()) {
							toDelete.add(chain);
						}
					}
				}
			}
			
			
		} else {
			calculations();
			return null;
		}
		return taskList;
	}
	
	/**
	 * looks for the raw data CSV files that are generated by SCWRL and other parts of the program.
	 * if they exist, read them and populate the relevant fields with the data for further processing.
	 *
	 * @return true if all raw data is present, false if something is missing or unreadable.
	 */
	private boolean checkExistingCSVs() {
		
		//try reading CSVs
		try {
			File tempCSVfolder = ScoringGeneralHelpers.makeFolder(new File(myProt.getSource().getParent() + File
					.separator + "tempCSVs"));
			
			
			for (SimpleProtein.ProtChain chain : myProt) {
				if (chain.getChainID() == requestedChain || requestedChain == '\0') {
					File resultCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
							+ "_" + chain.getChainID() + "_resultMatrix.csv");
					
					File correctPositions = new File(
							tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
									+ "_" + chain.getChainID() + "_originalPositions.csv");
					
					double[][] temp;
					temp = csvToMatrix(resultCSV);
					if (temp != null) {
						chain.resIntensityValueMatrix = temp;
					} else {
						System.out.println("Existing CSVs not found or malformed, processing from scratch.");
						return false;
					}
					
					double[][] tempPositions = csvToMatrix(correctPositions);
					if (tempPositions == null) {
						System.out.println("Existing CSVs not found or malformed, processing from scratch.");
						return false;
					}
					chain.originalPositions = new Integer[tempPositions[0].length];
					for (double[] tempPosition : tempPositions) {
						for (int j = 0; j < tempPosition.length; j++) {
							chain.originalPositions[j] = (int) tempPosition[j];
						}
					}
					return true;
				}
				
			}
			
		} catch (Exception e) {
			System.out.println("Existing CSVs not found or malformed, processing from scratch.");
			return false;
		}
		
		
		return false;
	}
	
	/**
	 * once done processing the temporary PDB files, delete them if requested.
	 */
	void deleteChains() {
		if (!params.isDebug()) {
			for (File deleteCandidate : toDelete) {
				deleteCandidate.delete();
				
				try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(deleteCandidate.toPath())) {
					for (Path path : directoryStream) {
						if (!path.getFileName().toString().endsWith("_SCWRLed.pdb")) {
							path.toFile().delete();
						}
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Read the SFCheck result list and populate the relevant fields with the CC values.
	 *
	 * @param sfCheckResultSet the list of SFCheck results for this chain.
	 * @throws SfCheckResultError if the result line from the SFCheck logfile is not according to known pattern, an error is thrown.
	 */
	RvalAlignerCluster processSFCheckResults(List<String[]> sfCheckResultSet) throws SfCheckResultError {
		//		if (sfCheckResultSet.size() != 2){
		//			throw new SfCheckResultError(sfCheckResultSet.get(0)[0]);
		//		}
		int chainLength = myProt.getChain(requestedChain).getLength();
		Pattern p = Pattern.compile(".*?(?:_(\\d+)_).*?_([A-Z]+)_.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher("");
		
		double[][] chainIntensityMatrix = new double[20][chainLength];
		for (String[] line : sfCheckResultSet) {
			if (line[0].equals("ERROR: it is CIFile of coordinates, not SF")) {
				throw new SfCheckResultError(line[0]);
			}
			m.reset(line[0]);
			if (!m.matches()) {
				throw new SfCheckResultError(line[0]);
			}
			int position = 0;
			int resNum = 0;
			try {
				position = Integer.parseInt(m.group(1));
				resNum = ProteinActions.acidToIndex(m.group(2));
			} catch (Exception e) {
				System.out.println("Problem matching string: " + line[0]);
				e.printStackTrace();
			}
			try {
				chainIntensityMatrix[resNum][position] = Double.parseDouble(line[1]);
				myProt.getChain(requestedChain).resIntensityValueMatrix = chainIntensityMatrix;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		
		calculations();
		alignThread = null;
		try {
			alignThread = createCSVs(myProt.getChain(requestedChain));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return alignThread;
	}
	
	
	/**
	 * Calculate Z-Scores, Medians and SVM-Weighted Z-Score from raw data.
	 */
	private void calculations() {
		System.out.println("Starting calculations\n");
		calcMedian(myProt.getChain(requestedChain));
		calcZvalue(myProt.getChain(requestedChain));
		calcSVMweightedZvalue(myProt.getChain(requestedChain));
	}
	
	private void calcSVMweightedZvalue(SimpleProtein.ProtChain chain) {
		
		for (int i = 0; i < chain.newZvalue[0].length; i++) {
			for (int j = 0; j < 20; j++) {
				double tmpVal = 0d;
				for (int k = 0; k < 20; k++) {
					tmpVal += crysSVMmatrix[j][k] * chain.allZvalueMatrix[k][i];
				}
				tmpVal += crysSVMmatrix[j][20];
				
				chain.newZvalue[j][i] = tmpVal;
			}
		}
	}
	
	/**
	 * calculate the Median score for a protein chain. this is used for Z-Score calculations later.
	 * also calculate seperate Median for the True results and False results (results in true positions Vs. all other iterations).
	 *
	 * @param chain The chain to calculate for.
	 */
	private void calcMedian(SimpleProtein.ProtChain chain) {
		List<List<Double>> falseValuesList = new LinkedList<>();
		List<List<Double>> trueValuesList = new LinkedList<>();
		List<List<Double>> allValuesList = new LinkedList<>();
		
		for (int i = 0; i < chain.resIntensityValueMatrix.length; i++) {
			falseValuesList.add(new LinkedList<>());
			trueValuesList.add(new LinkedList<>());
			allValuesList.add(new LinkedList<>());
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
				if (chain.originalPositions[j] != i) {
					falseValuesList.get(i).add(chain.resIntensityValueMatrix[i][j]);
					
				} else {
					trueValuesList.get(i).add(chain.resIntensityValueMatrix[i][j]);
					
				}
				allValuesList.get(i).add(chain.resIntensityValueMatrix[i][j]);
			}
			Collections.sort(falseValuesList.get(i));
			Collections.sort(trueValuesList.get(i));
			Collections.sort(allValuesList.get(i));
		}
		double[] falseValuesMedian = new double[falseValuesList.size()];
		double[] trueValuesMedian = new double[trueValuesList.size()];
		double[] allValuesMedian = new double[allValuesList.size()];
		
		
		for (int i = 0; i < falseValuesList.size(); i++) {
			if (falseValuesList.get(i).size() == 0) {
				falseValuesMedian[i] = 0;
			} else {
				int middle = falseValuesList.get(i).size() / 2;
				if (falseValuesList.get(i).size() % 2 == 1) {
					falseValuesMedian[i] = falseValuesList.get(i).get(middle);
				} else {
					falseValuesMedian[i] = (falseValuesList.get(i).get(middle - 1) + falseValuesList.get(i).get(middle)) / 2.0;
				}
			}
		}
		
		for (int i = 0; i < allValuesList.size(); i++) {
			if (allValuesList.get(i).size() == 0) {
				allValuesMedian[i] = 0;
			} else {
				int middle = allValuesList.get(i).size() / 2;
				if (allValuesList.get(i).size() % 2 == 1) {
					allValuesMedian[i] = allValuesList.get(i).get(middle);
				} else {
					allValuesMedian[i] = (allValuesList.get(i).get(middle - 1) + allValuesList.get(i).get(middle)) / 2.0;
				}
			}
		}
		
		for (int i = 0; i < trueValuesList.size(); i++) {
			if (trueValuesList.get(i).size() == 0) {
				trueValuesMedian[i] = 0;
			} else {
				int middle = trueValuesList.get(i).size() / 2;
				if (trueValuesList.get(i).size() % 2 == 1) {
					trueValuesMedian[i] = trueValuesList.get(i).get(middle);
				} else {
					trueValuesMedian[i] = (trueValuesList.get(i).get(middle - 1) + trueValuesList.get(i).get(middle)) / 2.0;
				}
			}
		}
		
		chain.medianTrue = trueValuesMedian;
		chain.medianFalse = falseValuesMedian;
		chain.allMedian = allValuesMedian;
		
		for (int i = 0; i < chain.signalMaybe.length; i++) {
			chain.signalMaybe[i] = chain.medianTrue[i] - chain.medianFalse[i];
		}
		
	}
	
	
	/**
	 * calc zValue per position/AA combination. only for residue atoms, no BB
	 *
	 * @param chain
	 */
	private void calcZvalue(SimpleProtein.ProtChain chain) {
		double tempAvg[] = new double[20];
		double tempStD[] = new double[20];
		chain.allZvalueMatrix = new double[20][chain.resIntensityValueMatrix[0].length];
		
		
		// ture z-score uses mean, we use Median.
		
		//		// calc tempAvg per column in the intensity value matrix ( avarage of scores per amino acid in every pos)
		for (int i = 0; i < chain.resIntensityValueMatrix.length; i++) {
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
				tempAvg[i] += chain.resIntensityValueMatrix[i][j];
			}
			tempAvg[i] = tempAvg[i] / chain.resIntensityValueMatrix[i].length;
			
			//			calc standard deviation for each column
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
				tempStD[i] += ((chain.resIntensityValueMatrix[i][j] - tempAvg[i]) * (chain.resIntensityValueMatrix[i][j] - tempAvg[i]));
			}
			tempStD[i] = (double) Math.round((Math.sqrt(tempStD[i] / (double) chain.resIntensityValueMatrix[i].length)) * 10000d) / 10000d;
		}
		
		
		// calc Z-Value for each discrete acid in every position
		for (int i = 0; i < chain.resIntensityValueMatrix.length; i++) {
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
				
				if (tempStD[i] < (0.00009d)) {
					chain.allZvalueMatrix[i][j] = 0.0d;
				} else {
					Double tmpScore = (double) Math.round(
							(((chain.resIntensityValueMatrix[i][j]) - chain.allMedian[i]) / tempStD[i]) * 10000d) / 10000d;
					if (tmpScore.isNaN()) {
						tmpScore = 0.0;
					}
					// add score to original acid score only if right position
					if (chain.originalPositions[j] == i) {
						chain.trueZvalues[j] = tmpScore > 4 ? 4 : (tmpScore < -4 ? -4 : tmpScore);
					}
					
					chain.allZvalueMatrix[i][j] = tmpScore > 4 ? 4 : (tmpScore < -4 ? -4 : tmpScore);
					
				}
			}
		}
		
		
	}
	
	private RvalAlignerCluster createCSVs(SimpleProtein.ProtChain chain) throws IOException {
		
		System.out.println("Creating CSV Files");
		File tempCSVfolder = ScoringGeneralHelpers.makeFolder(new File(myProt.getSource().getParent() + File.separator + "tempCSVs"));
		
		
		// run zvaluematrix through de-negativeation vector (try to make all values positive)
		
		File resultCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
				+ "_" + chain.getChainID() + "_resultMatrix.csv");
		
		
		File zscoreCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
				+ "_" + chain.getChainID() + "_allZscore.csv");
		
		
		File trueMedian = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
				+ "_" + chain.getChainID() + "_trueMedian.csv");
		
		File falseMedian = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
				+ "_" + chain.getChainID() + "_falseMedian.csv");
		
		
		File combinedMatrix = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
				+ "_" + chain.getChainID() + "_profileNoVec.txt");
		
		File postSvmMatrix = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
				+ "_" + chain.getChainID() + "_profileWithSVM.txt");
		
		
		File combinedMatrixLatestVec = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt
				.getFileName() + "_" + chain.getChainID() + "_profileLatestVec.txt");
		
		
		File zscoreCorrect = new File(
				tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName() + "_" + chain
						.getChainID() + "_zscoreCorrect.csv");
		
		File correctPositions = new File(
				tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName() + "_" + chain
						.getChainID() +
						"_originalPositions.csv");
		
		
		File logFileTarget = new File(
				tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName() + "_" +
						chain.getChainID() + "_log.txt");
		
		writeMatrixToCSV(trueMedian, chain.medianTrue);
		writeMatrixToCSV(falseMedian, chain.medianFalse);
		
		// generate profile files per vector
		
		// standard profile with normal VECTOR multiplication *without* SVM weights.
		writeNewMatrixFormat(combinedMatrix, chain, ScoringGeneralHelpers.normalVector, false);
		// with backbone score weight.
		//			writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 10);
		// standard profile with *latest* VECTOR mult and SVM weights.
		writeNewMatrixFormat(postSvmMatrix, chain, ScoringGeneralHelpers.normalVector, true);
		
		writeMatrixToCSV(correctPositions, chain.originalPositions);
		writeMatrixToCSV(resultCSV, chain.resIntensityValueMatrix);
		writeMatrixToCSV(zscoreCSV, chain.allZvalueMatrix);
		writeTrueValueCSVs(zscoreCorrect, chain);
		
		/***********************************
		 ********* threading: ***********
		 **********************************/
		
		
//		String folderPath = myProt.getSource().getParent() + File.separator + "tempCSVs" + File.separator;
//		String filePrefix = myProt.getFileName() + "_" + requestedChain;
//
//		// look for FASTA file in default position, if none is given through GUI.
//		String seqListPath = folderPath + filePrefix + ".fasta";
//
//		if (fastaFile != null) {
//			seqListPath = fastaFile.getAbsolutePath();
//
//		} else if (fastaSequence != null) {
//			FileWriter FW = new FileWriter(seqListPath);
//			FW.write(fastaSequence);
//			FW.close();
//		}
		
		
		// return align thread without SVM.
		//String profileFilePathNovec = folderPath + filePrefix + "_profileNoVec.txt";
		//		RvalAlignerCluster alignThread = new RvalAlignerCluster(swissProtPath, finalSeqListPath, profileFilePathNovec, params);
		
		// return align thread with SVM
		String profilePostSVM = postSvmMatrix.getAbsolutePath();
//		String profileNoSVM = combinedMatrix.getAbsolutePath();
		
		alignThread = new RvalAlignerCluster(profilePostSVM, params);
		
		
		return alignThread;
	}
	
	
	///////////////////////////////////////////////////
	// Matrix file writers for different type matrices
	///////////////////////////////////////////////////
	
	
	private void writeMatrixToCSV(File outputCSV, double[][] matrix) throws IOException {
		FileWriter FW = new FileWriter(outputCSV);
		for (int i = 0; i < matrix[0].length; i++) {
			String row = "";
			for (int j = 0; j < matrix.length; j++) {
				row += matrix[j][i];
				if (j != matrix.length - 1) {
					row += ", ";
				}
			}
			row += "\n";
			FW.write(row);
		}
		FW.close();
	}
	
	private void writeMatrixToCSV(File outputCSV, double[] matrix) throws IOException {
		FileWriter FW = new FileWriter(outputCSV);
		
		String row = "";
		for (double aMatrix : matrix) {
			row += aMatrix + "\n";
		}
		FW.write(row);
		
		FW.close();
	}
	
	private void writeMatrixToCSV(File outputCSV, Integer[] matrix) throws IOException {
		FileWriter FW = new FileWriter(outputCSV);
		
		String row = "";
		for (Integer aMatrix : matrix) {
			row += aMatrix + "\n";
		}
		FW.write(row);
		
		FW.close();
	}
	
	private void writeNewMatrixFormat(File outputCSV, SimpleProtein.ProtChain chain, double[] vector, boolean useSVM)
			throws
			IOException {
		double[][] workingMatrix;
		if (useSVM) {
			workingMatrix = chain.newZvalue;
		} else {
			workingMatrix = chain.allZvalueMatrix;
		}
		
		double[][] zvaltemp = new double[workingMatrix.length][workingMatrix[0].length];
		for (int i = 0; i < zvaltemp.length; i++) {
			System.arraycopy(workingMatrix[i], 0, zvaltemp[i], 0, zvaltemp[0].length);
		}
		
		
		ScoringGeneralHelpers.multiplyMatrixByVector(zvaltemp, vector);
		
		FileWriter FW = new FileWriter(outputCSV);
		for (int i = 0; i < zvaltemp[0].length; i++) {
			String row = "";
			row += chain.getAcidSequenceID(i) + "\t";
			row += chain.originalPositions[i] + "\t";
			for (int j = 0; j < workingMatrix.length; j++) {
				row += (zvaltemp[j][i]) + "\t";
			}
			
			row += "\n";
			FW.write(row);
		}
		FW.close();
		
	}
	
	//logfile writer for threading and such?
	private void writeLogToTxtFile(File logFileTarget, ArrayList<String> logFile) throws IOException {
		FileWriter FW = new FileWriter(logFileTarget);
		for (String line : logFile) {
			FW.write(line + "\n");
		}
		FW.close();
	}
	
	private void writeTrueValueCSVs(File outputCSV, SimpleProtein.ProtChain chain) throws
			IOException {
		FileWriter FW = new FileWriter(outputCSV);
		String row;
		
		// uncomment for table headers.
		//		row = "Acid Sequence Id, Single Letter Name, Res zScore, Backbone zScore \n";
		//		FW.write(row);
		row = "";
		for (int j = 0; j < chain.trueZvalues.length; j++) {
			row += chain.getAcidSequenceID(j) + ", ";
			row += chain.originalPositions[j] + ", ";
			row += chain.trueZvalues[j] + "\n";
			
			//			row += chain.backBoneZvalue[j] + "\n";
		}
		FW.write(row);
		
		FW.close();
	}
	
	
	int getExpectedTotalNumberOfFiles() {
		return expectedTotalNumberOfFiles;
	}
	
	void setFastaFile(File fastaFile) {
		this.fastaFile = fastaFile;
		try {
			List<String> fileContent = Files.readAllLines(fastaFile.toPath());
			this.fastaSequence = String.join("\n", fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void setFastaSequence(String fastaSequence) {
		this.fastaSequence = fastaSequence;
		
	}
	
	public RvalAlignerCluster getRvalThread() throws IOException {
		return createCSVs(myProt.getChain(requestedChain));
	}
}
