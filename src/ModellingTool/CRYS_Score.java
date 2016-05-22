package ModellingTool;

import ModellingUtilities.molecularElements.ProteinActions;
import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.MRC_Map_New;
import ScoreUtilities.SFCheckIntegration.SFCheckThread;
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
public class CRYS_Score {
	private final RunParameters params;
	private MRC_Map_New myMap;
	private SimpleProtein myProt;
	public char requestedChain;
	private double[][] chainIntensityMatrix;
	public ArrayList<String> logFile = new ArrayList<>();
	public ArrayList<File> toDelete = new ArrayList<>();
	private List<Integer[]> originalPos;
	private File scwrlOutput;
	private File fastaFile;
	private String fastaSequence;

	public int getExpectedTotalNumberOfFiles() {
		return expectedTotalNumberOfFiles;
	}

	private int expectedTotalNumberOfFiles;

	public CRYS_Score(RunParameters params) {
		this.params = params;
		try {
			this.myProt = new SimpleProtein(params.getPDBsrc(), params);
			requestedChain = params.getChainToProcess();
			expectedTotalNumberOfFiles = myProt.getChain(requestedChain).getLength() * 20;
			//TODO find a way to read CIF files to map object to allow for multithreading of SFCHECK


		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println("Problem processing PDB file");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int[][] getAcidDist() throws InvalidPropertiesFormatException {
		return (myProt.acidDist != null ? myProt.acidDist : myProt.calcAcidDist());
	}

	public List<List<SCWRLrunner>> iterateAndGenScwrl() throws IOException, MissingChainID {

		List<List<SCWRLrunner>> taskList = new LinkedList<>();
		// if cannot read existing results, generate new files.
		if (!checkExistingCSVs()) {

			System.out.println("Starting protein scoring, saving original positions.");
			myProt.saveOriginalPositions();
			originalPos = myProt.getOriginalPositions();

			System.out.println("Stripping all amino acid resiudes and setting to ALA.");
			ProteinActions.stripAndAllALAToObject(myProt, params);
			System.out.println("Iterating all acid permutations and creating SCWRL input files");
			scwrlOutput = ProteinActions.iterateAcids(myProt, params);
			params.setScwrlOutputFolder(scwrlOutput);
			File chainSubFolders[] = scwrlOutput.listFiles();
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
					for (int i = 0; i < tempPositions.length; i++) {
						for (int j = 0; j < tempPositions[i].length; j++) {
							chain.originalPositions[j] = (int) tempPositions[i][j];
						}
					}
				}
				return true;
			}

		} catch (Exception e) {
			System.out.println("Existing CSVs not found or malformed, processing from scratch.");
			return false;
		}


		return false;
	}


	List<SFCheckThread> processSCWRLfolder() throws IOException, MissingChainID {

		List<SFCheckThread> taskList = new LinkedList<>();
		SimpleProtein tempProt;

		/**
		 * get input File object, read and convert to List of Files (PDB Iterations) for processing.
		 */
		List<File> chainFolders = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(scwrlOutput.toPath())) {
			for (Path path : directoryStream) {
				if (path.toString().endsWith(File.separator + requestedChain)) {
					chainFolders.add(path.toFile());
				} else if (requestedChain == '\0') {
					chainFolders.add(path.toFile());
				}
			}
		} catch (IOException ex) {
			System.out.println("There was a problem processing the SCWRL input folder.");
			System.err.println(ex.getLocalizedMessage());
			System.err.println(ex.getMessage());
			throw ex;
		}

		/**
		 * read each chain File object
		 */
		for (File chain : chainFolders) {
			if (chain.isDirectory()) {
				try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(chain.toPath())) {
					for (Path path : directoryStream) {
						if (path.getFileName().toString().endsWith("_SCWRLed.pdb")) {
							taskList.add(new SFCheckThread(path.toFile(), params));
						}

					}
				} catch (ArrayIndexOutOfBoundsException ex) {
					System.out.println("array index OOB exception for file: " + chain.getAbsolutePath());
				} catch (IOException ex) {
					System.out.println("array index OOB exception for file: " + chain.getAbsolutePath());
					System.err.println(ex.getMessage());
					throw ex;
				}
			}

		}
		return taskList;
	}

	private void scoreSingleScwrl(SimpleProtein tempProt) {
		//TODO process SCWRL file with SFCHECK
	}


	public void deleteChains() {
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


	public void processSFCheckResults(List<String[]> sfCheckResultSet) throws SfCheckResultError, IOException {
		int chainLength = myProt.getChain(requestedChain).getLength();



		Pattern p = Pattern.compile(".*?(?:_(\\d+)_).*?_([A-Z]+)_.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher("");

		chainIntensityMatrix = new double[20][chainLength];
		for (String[] line : sfCheckResultSet) {
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
			chainIntensityMatrix[resNum][position] = Double.parseDouble(line[1]);
			myProt.getChain(requestedChain).resIntensityValueMatrix = chainIntensityMatrix;
		}

		calculations();

	}

	private void calculations() {
		calcMedian(myProt.getChain(requestedChain));
		calcZvalue(myProt.getChain(requestedChain));
		calcSVMweightedZvalue(myProt.getChain(requestedChain));

		try {
			createCSVs(myProt.getChain(requestedChain));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getScwrledFiles() throws IOException {
		List<String> filesToScwrl = new ArrayList<>();

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(scwrlOutput.toPath())) {
			for (Path path : directoryStream) {
				if (!path.getFileName().toString().endsWith("_SCWRLed.pdb")) {
					filesToScwrl.add(path.toString());
				}
			}
		} catch (IOException ex) {
			throw ex;
		}
		return filesToScwrl;
	}

	private void calcSVMweightedZvalue(SimpleProtein.ProtChain chain) {
		for (int i =0; i<chain.newZvalue.length; i++){
			for (int j=0; j < chain.newZvalue[i].length; j++){
				double tempNewZvalue = 0d;
				for (int k=0; k < chain.newZvalue.length; k++){
					tempNewZvalue += (chain.allZvalueMatrix[k][j] * crysSVMmatrix[i][k]);
				}
				tempNewZvalue += crysSVMmatrix[i][20];
				chain.newZvalue[i][j] = tempNewZvalue;
			}
		}
	}



	private void calcMedian(SimpleProtein.ProtChain chain) {
		List<List<Double>> falseValuesList = new LinkedList<>();
		List<List<Double>> trueValuesList = new LinkedList<>();
		List<List<Double>> allValuesList = new LinkedList<>();

		for (int i = 0; i < chain.resIntensityValueMatrix.length; i++) {
			falseValuesList.add(new LinkedList<Double>());
			trueValuesList.add(new LinkedList<Double>());
			allValuesList.add(new LinkedList<Double>());
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
				if (chain.originalPositions[j] != i) {
					falseValuesList.get(i).add(chain.resIntensityValueMatrix[i][j]);
					allValuesList.get(i).add(chain.resIntensityValueMatrix[i][j]);
				} else {
					trueValuesList.get(i).add(chain.resIntensityValueMatrix[i][j]);
					allValuesList.get(i).add(chain.resIntensityValueMatrix[i][j]);
				}
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

	public void calcZvalue() throws InvalidPropertiesFormatException {


		for (SimpleProtein.ProtChain chain : myProt) {
			logFile.add("Calculating Z-Values for chain: " + chain.getChainID());
			calcZvalue(chain);
			//			zValueHelperWithBackBone(chain);
			calcMedian(chain);
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
			tempStD[i] = (double) Math.round((Math.sqrt(tempStD[i] / chain.resIntensityValueMatrix[i].length)) * 10000000d) / 10000000d;
		}


		// calc Z-Value for each discrete acid in every position
		for (int i = 0; i < chain.resIntensityValueMatrix.length; i++) {
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {

				if (tempStD[i] < (0.000009d)) {
					chain.allZvalueMatrix[i][j] = 0.0d;
				} else {
					Double tmpScore = (double) Math.round(
							(((chain.resIntensityValueMatrix[i][j]) - chain.allMedian[i]) / tempStD[i]) * 10000000d) / 10000000d;
					if (tmpScore.isNaN()) {
						tmpScore = 0.0;
					}
					// add score to original acid score only if right position
					if (chain.originalPositions[j] == i) {
						chain.trueZvalues[j] = tmpScore;
					}
					chain.allZvalueMatrix[i][j] = tmpScore;

				}
			}
		}


	}

	public void createCSVs(SimpleProtein.ProtChain chain) throws IOException {

		logFile.add("Creating CSV Files");
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
		writeLogToTxtFile(logFileTarget, logFile);

		// generate profile files per vector

		//			writeNewMatrixFormat(combinedMatrixVec4, chain, ScoringGeneralHelpers.vector4);
		//			writeNewMatrixFormat(combinedMatrixVec3, chain, ScoringGeneralHelpers.vector3);
		//			writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 2);
		//			writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 5);
		//			writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 10);
		writeNewMatrixFormat(combinedMatrix, chain, ScoringGeneralHelpers.normalVector, false);
		writeNewMatrixFormat(postSvmMatrix, chain, ScoringGeneralHelpers.normalVector, true);

		writeMatrixToCSV(correctPositions, chain.originalPositions);
		writeMatrixToCSV(resultCSV, chain.resIntensityValueMatrix);
		writeMatrixToCSV(zscoreCSV, chain.allZvalueMatrix);
		writeTrueValueCSVs(zscoreCorrect, chain);

		//threading:
		String swissProtPath = "/home/zivben/IdeaProjects/Results/uniprot_sprot_10_2015.fasta";

		String folderPath = myProt.getSource().getParent() + File.separator + "tempCSVs" + File.separator;
		String filePrefix = myProt.getFileName() + "_" + requestedChain;

		String seqListPath =  folderPath + filePrefix + ".fasta";
		if (fastaFile != null){
			seqListPath = fastaFile.getAbsolutePath();

		} else if (fastaSequence != null) {
			FileWriter FW = new FileWriter(seqListPath);
			FW.write(fastaSequence);
			FW.close();
		}


		String profileFilePathNovec = folderPath + filePrefix + "_profileNoVec.txt";

		RvalAlignerCluster.runThread(swissProtPath, seqListPath, profileFilePathNovec,params.getFullFasta());


	}

	// Matrix file writers for different type matrices
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
		for (int j = 0; j < matrix.length; j++) {
			row += matrix[j] + "\n";
		}
		FW.write(row);

		FW.close();
	}

	private void writeMatrixToCSV(File outputCSV, Integer[] matrix) throws IOException {
		FileWriter FW = new FileWriter(outputCSV);

		String row = "";
		for (int j = 0; j < matrix.length; j++) {
			row += matrix[j] + "\n";
		}
		FW.write(row);

		FW.close();
	}

	private void writeNewMatrixFormat(File outputCSV, SimpleProtein.ProtChain chain, double[] vector, boolean useSVM)
			throws
			IOException {
		double[][] workingMatrix;
		if (useSVM){
			workingMatrix = chain.newZvalue;
		} else{
			workingMatrix = chain.allZvalueMatrix;
		}

		double[][] zvaltemp = new double[workingMatrix.length][workingMatrix[0].length];
		for (int i = 0; i < zvaltemp.length; i++) {
			for (int j = 0; j < zvaltemp[0].length; j++) {
				zvaltemp[i][j] = workingMatrix[i][j];
			}
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

	public void setFastaFile(File fastaFile) {
		this.fastaFile = fastaFile;
	}

	public void setFastaSequence(String fastaSequence) {
		this.fastaSequence =fastaSequence;

	}
}
