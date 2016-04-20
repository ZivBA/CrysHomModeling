package ModellingTool;

import ModellingUtilities.molecularElements.ProteinActions;
import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.MRC_Map_New;
import ScoreUtilities.SFCheckIntegration.SFCheckThread;
import ScoreUtilities.ScoringGeneralHelpers;
import ScoreUtilities.scwrlIntegration.SCWRLactions;
import ScoreUtilities.scwrlIntegration.SCWRLrunner;
import UtilExceptions.MissingChainID;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;

import static ScoreUtilities.ScoringGeneralHelpers.csvToMatrix;

/**
 * Created by zivben on 09/04/16.
 */
public class CRYS_Score {
	private final RunParameters params;
	private MRC_Map_New myMap;
	private SimpleProtein myProt;
	public char requestedChain;
	private double[][] proteinIntensityValueMatrix;
	public ArrayList<String> logFile = new ArrayList<>();
	public ArrayList<File> toDelete = new ArrayList<>();
	private List<Integer[]> originalPos;
	File scwrlOutput;

	public CRYS_Score(RunParameters params) {
		this.params = params;
		try {
			this.myProt = new SimpleProtein(params.getPDBsrc(), params);
			requestedChain = params.getChainToProcess();
			//TODO new MAP object
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println("Problem processing PDB file");
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
			ProteinActions.stripAndAllALAToObject(myProt,params);
			System.out.println("Iterating all acid permutations and creating SCWRL input files");
			scwrlOutput = ProteinActions.iterateAcids(myProt,params);
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

					File backBoneIntMatrix = new File(
							tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
									+ "_" + chain.getChainID() + "_BBresultMatrix.csv");

					File correctPositions = new File(
							tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
									+ "_" + chain.getChainID() + "_originalPositions.csv");

					double[][] temp;
					temp = csvToMatrix(resultCSV);
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
				return true;
			}

		} catch (Exception e) {
			System.out.println("Existing CSVs not found or malformed, processing from scratch.");
			;
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
		if (!params.isDebug()){
			for (File deleteCandidate: toDelete){
				deleteCandidate.delete();
			}
		}
	}


}
