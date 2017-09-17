package ModellingTool;

import ModellingUtilities.molecularElements.ProteinActions;
import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.EMCheckIntegration.EMCheckThread;
import ScoreUtilities.EMCheckIntegration.ExtractMaxValue;
import ScoreUtilities.EMCheckIntegration.MRC_Map_New;
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

import static ScoreUtilities.ScoringGeneralHelpers.csvToMatrix;
import static ScoreUtilities.ScoringGeneralHelpers.emNormalMatrix;

/**
 * Created by zivben on 09/04/16.
 */
class EM_Score {
	private final RunParameters params;
	private final ArrayList<File> toDelete = new ArrayList<>();
	private MRC_Map_New myMap;
	private RvalAlignerCluster alignThread;
	private SimpleProtein myProt;
	private char requestedChain;
	private File fastaFile;
	private String fastaSequence;
	private int expectedTotalNumberOfFiles;
	/**
	 * Constructor for EM_Score main program object.
	 * Creates the SimpleProtein object and sets the requested chains to process according to Params.
	 * also calculates the expected number of files to process for progress tracking.
	 *
	 * @param params a RunParameters object used to track and pass around the program parameters.
	 */
	EM_Score(RunParameters params) {
		this.params = params;
		try {
			this.myProt = new SimpleProtein(params.getPDBsrc(), params);
			this.myMap = EMCheckThread.getMRCMap();
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

	void exMax() {
		File targetMarker = new File(myProt.getSource().getParent() + File.separator + "marker.cmm");
		if (targetMarker.isFile()) {
			return;
		}
		if (myMap == null) {
			myMap = new MRC_Map_New(params.getMAPsrc().getAbsolutePath());
		}
		float[] maxValResult = ExtractMaxValue.getMaxValue(myMap);
		System.out.println(Arrays.toString(maxValResult));
		try {
			ExtractMaxValue.writeMarkerFile(myProt.getSource().getParent(), maxValResult);
		} catch (IOException e) {
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
				String upperCaseFileName = myProt.getFileName().toUpperCase();
				if (chain.getChainID() == requestedChain || requestedChain == '\0') {
					File resultCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
							+ "_" + chain.getChainID() + "_resultMatrix.csv");

					File backBoneIntMatrix = new File(
							tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
									+ "_" + chain.getChainID() + "_BBresultMatrix.csv");

					File correctPositions = new File(
							tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
									+ "_" + chain.getChainID() + "_originalPositions.csv");

					double[][] temp;
					temp = ScoringGeneralHelpers.csvToMatrix(resultCSV);
					if (temp != null) {
						chain.resIntensityValueMatrix = temp;
					} else {
						return false;
					}
					temp = csvToMatrix(backBoneIntMatrix);
					if (temp != null) {
						chain.backBoneIntensityValueMatrix = temp;
					} else {
						return false;
					}
					double[][] tempPositions = csvToMatrix(correctPositions);
					if (tempPositions == null)
						return false;
					chain.originalPositions = new Integer[tempPositions[0].length];
					for (int i = 0; i < tempPositions.length; i++) {
						for (int j = 0; j < tempPositions[i].length; j++) {
							chain.originalPositions[j] = (int) tempPositions[i][j];
						}
					}
				}
//				System.out.println("all good with existing CSVs");

			}
			return true;
			
		} catch (Exception e) {
			System.out.println("Existing CSVs not found or malformed, processing from scratch.");
			return false;
		}

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
	 * @param mapCheckResultSet the list of SFCheck results for this chain.
	 * @throws SfCheckResultError if the result line from the SFCheck logfile is not according to known pattern, an error is thrown.
	 */
	RvalAlignerCluster processMapCheckResults(List<Object[]> mapCheckResultSet) throws MissingChainID {

		int chainLength = myProt.getChain(requestedChain).getLength();
//		obj[] format: {residue.getChainID(), acidToIndex(residue.getName()), residue.getPosition(),
//				resSum, backBoneSum}

		double[][] resIntensityMatrix = new double[20][chainLength];
		double[][] backBoneIntensityMatrix = new double[20][chainLength];
		for (Object[] res : mapCheckResultSet) {

			char reqChain = (char) res[0];
			if (reqChain != params.getChainToProcess()) {
				throw new MissingChainID(reqChain, myProt);
			}
			int resType = (int) res[1];
			int resSeqNum = (int) res[2];
			double resIntensity = (double) res[3];
			double bbIntensity = (double) res[4];

			myProt.getChain(requestedChain).backBoneIntensityValueMatrix[resType][resSeqNum] = bbIntensity;
			myProt.getChain(requestedChain).resIntensityValueMatrix[resType][resSeqNum] = resIntensity;

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
					tmpVal += emNormalMatrix[j][k] * chain.allZvalueMatrix[k][i];
				}
				tmpVal += emNormalMatrix[j][20];
				
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
//		List<List<Double>> trueValuesList = new LinkedList<>();
		List<List<Double>> resValueList = new LinkedList<>();
		List<List<Double>> BBValueList = new LinkedList<>();

		for (int i = 0; i < chain.resIntensityValueMatrix.length; i++) {
//			falseValuesList.add(new LinkedList<Double>());
//			trueValuesList.add(new LinkedList<Double>());
			BBValueList.add(new LinkedList<Double>());
			resValueList.add(new LinkedList<Double>());
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
//				double tmpScore = ;
//				if (chain.originalPositions[j] != i) {
//					falseValuesList.get(i).add(tmpScore);
//				} else {
//					trueValuesList.get(i).add(tmpScore);
//				}
				BBValueList.get(i).add(chain.backBoneIntensityValueMatrix[i][j]);
				resValueList.get(i).add(chain.resIntensityValueMatrix[i][j]);

			}
//			Collections.sort(falseValuesList.get(i));
//			Collections.sort(trueValuesList.get(i));
			Collections.sort(BBValueList.get(i));
			Collections.sort(resValueList.get(i));
		}
//		double[] falseValuesMedian = new double[falseValuesList.size()];
//		double[] trueValuesMedian = new double[trueValuesList.size()];
		double[] BBValuesMedian = new double[BBValueList.size()];
		double[] resValuesMedian = new double[resValueList.size()];

//
//		for (int i = 0; i < falseValuesList.size(); i++) {
//			if (falseValuesList.get(i).size() == 0) {
//				falseValuesMedian[i] = 0;
//			} else {
//				int middle = falseValuesList.get(i).size() / 2;
//				if (falseValuesList.get(i).size() % 2 == 1) {
//					falseValuesMedian[i] = falseValuesList.get(i).get(middle);
//				} else {
//					falseValuesMedian[i] = (falseValuesList.get(i).get(middle - 1) + falseValuesList.get(i).get(middle)) / 2.0;
//				}
//			}
//		}


		for (int i = 0; i < BBValueList.size(); i++) {
			if (BBValueList.get(i).size() == 0) {
				BBValuesMedian[i] = 0;
			} else {
				int middle = BBValueList.get(i).size() / 2;
				if (BBValueList.get(i).size() % 2 == 1) {
					BBValuesMedian[i] = BBValueList.get(i).get(middle);
				} else {
					BBValuesMedian[i] = (BBValueList.get(i).get(middle - 1) + BBValueList.get(i).get(middle)) / 2.0;
				}
			}
		}

		for (int i = 0; i < resValueList.size(); i++) {
			if (resValueList.get(i).size() == 0) {
				resValuesMedian[i] = 0;
			} else {
				int middle = resValueList.get(i).size() / 2;
				if (resValueList.get(i).size() % 2 == 1) {
					resValuesMedian[i] = resValueList.get(i).get(middle);
				} else {
					resValuesMedian[i] = (resValueList.get(i).get(middle - 1) + resValueList.get(i).get(middle)) / 2.0;
				}
			}
		}

//		chain.medianTrue = trueValuesMedian;
//		chain.medianFalse = falseValuesMedian;
		chain.allMedian = resValuesMedian;
		chain.backBoneMedian = BBValuesMedian;

	}
	
	
	/**
	 * calc zValue per position/AA combination. only for residue atoms, no BB
	 *
	 * @param chain
	 */
	private void calcZvalue(SimpleProtein.ProtChain chain) {
		double tempAvg[] = new double[20];
		double tempStD[] = new double[20];
		chain.allZvalueMatrix = new double[chain.resIntensityValueMatrix.length][chain.resIntensityValueMatrix[0]
				.length];
		chain.backBoneZvalueMatrix = new double[chain.resIntensityValueMatrix.length][chain.resIntensityValueMatrix[0]
				.length];
//
		//*************************************
		// redo all for backbone, just in case.
		//*************************************


		for (int i = 0; i < chain.backBoneIntensityValueMatrix.length; i++) {
			for (int j = 0; j < chain.backBoneIntensityValueMatrix[i].length; j++) {
				tempAvg[i] += chain.backBoneIntensityValueMatrix[i][j];
			}
			tempAvg[i] = tempAvg[i] / chain.backBoneIntensityValueMatrix[i].length;

			//calc standard deviation for each column
			for (int j = 0; j < chain.backBoneIntensityValueMatrix[i].length; j++) {
				tempStD[i] += ((chain.backBoneIntensityValueMatrix[i][j] - tempAvg[i]) * (chain.backBoneIntensityValueMatrix[i][j] - tempAvg[i]));
			}

			tempStD[i] = (double) Math.round((Math.sqrt(tempStD[i] / chain.backBoneIntensityValueMatrix[i].length)) * 10000000d) / 10000000d;
		}

		// calc Z-Value for each discrete acid in every position
		for (int i = 0; i < chain.backBoneIntensityValueMatrix.length; i++) {
			for (int j = 0; j < chain.backBoneIntensityValueMatrix[i].length; j++) {

				Double tmpScore = (double) Math.round(
						(((chain.backBoneIntensityValueMatrix[i][j]) - chain.backBoneMedian[i]) / tempStD[i]) * 10000000d) / 10000000d;
				if (tempStD[i] < (0.000009d)) {
					chain.backBoneZvalueMatrix[i][j] = 0.0d;
				} else {

					if (tmpScore.isNaN()) {
						tmpScore = 0.0;
					}
					// add score to original acid score only if right position
					if (chain.originalPositions[j] == i) {
						chain.BBTrueZvalue[j] = tmpScore > 4 ? 4 : (tmpScore < -4 ? -4 : tmpScore);
					}
					// add all scores to the allZvalueMatrix.
					chain.backBoneZvalueMatrix[i][j] = tmpScore > 4 ? 4 : (tmpScore < -4 ? -4 : tmpScore);

				}
			}
		}

		// calc tempAvg per column in the intensity value matrix ( avarage of scores per amino acid in every pos)

		tempAvg = new double[20];
		tempStD = new double[20];

		// calc tempAvg per column in the intensity value matrix ( avarage of scores per amino acid in every pos)
		for (int i = 0; i < chain.resIntensityValueMatrix.length; i++) {
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
				tempAvg[i] += chain.resIntensityValueMatrix[i][j];
			}
			tempAvg[i] = tempAvg[i] / chain.resIntensityValueMatrix[i].length;

			//calc standard deviation for each column
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
				double tmpResScore = chain.resIntensityValueMatrix[i][j];
				tempStD[i] += ((tmpResScore - tempAvg[i]) * (tmpResScore - tempAvg[i]));
			}
			tempStD[i] = (double) Math.round((Math.sqrt(tempStD[i] / chain.resIntensityValueMatrix[i].length)) * 10000000d) / 10000000d;
		}

		// calc Z-Value for each discrete acid in every position
		for (int i = 0; i < chain.resIntensityValueMatrix.length; i++) {
			for (int j = 0; j < chain.resIntensityValueMatrix[i].length; j++) {
				double tmpResScore = chain.resIntensityValueMatrix[i][j];
				if (tempStD[i] < (0.000009d)) {
					chain.allZvalueMatrix[i][j] = 0.0d;
				} else {
					Double tmpScore = (double) Math.round(
							((tmpResScore - chain.allMedian[i]) / tempStD[i]) * 10000000d) / 10000000d;
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

		File tempCSVfolder = ScoringGeneralHelpers.makeFolder(new File(myProt.getSource().getParent() + File.separator + "tempCSVs"));
		String upperCaseFileName = myProt.getFileName().toUpperCase();
		System.out.println("Creating CSV Files");
//		{
//
//
//			// run zvaluematrix through de-negativeation vector (try to make all values positive)
//
//			File resultCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
//					+ "_" + chain.getChainID() + "_resultMatrix.csv");
//
//
//			File zscoreCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
//					+ "_" + chain.getChainID() + "_allZscore.csv");
//
//
//			File trueMedian = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
//					+ "_" + chain.getChainID() + "_trueMedian.csv");
//
//			File falseMedian = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
//					+ "_" + chain.getChainID() + "_falseMedian.csv");
//
//
//			File combinedMatrix = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
//					+ "_" + chain.getChainID() + "_profileNoVec.txt");
//
//			File postSvmMatrix = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
//					+ "_" + chain.getChainID() + "_profileWithSVM.txt");
//
//
//			File combinedMatrixLatestVec = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt
//					.getFileName() + "_" + chain.getChainID() + "_profileLatestVec.txt");
//
//
//			File zscoreCorrect = new File(
//					tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName() + "_" + chain
//							.getChainID() + "_zscoreCorrect.csv");
//
//			File correctPositions = new File(
//					tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName() + "_" + chain
//							.getChainID() +
//							"_originalPositions.csv");
//
//
//			File logFileTarget = new File(
//					tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName() + "_" +
//							chain.getChainID() + "_log.txt");
//
//			writeMatrixToCSV(trueMedian, chain.medianTrue);
//			writeMatrixToCSV(falseMedian, chain.medianFalse);
//
//			// generate profile files per vector
//
//			// standard profile with normal VECTOR multiplication *without* SVM weights.
//			writeNewMatrixFormat(combinedMatrix, chain, ScoringGeneralHelpers.normalVector, false);
//			// with backbone score weight.
//			//			writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 10);
//			// standard profile with *latest* VECTOR mult and SVM weights.
//			writeNewMatrixFormat(postSvmMatrix, chain, ScoringGeneralHelpers.normalVector, true);
//
//			writeMatrixToCSV(correctPositions, chain.originalPositions);
//			writeMatrixToCSV(resultCSV, chain.resIntensityValueMatrix);
//			writeMatrixToCSV(zscoreCSV, chain.allZvalueMatrix);
//			writeTrueValueCSVs(zscoreCorrect, chain);
//
//			/***********************************
//			 ********* threading: ***********
//			 **********************************/
//
//
////		String folderPath = myProt.getSource().getParent() + File.separator + "tempCSVs" + File.separator;
////		String filePrefix = myProt.getFileName() + "_" + requestedChain;
////
////		// look for FASTA file in default position, if none is given through GUI.
////		String seqListPath = folderPath + filePrefix + ".fasta";
////
////		if (fastaFile != null) {
////			seqListPath = fastaFile.getAbsolutePath();
////
////		} else if (fastaSequence != null) {
////			FileWriter FW = new FileWriter(seqListPath);
////			FW.write(fastaSequence);
////			FW.close();
////		}
//
//
//			// return align thread without SVM.
//			//String profileFilePathNovec = folderPath + filePrefix + "_profileNoVec.txt";
//			//		RvalAlignerCluster alignThread = new RvalAlignerCluster(swissProtPath, finalSeqListPath, profileFilePathNovec, params);
//
//			// return align thread with SVM
//			String profilePostSVM = postSvmMatrix.getAbsolutePath();
//	//		String profileNoSVM = combinedMatrix.getAbsolutePath();
//		}
		File resultCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_resultMatrix.csv");

		File backBoneIntMatrix = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_BBresultMatrix.csv");

		File zscoreCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_allZscore.csv");

		File zscoreBBCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_allZscoreWtBB.csv");
		File zscoreOnlyFalseCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_onlyFalseZscore.csv");

		File trueMedian = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_trueMedian.csv");

		File falseMedian = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_falseMedian.csv");

		File signalMaybe = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_signalMaybe.csv");

		File combinedMatrixNoVec = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_profileNoVec.txt");


		File combinedMatrixLatestVec = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
				+ "_" + chain.getChainID() + "_profileLatestVec.txt");

//		File combinedMatrixLatestVecWeightBB2 = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
//				+ "_" + chain.getChainID() + "_profileLatestVec_weightedBB_2.txt");
//
//		File combinedMatrixLatestVecWeightBB5 = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
//				+ "_" + chain.getChainID() + "_profileLatestVec_weightedBB_5.txt");
//		File combinedMatrixLatestVecWeightBB10 = new File(tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName
//				+ "_" + chain.getChainID() + "_profileLatestVec_weightedBB_10.txt");

		File zscoreCorrect = new File(
				tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName + "_" + chain
						.getChainID() + "_zscoreCorrect.csv");

		File correctPositions = new File(
				tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName + "_" + chain
						.getChainID() +
						"_originalPositions.csv");

		File backBoneZscore = new File(
				tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName + "_" + chain
						.getChainID() + "_backboneZscore.csv");


		File logFileTarget = new File(
				tempCSVfolder.getAbsolutePath() + File.separator + upperCaseFileName + "_" +
						chain.getChainID() + "_log.txt");

		writeMatrixToCSV(trueMedian, chain.medianTrue);
		writeMatrixToCSV(falseMedian, chain.medianFalse);
		writeMatrixToCSV(signalMaybe, chain.signalMaybe);

		// generate profile files per vector
		//			writeNewMatrixFormat(combinedMatrixVec4, chain, ScoringGeneralHelpers.vector4);
		//			writeNewMatrixFormat(combinedMatrixVec3, chain, ScoringGeneralHelpers.vector3);
//		writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 0.0);
//		writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 0.5);
		writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 1);
//		writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 2);
//		writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 4);
//		writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 8);
//		writeNewMatrixFormat(combinedMatrixLatestVec, chain, ScoringGeneralHelpers.latestVector, 12);
//		writeNewMatrixFormat(combinedMatrixNoVec, chain, ScoringGeneralHelpers.normalVector, 0.0);
//		writeNewMatrixFormat(combinedMatrixNoVec, chain, ScoringGeneralHelpers.normalVector, 0.5);
//		writeNewMatrixFormat(combinedMatrixNoVec, chain, ScoringGeneralHelpers.normalVector, 1);
//		writeNewMatrixFormat(combinedMatrixNoVec, chain, ScoringGeneralHelpers.normalVector, 2);
//		writeNewMatrixFormat(combinedMatrixNoVec, chain, ScoringGeneralHelpers.normalVector, 12);
//		writeNewMatrixFormat(combinedMatrixNoVec, chain, ScoringGeneralHelpers.normalVector, 4);
//		writeNewMatrixFormat(combinedMatrixNoVec, chain, ScoringGeneralHelpers.normalVector, 8);

		writeMatrixToCSV(resultCSV, chain.resIntensityValueMatrix);
		writeMatrixToCSV(backBoneIntMatrix, chain.backBoneIntensityValueMatrix);
		writeFalseZvalueResults(zscoreOnlyFalseCSV, chain);
		writeMatrixToCSV(zscoreCSV, chain.allZvalueMatrix);
		writeMatrixToCSV(backBoneZscore, chain.BBTrueZvalue);
		writeMatrixToCSV(correctPositions, chain.originalPositions);
		writeTrueValueCSVs(zscoreCorrect, chain);
		writeBBzScoreToCSV(zscoreBBCSV, chain, 5);

		alignThread = new RvalAlignerCluster(combinedMatrixLatestVec.getAbsolutePath().replace(".txt", "_weightedBB_1.0.txt"), params);
//		alignThread = new RvalAlignerCluster(combinedMatrixLatestVec.getAbsolutePath(), params);

		
		return alignThread;
	}
	
	
	///////////////////////////////////////////////////
	// Matrix file writers for different type matrices
	///////////////////////////////////////////////////


//	private void writeMatrixToCSV(File outputCSV, double[][] matrix) throws IOException {
//		FileWriter FW = new FileWriter(outputCSV);
//		for (int i = 0; i < matrix[0].length; i++) {
//			String row = "";
//			for (int j = 0; j < matrix.length; j++) {
//				row += matrix[j][i];
//				if (j != matrix.length - 1) {
//					row += ", ";
//				}
//			}
//			row += "\n";
//			FW.write(row);
//		}
//		FW.close();
//	}
//
//	private void writeMatrixToCSV(File outputCSV, double[] matrix) throws IOException {
//		FileWriter FW = new FileWriter(outputCSV);
//
//		String row = "";
//		for (double aMatrix : matrix) {
//			row += aMatrix + "\n";
//		}
//		FW.write(row);
//
//		FW.close();
//	}
//
//	private void writeMatrixToCSV(File outputCSV, Integer[] matrix) throws IOException {
//		FileWriter FW = new FileWriter(outputCSV);
//
//		String row = "";
//		for (Integer aMatrix : matrix) {
//			row += aMatrix + "\n";
//		}
//		FW.write(row);
//
//		FW.close();
//	}
//
//	private void writeNewMatrixFormat(File outputCSV, SimpleProtein.ProtChain chain, double[] vector, boolean useSVM)
//			throws
//			IOException {
//		double[][] workingMatrix;
//		if (useSVM) {
//			workingMatrix = chain.newZvalue;
//		} else {
//			workingMatrix = chain.allZvalueMatrix;
//		}
//
//		double[][] zvaltemp = new double[workingMatrix.length][workingMatrix[0].length];
//		for (int i = 0; i < zvaltemp.length; i++) {
//			System.arraycopy(workingMatrix[i], 0, zvaltemp[i], 0, zvaltemp[0].length);
//		}
//
//
//		ScoringGeneralHelpers.multiplyMatrixByVector(zvaltemp, vector);
//
//		FileWriter FW = new FileWriter(outputCSV);
//		for (int i = 0; i < zvaltemp[0].length; i++) {
//			String row = "";
//			row += chain.getAcidSequenceID(i) + "\t";
//			row += chain.originalPositions[i] + "\t";
//			for (int j = 0; j < workingMatrix.length; j++) {
//				row += (zvaltemp[j][i]) + "\t";
//			}
//
//			row += "\n";
//			FW.write(row);
//		}
//		FW.close();
//
//	}
//
//	//logfile writer for threading and such?
//	private void writeLogToTxtFile(File logFileTarget, ArrayList<String> logFile) throws IOException {
//		FileWriter FW = new FileWriter(logFileTarget);
//		for (String line : logFile) {
//			FW.write(line + "\n");
//		}
//		FW.close();
//	}
//
//	private void writeTrueValueCSVs(File outputCSV, SimpleProtein.ProtChain chain) throws
//			IOException {
//		FileWriter FW = new FileWriter(outputCSV);
//		String row;
//
//		// uncomment for table headers.
//		//		row = "Acid Sequence Id, Single Letter Name, Res zScore, Backbone zScore \n";
//		//		FW.write(row);
//		row = "";
//		for (int j = 0; j < chain.trueZvalues.length; j++) {
//			row += chain.getAcidSequenceID(j) + ", ";
//			row += chain.originalPositions[j] + ", ";
//			row += chain.trueZvalues[j] + "\n";
//
//			//			row += chain.backBoneZvalue[j] + "\n";
//		}
//		FW.write(row);
//
//		FW.close();
//	}

	private void wirteLogToTxtFile(File logFileTarget, ArrayList<String> logFile) throws IOException {
		FileWriter FW = new FileWriter(logFileTarget);
		for (String line : logFile) {
			FW.write(line + "\n");
		}
		FW.close();
	}

	private void writeNewMatrixFormat(File outputCSV, SimpleProtein.ProtChain chain, double[] vector, double weight)
			throws
			IOException {

//		calcZvalue();

		double[][] zvaltemp = new double[chain.allZvalueMatrix.length][chain.allZvalueMatrix[0].length];
		for (int i = 0; i < zvaltemp.length; i++) {
			for (int j = 0; j < zvaltemp[0].length; j++) {
				zvaltemp[i][j] = chain.allZvalueMatrix[i][j];
			}
		}


		ScoringGeneralHelpers.multiplyMatrixByVector(zvaltemp, vector);

		FileWriter FW = new FileWriter(outputCSV);
		for (int i = 0; i < zvaltemp[0].length; i++) {
			String row = "";
			row += chain.getAcidSequenceID(i) + "\t";
			row += chain.originalPositions[i] + "\t";
			for (int j = 0; j < chain.allZvalueMatrix.length; j++) {
				row += (zvaltemp[j][i]) + "\t";
			}

			row += "\n";
			FW.write(row);
		}
		FW.close();

		zvaltemp = new double[chain.allZvalueMatrix.length][chain.allZvalueMatrix[0].length];
		for (int i = 0; i < zvaltemp.length; i++) {
			for (int j = 0; j < zvaltemp[0].length; j++) {
				zvaltemp[i][j] = chain.allZvalueMatrix[i][j] * (1 + (weight * (chain.backBoneZvalueMatrix[i][j] / 4)));
			}
		}

		ScoringGeneralHelpers.multiplyMatrixByVector(zvaltemp, vector);

		FileWriter FWWBB = new FileWriter(new File(outputCSV.getAbsolutePath().replace(".txt", "_weightedBB_" + weight + ".txt")));

		for (int i = 0; i < zvaltemp[0].length; i++) {
			String row = "";
			row += chain.getAcidSequenceID(i) + "\t";
			row += chain.originalPositions[i] + "\t";
			for (int j = 0; j < zvaltemp.length; j++) {
				row += (zvaltemp[j][i]) + "\t";
			}

			row += "\n";
			FWWBB.write(row);
		}
		FWWBB.close();


	}

	private void writeFalseZvalueResults(File outputCSV, SimpleProtein.ProtChain chain) throws IOException {
		FileWriter FW = new FileWriter(outputCSV);
		for (int i = 0; i < chain.allZvalueMatrix[0].length; i++) {
			String row = "";
			for (int j = 0; j < chain.allZvalueMatrix.length; j++) {
				if (chain.originalPositions[i] == j) {
					row += 0.0;
				} else {
					row += chain.allZvalueMatrix[j][i];
				}
				if (j != chain.allZvalueMatrix.length - 1) {
					row += ", ";
				}
			}
			row += "\n";
			FW.write(row);
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

			//			row += chain.BBTrueZvalue[j] + "\n";
		}
		FW.write(row);

		FW.close();
	}


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

	private void writeBBzScoreToCSV(File outputCSV, SimpleProtein.ProtChain chain, int weight) throws IOException {


		FileWriter FW = new FileWriter(outputCSV);
		for (int i = 0; i < chain.allZvalueMatrix[0].length; i++) {
			String row = "";
			for (int j = 0; j < chain.allZvalueMatrix.length; j++) {
				row += (chain.allZvalueMatrix[j][i] * (1 + (chain.backBoneZvalueMatrix[j][i] / weight)));
				if (j != chain.allZvalueMatrix.length - 1) {
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
